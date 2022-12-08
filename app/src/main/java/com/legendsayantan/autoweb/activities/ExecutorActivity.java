package com.legendsayantan.autoweb.activities;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legendsayantan.autoweb.BuildConfig;
import com.legendsayantan.autoweb.R;
import com.legendsayantan.autoweb.interfaces.AutomationData;
import com.legendsayantan.autoweb.workers.ScriptRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class ExecutorActivity extends AppCompatActivity {
    AutomationData data  = new AutomationData();
    boolean execution = false ;
    WebView webView;
    ImageView back,btn;
    TextView urlText;
    ScriptRunner runner;
    String code = "";
    ConstraintLayout loader;
    boolean started = false;
    @SuppressLint({"MissingInflatedId", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executor);
        webView=findViewById(R.id.webView);
        btn = findViewById(R.id.btn);
        back = findViewById(R.id.back);
        urlText = findViewById(R.id.urlText);
        loader = findViewById(R.id.loader);
        loader.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webView.getSettings().setForceDark(
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("dark",false)?
                    WebSettings.FORCE_DARK_ON:WebSettings.FORCE_DARK_OFF);
        }
        webView.getSettings().setSupportZoom(true);
        WebView.setWebContentsDebuggingEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        code = readAsset("default_script.js");
        back.setOnClickListener(v -> {
            if(webView.canGoBack())webView.goBack();
            else ExecutorActivity.super.onBackPressed();
        });
        try {
            data = getList().get(getIntent().getIntExtra("data",-1));
        } catch (JsonProcessingException e) {
            Toast.makeText(this, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loader.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                urlText.setText(url);
                pauseExecution();
                loader.setVisibility(View.GONE);
                if(!started && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("auto",false)){
                    started=true;
                    btn.callOnClick();
                }
            }
        });
        webView.loadUrl(data.jsActions.get(0).getUrl());
        if(BuildConfig.DEBUG)System.out.println(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("actions","[]"));
    }
    String readAsset(String file){
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getAssets().open(file), StandardCharsets.UTF_8))) {

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                data.append(mLine).append("\n");
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"could not execute javascript code",Toast.LENGTH_LONG).show();
        }
        return data.toString();
    }
    public void pauseExecution(){
        execution = false;
        if(runner!=null)runner.pause();
        btn.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_play_circle_outline_24));
        btn.setOnClickListener((v)->{
            Toast.makeText(getApplicationContext(),"Executing recorded data.",Toast.LENGTH_SHORT).show();
            resumeExecution();
        });
    }
    public void resumeExecution(){
        execution =true;
        btn.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_pause_circle_outline_24));
        btn.setOnClickListener((v)->{
            pauseExecution();
        });
        runner = new ScriptRunner(data);
        runner.executeOn(webView,code,()->{
            loader.setVisibility(View.VISIBLE);
        },()->{
            loader.setVisibility(View.GONE);
            urlText.setText(webView.getUrl());
        },ExecutorActivity.this);
    }
    public ArrayList<AutomationData> getList() throws JsonProcessingException {
        String data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("actions","[]");
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(data, new TypeReference<ArrayList<AutomationData>>(){});
    }
}
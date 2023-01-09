package com.legendsayantan.autoweb.activities;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
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
import androidx.core.content.ContextCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legendsayantan.autoweb.BuildConfig;
import com.legendsayantan.autoweb.R;
import com.legendsayantan.autoweb.interfaces.AutomationData;
import com.legendsayantan.autoweb.workers.ColorParser;
import com.legendsayantan.autoweb.workers.ScriptRunner;
import com.legendsayantan.autoweb.workers.WebDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;


public class ExecutorActivity extends AppCompatActivity {
    AutomationData data  = new AutomationData();
    boolean execution = false ;
    WebView webView;
    ImageView back,btn;
    TextView urlText;
    ScriptRunner runner;
    String code = "";
    WebDriver driver;
    ConstraintLayout loader;
    boolean started = false;
    @SuppressLint({"MissingInflatedId", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_executor);
        findViewById(R.id.container).setBackgroundColor(ColorParser.getPrimary(this));
        getWindow().setStatusBarColor(ColorParser.getPrimary(this));
        Objects.requireNonNull(getSupportActionBar()).hide();
        webView=findViewById(R.id.webView);
        btn = findViewById(R.id.btn);
        btn.setColorFilter(ColorParser.getSecondary(this));
        back = findViewById(R.id.back);
        back.setColorFilter(ColorParser.getSecondary(this));
        urlText = findViewById(R.id.urlText);
        loader = findViewById(R.id.loader);
        loader.setVisibility(View.GONE);
        if(Configuration.ORIENTATION_LANDSCAPE==getResources().getConfiguration().orientation){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        driver = new WebDriver(this,webView);
        code = readAsset("default.js");
        back.setOnClickListener(v -> {
            if(runner!=null)runner.resetExecution();
            super.onBackPressed();
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
        if(data.isLandscape())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if(data.isDesktopMode()) {
            webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36");
            webView.getSettings().setLoadWithOverviewMode(false);
        }
        webView.loadUrl(data.jsActions.get(0).getUrl());
        if(BuildConfig.DEBUG)System.out.println(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("actions","[]"));
        runner = new ScriptRunner(data);
        runner.setOnPause(()->{
            pauseExecution();
            Toast.makeText(getApplicationContext(),"Script Paused.",Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        driver.createMenu(menu);
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
            Toast.makeText(getApplicationContext(),"could not read metadata, app might be corrupted.",Toast.LENGTH_LONG).show();
        }
        return data.toString();
    }
    public void pauseExecution(){
        execution = false;
        if(runner!=null)runner.pause();
        btn.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_play_circle_outline_24));
        btn.setOnClickListener((v)->{
            Toast.makeText(getApplicationContext(),"Executing.",Toast.LENGTH_SHORT).show();
            resumeExecution();
        });
    }
    public void resumeExecution(){
        if(execution)return;
        execution =true;
        btn.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_pause_circle_outline_24));
        btn.setOnClickListener((v)->{
            Toast.makeText(getApplicationContext(),"Paused.",Toast.LENGTH_SHORT).show();
            pauseExecution();
        });
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

    @Override
    public void onBackPressed() {
        if(webView.canGoBack())webView.goBack();
        else {
            finish();
            ExecutorActivity.super.onBackPressed();
        }

    }
}
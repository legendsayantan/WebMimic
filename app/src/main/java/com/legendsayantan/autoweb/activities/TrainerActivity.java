package com.legendsayantan.autoweb.activities;
import static com.legendsayantan.autoweb.interfaces.AutomationData.optimise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.legendsayantan.autoweb.R;
import com.legendsayantan.autoweb.interfaces.AutomationData;
import com.legendsayantan.autoweb.interfaces.JsAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;


public class TrainerActivity extends AppCompatActivity {
    AutomationData data  = new AutomationData();
    boolean record = false ;
    WebView webView;
    ImageView btn,btn2,back;
    EditText editText;
    String loadedUrl;
    String code = "console.log('start trainer mode');";
    ConstraintLayout loader;
    @SuppressLint({"MissingInflatedId", "SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        //remove action bar
        Objects.requireNonNull(getSupportActionBar()).hide();
        webView=findViewById(R.id.webView);
        btn = findViewById(R.id.btn);
        btn2 = findViewById(R.id.btn2);
        back = findViewById(R.id.back);
        loader = findViewById(R.id.loader);
        editText = findViewById(R.id.editText);
        webView.getSettings().setSupportZoom(true);
        WebView.setWebContentsDebuggingEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webView.getSettings().setForceDark(
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getBoolean("dark",false)?
                            WebSettings.FORCE_DARK_ON:WebSettings.FORCE_DARK_OFF);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            webView.getSettings().setAlgorithmicDarkeningAllowed(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean("dark",false));
        }
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        webView.setScrollbarFadingEnabled(false);
        code = readAsset("trainer.js");
        webView.loadUrl("https://www.google.com");
        initialiseTestMode();
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loader.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                editText.setText(url);
                if(!record)pauseRecording();
                else resumeRecording();
                webView.evaluateJavascript(code, s -> {});
                loadedUrl = url;
                super.onPageFinished(view, url);
                loader.setVisibility(View.GONE);
            }
        });
        editText.setOnClickListener(v -> {
            btn.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_baseline_send_24));
            btn.setOnClickListener((view)->{
                if(editText.getText().toString().isEmpty())return;
                webView.loadUrl(editText.getText().toString());
            });
        });
        back.setOnClickListener(v -> {
            if(webView.canGoBack())webView.goBack();
            else super.onBackPressed();
        });
        if(Configuration.ORIENTATION_LANDSCAPE==getResources().getConfiguration().orientation){
            data.setLandscape(true);
            Snackbar snackbar = Snackbar.make(webView,"Start in desktop mode?",Snackbar.LENGTH_SHORT);
            snackbar.addCallback(new Snackbar.Callback(){
                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);
                    snackbar.setAction("Yes", v -> {
                        String newUA= "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.51 Safari/537.36";
                        webView.getSettings().setUserAgentString(newUA);
                        webView.getSettings().setLoadWithOverviewMode(false);
                        webView.reload();
                        data.setDesktopMode(true);
                    });
                }
            });
            snackbar.show();
        }
    }
    public void initialiseTestMode(){
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if(!record)return super.onConsoleMessage(consoleMessage);
                String message = consoleMessage.message();
                String[] split = message.split("-->");
                switch (split[0]) {
                    case "click":
                        data.jsActions.add(new JsAction(loadedUrl, JsAction.ActionType.click, split[1], split.length > 2 ? split[2] : ""));
                        break;
                    case "change":
                        data.jsActions.add(new JsAction(loadedUrl, JsAction.ActionType.change, split[1], split.length > 2 ? split[2] : ""));
                        break;
                }
                loadedUrl = webView.getUrl();
                return super.onConsoleMessage(consoleMessage);
            }
        });
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
    public void pauseRecording(){
        record = false;
        btn.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_play_circle_outline_24));
        btn.setOnClickListener((v)->{
            Toast.makeText(getApplicationContext(),"Recording.",Toast.LENGTH_SHORT).show();
            resumeRecording();
        });
    }
    public void resumeRecording(){
        record=true;
        btn.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_baseline_pause_circle_outline_24));
        btn.setOnClickListener((v)->{
            Toast.makeText(getApplicationContext(),"Recording paused.",Toast.LENGTH_SHORT).show();
            pauseRecording();
        });
        btn.setOnLongClickListener(v -> {
            data.jsActions.add(new JsAction(webView.getUrl(),JsAction.ActionType.pause));
            pauseRecording();
            Toast.makeText(getApplicationContext(),"Added pause action to recording",Toast.LENGTH_SHORT).show();
            return true;
        });
        btn2.setOnClickListener(v -> {
            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            EditText name = new EditText(getApplicationContext());
            name.setHint("Automation name");
            name.setHintTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_secondary_variant));
            name.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_secondary_variant));
            EditText time = new EditText(getApplicationContext());
            time.setHint("Delay between actions in ms (250+)"); //250ms is the minimum delay between actions
            time.setHintTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_secondary_variant));
            time.setTextColor(getResources().getColor(com.google.android.material.R.color.design_default_color_secondary_variant));
            time.setInputType(InputType.TYPE_CLASS_NUMBER);
            linearLayout.addView(name);
            linearLayout.addView(time);
            linearLayout.setPadding(25,10,25,10);
            new AlertDialog.Builder(TrainerActivity.this)
                    .setTitle("Save these actions?")
                    .setView(linearLayout)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if(name.getText().toString().isEmpty())return;
                        try{
                            data.setName(name.getText().toString());
                            data.setDelay(Integer.parseInt(time.getText().toString()));
                            data.jsActions.add(new JsAction(webView.getUrl(),JsAction.ActionType.pause));
                            ArrayList<AutomationData> allData = getList();
                            optimise(data);
                            System.out.println(AutomationData.toJson(data));
                            allData.add(data);
                            saveList(allData);
                            super.onBackPressed();
                        }catch (Exception e){
                            e.printStackTrace(System.out);
                            Toast.makeText(getApplicationContext(),"Couldn't save",Toast.LENGTH_LONG).show();
                        }
                    }).setNegativeButton("No", (dialog, which) -> {

                    }).show();
        });
    }
    public void saveList(ArrayList<AutomationData> list) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
            String data = mapper.writeValueAsString(list);
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("actions", data).apply();
    }
    public ArrayList<AutomationData> getList() throws JsonProcessingException {
        String data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("actions","[]");
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(data, new TypeReference<ArrayList<AutomationData>>(){});
    }

    @Override
    public void onBackPressed() {
        if(webView.canGoBack())webView.goBack();
        else super.onBackPressed();
    }
}
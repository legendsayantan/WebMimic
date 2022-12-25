package com.legendsayantan.autoweb.activities;
import static com.legendsayantan.autoweb.interfaces.AutomationData.optimise;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
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
import com.legendsayantan.autoweb.workers.WebDriver;

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
    String windowParams;
    ConstraintLayout loader;
    WebDriver driver;
    Snackbar scrollsaver;
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
        driver = new WebDriver(this,webView);
        code = readAsset("trainer.js");
        webView.loadUrl("https://www.google.com");
        initialiseTrainer();
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

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                editText.setText(url);
                webView.evaluateJavascript(code, s -> {});
                loadedUrl = url;
                super.doUpdateVisitedHistory(view, url, isReload);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if(!record)return;
                windowParams = scrollX+"-"+scrollY+"-"+webView.getScale();
                initialiseScrollSaver(webView);
            });

        }
        editText.setOnClickListener(v -> {
            btn.setImageDrawable(AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_baseline_send_24));
            btn.setOnClickListener((view)->{
                if(editText.getText().toString().isEmpty())return;
                webView.loadUrl(editText.getText().toString());
            });
        });
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                        if(editText.getText().toString().isEmpty())return true;
                        webView.loadUrl(editText.getText().toString());
                        return true;
                    default:
                        break;
                }
            }
            return false;
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        driver.createMenu(menu);
    }

    public void initialiseTrainer(){
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if(!record)return super.onConsoleMessage(consoleMessage);
                String message = consoleMessage.message();
                String[] split = message.split("-->");
                switch (split[0]) {
                    case "click":
                        data.jsActions.add(new JsAction(loadedUrl, JsAction.ActionType.click,split.length > 1 ?split[1]:"", split.length > 2 ? split[2] : ""));
                        break;
                    case "change":
                        data.jsActions.add(new JsAction(loadedUrl, JsAction.ActionType.change,split.length > 1 ?split[1]:"", split.length > 2 ? split[2] : ""));
                        break;
                }
                loadedUrl = webView.getUrl();
                removeScrollSaver();
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
            time.setHint("Delay between actions in ms (249+)"); //250ms is the minimum delay between actions
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
                            data.setDelay(Integer.parseInt(time.getText().toString().isEmpty()? "0" : time.getText().toString()));
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
    public void initialiseScrollSaver(WebView webView){
        if(scrollsaver!=null && scrollsaver.isShown())return;
        scrollsaver = Snackbar.make(webView,"Save window position?",Snackbar.LENGTH_INDEFINITE);
        scrollsaver.setAction("Save", v -> {
            data.jsActions.add(new JsAction(webView.getUrl(),JsAction.ActionType.scroll,null,windowParams));
            scrollsaver.dismiss();
        });
        scrollsaver.show();
    }
    public void removeScrollSaver(){
        if(scrollsaver!=null && scrollsaver.isShown())scrollsaver.dismiss();
    }
}
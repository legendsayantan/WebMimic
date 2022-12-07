package com.legendsayantan.autoweb.workers;

import static com.legendsayantan.autoweb.interfaces.JsAction.ActionType.pause;

import android.app.Activity;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.legendsayantan.autoweb.interfaces.AutomationData;
import com.legendsayantan.autoweb.interfaces.JsAction;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author legendsayantan
 */
public class ScriptRunner {
    Timer worker = null;
    private int pausedIndex = 0;
    ArrayList<JsAction> jsActions ;
    long delay = 100;

    public ScriptRunner(ArrayList<JsAction> jsActions) {
        this.jsActions = jsActions;
    }
    public ScriptRunner(AutomationData automationData) {
        this.jsActions = automationData.jsActions;
        this.delay = automationData.getDelay();
    }
    public ScriptRunner(){}

    public void executeOn(WebView webView, String code,Runnable pauseCallBack, Runnable resumeCallback, Activity activity) {
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {}
            @Override
            public void onPageFinished(WebView view, String url) {}
        });
        webView.evaluateJavascript(code, s -> {
            processExecution(webView,code,pauseCallBack, resumeCallback, activity);
        });
    }
    public void processExecution(WebView webView,String code,Runnable pauseCallBack, Runnable resumeCallback, Activity activity){
        worker = new Timer();
        worker.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(pausedIndex>=jsActions.size()-1){
                    activity.runOnUiThread(()->Toast.makeText(activity, "Execution Complete", Toast.LENGTH_SHORT).show());
                    resetExecution();
                    worker.cancel();
                }else {
                    activity.runOnUiThread(() -> {
                        JsAction jsAction = jsActions.get(pausedIndex);
                        if (!webView.getUrl().equals(jsAction.getUrl())) {
                            worker.cancel();
                            webView.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                    super.onPageStarted(view, url, favicon);
                                    pauseCallBack.run();
                                }
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    resumeCallback.run();
                                    if(pausedIndex<jsActions.size()-1) {
                                        webView.evaluateJavascript(code, s -> {
                                            processExecution(webView,code,pauseCallBack, resumeCallback, activity);
                                        });
                                    }
                                }
                            });
                            webView.loadUrl(jsAction.getUrl());
                        }else{
                            webView.setWebViewClient(new WebViewClient(){
                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {}
                                @Override
                                public void onPageFinished(WebView view, String url) {}}
                            );
                            if (jsAction.actionType == pause) {
                                activity.runOnUiThread(()->Toast.makeText(activity, "Execution paused.", Toast.LENGTH_SHORT).show());
                                pause();
                            }else webView.evaluateJavascript(JsAction.getJs(jsAction), null);
                            pausedIndex++;
                        }
                    });
                }
            }
        }, delay, delay);
    }
    public void resetExecution(){
        pausedIndex = 0;
    }
    public void pause(){
        if(worker!=null)worker.cancel();
    }
}

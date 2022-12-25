package com.legendsayantan.autoweb.workers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.legendsayantan.autoweb.BuildConfig;

/**
 * @author legendsayantan
 */
public class WebDriver {
    Activity activity;
    private WebView webView;
    public WebDriver(Activity activity , WebView webView) {
        this.activity = activity;
        this.webView = webView;
        initialise();
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void initialise(){
        webView.getSettings().setSupportZoom(true);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            activity.startActivity(i);
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webView.getSettings().setForceDark(
                    PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext())
                            .getBoolean("dark",false)?
                            WebSettings.FORCE_DARK_ON:WebSettings.FORCE_DARK_OFF);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            webView.getSettings().setAlgorithmicDarkeningAllowed(PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext())
                    .getBoolean("dark",false));
        }
        activity.registerForContextMenu(webView);
    }
    public void createMenu(ContextMenu menu){
        final WebView.HitTestResult result = webView.getHitTestResult();

        // If user long press on url
        if (result.getType() == WebView.HitTestResult.ANCHOR_TYPE ||
                result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {

            // Set the title for context menu
            menu.setHeaderTitle("\t\t\t\t\t\t\t\t\t\t ◦ ◉ ⦿ Select Option ⦿ ◉ ◦ \t");

            // Add an item to the menu
            menu.add(0, 1, 0, " \t \t➤\t Copy URL")
                    .setOnMenuItemClickListener(menuItem -> {
                        String Pressed_url = result.getExtra();
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Url", Pressed_url);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(activity, "URL Copied.", Toast.LENGTH_SHORT).show();
                        return false;
                    });
            menu.add(0, 2, 0, " \t \t➤\t Open in Browser")
                    .setOnMenuItemClickListener(menuItem -> {
                        String Pressed_url = result.getExtra();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Pressed_url));
                        activity.startActivity(browserIntent);
                        return false;
                    });

        }
    }
}

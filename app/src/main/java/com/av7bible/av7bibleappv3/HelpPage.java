package com.av7bible.av7bibleappv3;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;


public class HelpPage extends Activity {

    private WebView webView;
    String helpPageURL = "file:///android_asset/NT/helpPage.htm";



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        JavaScriptInterface JSInterface = new JavaScriptInterface(this);

        webView.addJavascriptInterface(JSInterface, "JSInterface");



        webView.loadUrl(helpPageURL);

    }


    public class JavaScriptInterface {
        Context context;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            context = c;
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void showToast()
        {
            Toast.makeText(context, "Works!", Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goBack() {
            //Toast.makeText(context, "New Thing!", Toast.LENGTH_SHORT).show();
            webView.post(new Runnable() {
                public void run() {
                    finish();
                    // getActivity().onBackPressed();
                }
            });
        }

    }





}





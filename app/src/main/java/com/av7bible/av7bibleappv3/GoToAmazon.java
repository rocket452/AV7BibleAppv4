package com.av7bible.av7bibleappv3;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.av7bible.av7bibleappv3.R;

public class GoToAmazon extends Activity {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);


        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://www.amazon.com/Authorized-Version-Bible-Present-day-English/dp/1597894486");

    }


}
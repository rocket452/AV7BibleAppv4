package com.av7bible.av7bibleappv3;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by Brady on 5/29/2015.
 */
public class JavaScriptInterface {
    Context context;

    /** Instantiate the interface and set the context */
    JavaScriptInterface(Context c) {
        context = c;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public void showToast()
    {
        Toast.makeText(context,"Works!" ,Toast.LENGTH_SHORT).show();
    }

}

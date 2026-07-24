package com.av7bible.av7bibleappv3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;


public class BibleContent extends Activity {

    private WebView webView;
    CharSequence currentSelectedFont;
    SharedPreferences savedSettings;
    String bookNameFromExtras = "";
    String selectedChapterFromExtras = "";
    String nextChapterCounter = "";
    String previousChapterCounter = "";
    String searchStringFromExtras = "";

    SQLiteDatabase myDataBase;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);

        savedSettings = this.getSharedPreferences("AV7BibleAppPreferences", Context.MODE_PRIVATE);
        currentSelectedFont = savedSettings.getString("savedFont", "14");

        JavaScriptInterface JSInterface = new JavaScriptInterface(this);
        webView.addJavascriptInterface(JSInterface, "JSInterface");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bookNameFromExtras = extras.getString("BookName");
            selectedChapterFromExtras = extras.getString("SelectedChapter");
            if (bookNameFromExtras == null) bookNameFromExtras = "";
            if (selectedChapterFromExtras == null) selectedChapterFromExtras = "1";
            nextChapterCounter = selectedChapterFromExtras;
            previousChapterCounter = selectedChapterFromExtras;
            searchStringFromExtras = extras.getString("SearchString");
            if (searchStringFromExtras == null) searchStringFromExtras = "";
        }

        String urlToBeLoaded;
        if ("0".equals(selectedChapterFromExtras)) {
            urlToBeLoaded = "file:///android_asset/NT/bookSummary.htm";
        } else {
            urlToBeLoaded = "file:///android_asset/NT/templatePage.htm";
        }

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(webView, url);
                BuildPage();
                if (android.os.Build.VERSION.SDK_INT >= 19) {
                    view.evaluateJavascript("var links = document.getElementsByTagName('a'); console.log('LINK_COUNT: ' + links.length); for(var i=0; i<links.length; i++) { var text = links[i].innerText.toUpperCase().trim(); if(text == 'WHY' || text == 'KEYS' || text == '1') { var rect = links[i].getBoundingClientRect(); console.log('LINK_RECT_' + text + ': ' + rect.left + ',' + rect.top + ',' + rect.width + ',' + rect.height); } }", null);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), "Error: " + description, Toast.LENGTH_SHORT).show();
            }
        });

        webView.loadUrl(urlToBeLoaded);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    void BuildPage() {
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.evaluateJavascript("console.log('BUILD_PAGE_CALLED');", null);
                }
            });
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String book = bookNameFromExtras;
                String chapter = selectedChapterFromExtras;
                
                if (chapter.length() == 1) {
                    chapter = "0" + chapter;
                }

                final Cursor resultSet;
                if ("VSQ".equals(book)) {
                    resultSet = Utility.getSearchResults(searchStringFromExtras, "", BibleContent.this);
                } else {
                    resultSet = getBibleText(book, chapter);
                }

                if (resultSet != null) {
                    InsertBibleTxt(resultSet);
                }
            }
        }).start();
    }

    protected void InsertBibleTxt(final Cursor resultSet) {
        final StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append("insertPreviousChapterAnchor();");

        while (resultSet.moveToNext()) {
            String bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
            String chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
            String verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
            String textResult = resultSet.getString(resultSet.getColumnIndex("Text"));

            if ("00".equals(chapterResult)) {
                if ("1".equals(verseResult)) {
                    scriptBuilder.append("insertHeader(").append(JSONObject.quote(textResult)).append(");");
                    Cursor chaptersCursor = getNumberOfChapters(bookResult);
                    if (chaptersCursor.moveToFirst()) {
                        String tableHtml = generateChapterSelectTableHTML(bookResult, chaptersCursor.getInt(0));
                        if ("1JO".equals(bookResult)) {
                            tableHtml = get123JohnCustomChapterSelectTable();
                        }
                        scriptBuilder.append("insertTable(").append(JSONObject.quote(tableHtml)).append(");");
                    }
                    chaptersCursor.close();
                } else {
                    scriptBuilder.append("insertBody(").append(JSONObject.quote(textResult)).append(");");
                }
            } else if ("-1".equals(chapterResult) || "-2".equals(chapterResult)) {
                scriptBuilder.append("insertBody(").append(JSONObject.quote("<p>" + textResult + "</p>")).append(");");
            } else {
                String combined = "<span id=\"verseNumber\">" + verseResult + "</span>" + textResult;
                scriptBuilder.append("insertFunction(").append(JSONObject.quote(combined)).append(");");
            }
        }
        scriptBuilder.append("adjustFont(").append(JSONObject.quote(currentSelectedFont.toString())).append(");");
        resultSet.close();

        final String finalScript = scriptBuilder.toString();
        webView.post(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= 19) {
                    webView.evaluateJavascript(finalScript, null);
                    webView.evaluateJavascript("console.log('BANNER_HEIGHT: ' + document.getElementById('logoTable').offsetHeight);", null);
                    webView.evaluateJavascript("var h1 = document.getElementsByTagName('h1')[0]; if(h1) console.log('TITLE_TOP: ' + h1.getBoundingClientRect().top);", null);
                    webView.evaluateJavascript("setTimeout(function() { var links = document.getElementsByTagName('a'); console.log('LINK_COUNT_POST: ' + links.length); for(var i=0; i<links.length; i++) { var text = links[i].innerText.toUpperCase().trim(); var rect = links[i].getBoundingClientRect(); console.log('LINK_RECT_' + text + ': ' + rect.left + ',' + rect.top + ',' + rect.width + ',' + rect.height); } }, 500);", null);
                } else {
                    webView.loadUrl("javascript:" + finalScript);
                }
            }
        });
    }

    protected void InsertBibleTxtBefore(final Cursor resultSet) {
        final StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder.append("insertBeforeFunction('<br><br>');");
        scriptBuilder.append("insertBeforePreviousChapterAnchor();");
        
        resultSet.moveToLast();
        while (resultSet.moveToPrevious()) {
            String chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
            String verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
            String textResult = resultSet.getString(resultSet.getColumnIndex("Text"));

            String combined;
            if ("-2".equals(chapterResult) || "-1".equals(chapterResult)) {
                combined = textResult;
            } else {
                combined = "<span id=\"verseNumber\">" + verseResult + "</span>" + textResult;
            }
            scriptBuilder.append("insertBeforeFunction(").append(JSONObject.quote(combined)).append(");");
        }
        resultSet.close();

        final String finalScript = scriptBuilder.toString();
        webView.post(new Runnable() {
            @Override
            public void run() {
                if (android.os.Build.VERSION.SDK_INT >= 19) {
                    webView.evaluateJavascript(finalScript, null);
                    webView.evaluateJavascript("console.log('BANNER_HEIGHT: ' + document.getElementById('logoTable').offsetHeight);", null);
                    webView.evaluateJavascript("var h1 = document.getElementsByTagName('h1')[0]; if(h1) console.log('TITLE_TOP: ' + h1.getBoundingClientRect().top);", null);
                    webView.evaluateJavascript("setTimeout(function() { var links = document.getElementsByTagName('a'); console.log('LINK_COUNT_POST: ' + links.length); for(var i=0; i<links.length; i++) { var text = links[i].innerText.toUpperCase().trim(); var rect = links[i].getBoundingClientRect(); console.log('LINK_RECT_' + text + ': ' + rect.left + ',' + rect.top + ',' + rect.width + ',' + rect.height); } }, 500);", null);
                } else {
                    webView.loadUrl("javascript:" + finalScript);
                }
            }
        });
    }

    protected String get123JohnCustomChapterSelectTable() {
        return "<tr>" +
                "<td style=\"width: 35%;\">1st John</td>" +
                "<td ><a onclick=\"goToChapter('1JO',1)\">1</a></td>" +
                "<td ><a onclick=\"goToChapter('1JO',2)\">2</a></td>" +
                "<td ><a onclick=\"goToChapter('1JO',3)\">3</a></td>" +
                "<td ><a onclick=\"goToChapter('1JO',4)\">4</a></td>" +
                "<td ><a onclick=\"goToChapter('1JO',5)\">5</a></td>" +
                "</tr>" +
                "<tr>" +
                "<td >2nd John</td>" +
                "<td ><a onclick=\"goToChapter('2JO',1)\">1</a></td>" +
                "<td colspan=\"4\"></td>" +
                "</tr>" +
                "<tr>" +
                "<td >3rd John</td>" +
                "<td ><a onclick=\"goToChapter('3JO',1)\">1</a></td>" +
                "<td colspan=\"4\"></td>" +
                "</tr>";
    }

    protected Cursor getBibleText(String bookName, String selectedChapter) {
        if (myDataBase == null || !myDataBase.isOpen()) {
            String myPath = getDatabasePath("newDb").getPath();
            myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }
        return myDataBase.rawQuery("Select * from Bible where Book = ? and chapter = ?", new String[]{bookName, selectedChapter});
    }

    protected Cursor getNumberOfChapters(String bookName) {
        if (myDataBase == null || !myDataBase.isOpen()) {
            String myPath = getDatabasePath("newDb").getPath();
            myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }
        return myDataBase.rawQuery("Select Max(chapter+0) from Bible where Book = ?", new String[]{bookName});
    }

    @Override
    protected void onDestroy() {
        if (myDataBase != null && myDataBase.isOpen()) {
            myDataBase.close();
        }
        super.onDestroy();
    }

    public class JavaScriptInterface {
        Context context;
        JavaScriptInterface(Context c) { context = c; }

        @JavascriptInterface
        public void showToast() { Toast.makeText(context, "Works!", Toast.LENGTH_SHORT).show(); }

        @JavascriptInterface
        public void goBack() {
            webView.post(new Runnable() { public void run() { finish(); } });
        }

        @JavascriptInterface
        public void openOptionsMenu() { showHelpPopup(BibleContent.this); }

        @JavascriptInterface
        public void searchForText(String s1, String s2) {
            Utility.retrieveAndDisplaySearchResults(s1, s2, webView, getApplicationContext());
        }

        @JavascriptInterface
        public void goToChapter(String book, String chapter) {
            Intent intent = new Intent(BibleContent.this, BibleContent.class);
            intent.putExtra("BookName", book);
            intent.putExtra("SelectedChapter", chapter);
            startActivity(intent);
        }

        @JavascriptInterface
        public void openHelpPage() {
            startActivity(new Intent(BibleContent.this, HelpPage.class));
        }

        @JavascriptInterface
        public void loadNextChapter(String b, String c) {
            webView.post(new Runnable() {
                public void run() {
                    if ("-1".equals(nextChapterCounter) || "-2".equals(nextChapterCounter)) return;
                    try {
                        int next = Integer.parseInt(nextChapterCounter) + 1;
                        nextChapterCounter = String.valueOf(next);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Cursor rs = getBibleText(bookNameFromExtras, nextChapterCounter);
                                if (rs.getCount() > 0) {
                                    webView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Loading Next Chapter...", Toast.LENGTH_SHORT).show();
                                            webView.loadUrl("javascript:insertFunction('<br><br>')");
                                        }
                                    });
                                    InsertBibleTxt(rs);
                                } else {
                                    rs.close();
                                }
                            }
                        }).start();
                    } catch (Exception e) {}
                }
            });
        }

        @JavascriptInterface
        public void loadPreviousChapter(String b, String c) {
            webView.post(new Runnable() {
                public void run() {
                    try {
                        int prev = Integer.parseInt(previousChapterCounter) - 1;
                        if (prev <= 0) return;
                        previousChapterCounter = String.valueOf(prev);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Cursor rs = getBibleText(bookNameFromExtras, previousChapterCounter);
                                if (rs.getCount() > 0) {
                                    webView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Loading Previous Chapter...", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    InsertBibleTxtBefore(rs);
                                    webView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            webView.loadUrl("javascript:scrollToAnchor()");
                                        }
                                    });
                                } else {
                                    rs.close();
                                }
                            }
                        }).start();
                    } catch (Exception e) {}
                }
            });
        }

        @JavascriptInterface
        public void sendShareEmail() {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_SUBJECT, "Shared AV7 Bible");
            i.putExtra(Intent.EXTRA_TEXT, "Check out the AV7 Bible: http://av7bible.com");
            startActivity(Intent.createChooser(i, "Send mail..."));
        }

        @JavascriptInterface
        public void goToTitleScreen() {
            startActivity(new Intent(BibleContent.this, MainActivity.class));
        }

        @JavascriptInterface
        public void goToTableOfContents() {
            finish();
            startActivity(new Intent(BibleContent.this, TableOfContents.class));
        }

        @JavascriptInterface
        public void clearCachedExtras() {
            getIntent().removeExtra("BookName");
            getIntent().removeExtra("SelectedChapter");
        }
    }

    private void showHelpPopup(final Activity context) {
        View layout = LayoutInflater.from(context).inflate(R.layout.popup_layout, (ViewGroup) findViewById(R.id.popup));
        final PopupWindow popup = new PopupWindow(layout, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, 100, 100);

        final Spinner spinner = (Spinner) layout.findViewById(R.id.fontOptionsSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(BibleContent.this, R.array.font_options_array, R.layout.spinner_properties);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(currentSelectedFont));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSelectedFont = parent.getItemAtPosition(position).toString();
                savedSettings.edit().putString("savedFont", currentSelectedFont.toString()).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        layout.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:adjustFont('" + spinner.getSelectedItem() + "')");
                    }
                });
                popup.dismiss();
            }
        });
    }

    String generateChapterSelectTableHTML(String bookName, int count) {
        StringBuilder sb = new StringBuilder();
        int i = 1, j = 1;
        while (i <= count) {
            if (j == 1) sb.append("<tr>");
            if (i == 1 && hasWhyKeys(bookName)) {
                sb.append("<td class=\"whyPage\"><a onclick=\"goToChapter('").append(bookName).append("', -2)\">Why</a></td>");
                sb.append("<td class=\"keysPage\"><a onclick=\"goToChapter('").append(bookName).append("', -1)\">Keys</a></td>");
                j += 2;
            }
            sb.append("<td><a onclick=\"goToChapter('").append(bookName).append("', ").append(i).append(")\">").append(i).append("</a></td>");
            if (j >= 7) { sb.append("</tr>"); j = 0; }
            i++; j++;
        }
        if (!sb.toString().endsWith("</tr>")) sb.append("</tr>");
        return sb.toString();
    }

    private boolean hasWhyKeys(String bookName) {
        if (myDataBase == null || !myDataBase.isOpen()) {
            String myPath = getDatabasePath("newDb").getPath();
            myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }
        Cursor cursor = myDataBase.rawQuery("SELECT EXISTS(SELECT 1 FROM bible WHERE book=? and chapter = '-2' LIMIT 1);", new String[]{bookName});
        cursor.moveToFirst();
        boolean exists = cursor.getInt(0) != 0;
        cursor.close();
        return exists;
    }
}

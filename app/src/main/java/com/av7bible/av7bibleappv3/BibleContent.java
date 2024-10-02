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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;


public class BibleContent extends Activity {

    private WebView webView;
    CharSequence currentSelectedFont;
    SharedPreferences savedSettings;
    Bundle extras = null;
    String bookNameFromExtras = "";
    String selectedChapterFromExtras = "";
    String nextChapterCounter = "";
    String previousChapterCounter = "";
    String searchStringFromExtras = "";

    //The Android's default system path of your application database.
    String DB_PATH = "/data/data/com.av7bible.av7bibleappv2/databases/";
    String DB_NAME = "newDb";
    SQLiteDatabase myDataBase;
    String myPath = DB_PATH + DB_NAME;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        // Restore preferences from font value saved on device
        savedSettings = this.getSharedPreferences("AV7BibleAppPreferences", Context.MODE_PRIVATE);
        currentSelectedFont = savedSettings.getString("savedFont", "12");
        JavaScriptInterface JSInterface = new JavaScriptInterface(this);

        webView.addJavascriptInterface(JSInterface, "JSInterface");

        //Get selected chapter passed here from TableOfContents.java through the chapter picker
        extras = getIntent().getExtras();

        if (extras != null) {
            bookNameFromExtras = extras.getString("BookName");
            selectedChapterFromExtras = extras.getString("SelectedChapter");
            nextChapterCounter = selectedChapterFromExtras;
            previousChapterCounter = selectedChapterFromExtras;
            searchStringFromExtras = extras.getString("SearchString");
        }

        String urlToBeLoaded;

        if (selectedChapterFromExtras.equals("0")) {
            urlToBeLoaded = "file:///android_asset/NT/bookSummary.htm";
        } else {
            urlToBeLoaded = "file:///android_asset/NT/templatePage.htm";
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                super.onPageFinished(webView, url);

                BuildPage("", "");

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        webView.loadUrl(urlToBeLoaded);
    }

    void BuildPage(String bookNameParam, String chapterNumberParam) {

        //Get selected chapter passed here from TableOfContents.java through the chapter picker
        Bundle extras = getIntent().getExtras();
        String bookName = "";
        String selectedChapter = "";
        String searchString = "";

        if (bookNameParam != "") {
            bookName = bookNameParam;
            selectedChapter = chapterNumberParam;
        } else {
            if (extras != null) {
                bookName = extras.getString("BookName");
                selectedChapter = extras.getString("SelectedChapter");
            }
        }

        //single digit chapters are represented as "01" not "1" so they must be appended
        if (selectedChapter.length() == 1) {

            selectedChapter = "0" + selectedChapter;
        }
        Cursor resultSet = null;

        if (bookName == "VSQ" && selectedChapter == "") { //Search Page
            resultSet = getSearchResults(searchString, "");
        } else {
            resultSet = getBibleText(bookName, selectedChapter);
        }

        InsertBibleTxt(resultSet);

        resultSet.close();

    }


    protected void InsertBibleTxt(Cursor resultSet) {

        String bookResult = "";
        String chapterResult = "";
        String verseResult = "";
        String textResult = "";
        String verseNumber = "";
        String combinedResult = "";

        webView.loadUrl("javascript:insertPreviousChapterAnchor()");

        while (resultSet.moveToNext()) {

            bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
            chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
            verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
            textResult = resultSet.getString(resultSet.getColumnIndex("Text"));

            textResult = textResult.replaceAll("'", "\\\\'");
            textResult = textResult.replaceAll(",", "\\\\,");

            verseNumber = verseResult;
            verseResult = "<span id=\"verseNumber\">" + verseResult + "</span>";
            combinedResult = verseResult + textResult;

            if (chapterResult.equals("00")) {

                if (verseNumber.equals("1")) {

                    webView.loadUrl("javascript:insertHeader('" + textResult + "')");

                    Cursor numberOfChaptersCursor = getNumberOfChapters(bookResult);
                    numberOfChaptersCursor.moveToFirst();

                    textResult = generateChapterSelectTableHTML(bookResult, Integer.parseInt(numberOfChaptersCursor.getString(0)));

                    //1,2,3 John has a special table
                    if (bookResult.equals("1JO")) {
                        textResult = get123JohnCustomChapterSelectTable();
                    }

                    webView.loadUrl("javascript:insertTable('" + textResult + "')");
                } else {
                    webView.loadUrl("javascript:insertBody('" + textResult + "')");
                }

            } else if (chapterResult.equals("-1") || chapterResult.equals("-2")) {
                webView.loadUrl("javascript:insertBody('<p>" + textResult + "</p>')");
            } else {
                webView.loadUrl("javascript:insertFunction('" + combinedResult + "')");
            }

            //Set font size to previously used font
            webView.loadUrl("javascript:adjustFont('" + currentSelectedFont + "')");

        }

    }

    protected void InsertBibleTxtBefore(Cursor resultSet) {

        String bookResult = "";
        String chapterResult = "";
        String verseResult = "";
        String textResult = "";
        String verseNumber = "";
        String combinedResult = "";

        webView.loadUrl("javascript:insertBeforeFunction('<br><br>')");
        webView.loadUrl("javascript:insertBeforePreviousChapterAnchor()");
        resultSet.moveToLast();
        while (resultSet.moveToPrevious()) {

            bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
            chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
            verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
            textResult = resultSet.getString(resultSet.getColumnIndex("Text"));
            verseNumber = verseResult;
            verseResult = "<span id=\"verseNumber\">" + verseResult + "</span>";
            combinedResult = verseResult + textResult;

            if (chapterResult.equals("00")) {

                if (verseNumber.equals("1")) {

                    webView.loadUrl("javascript:insertHeader('" + textResult + "')");

                    Cursor numberOfChaptersCursor = getNumberOfChapters(bookResult);
                    numberOfChaptersCursor.moveToFirst();

                    textResult = generateChapterSelectTableHTML(bookResult, Integer.parseInt(numberOfChaptersCursor.getString(0)));

                    //1,2,3 John has a special table
                    if (bookResult.equals("1JO")) {
                        textResult = get123JohnCustomChapterSelectTable();
                    }

                    webView.loadUrl("javascript:insertTable('" + textResult + "')");

                } else {
                    webView.loadUrl("javascript:insertBody('" + textResult + "')");
                }

            } else {
                webView.loadUrl("javascript:insertBeforeFunction('" + combinedResult + "')");
            }

        }

    }

    protected String get123JohnCustomChapterSelectTable() {

        return "<tr>" +
                "<td style=\"width: 35%;\">1st John</td>" +
                "<td ><a onclick=\"goToChapter(\\'1JO\\',1)\">1</a></td>" +
                "<td ><a onclick=\"goToChapter(\\'1JO\\',2)\">2</a></td>" +
                "<td ><a onclick=\"goToChapter(\\'1JO\\',3)\">3</a></td>" +
                "<td ><a onclick=\"goToChapter(\\'1JO\\',4)\">4</a></td>" +
                "<td ><a onclick=\"goToChapter(\\'1JO\\',5)\">5</a></td>" +
                "</tr>" +
                "<tr>" +
                "<td >2nd John</td>" +
                "<td ><a onclick=\"goToChapter(\\'2JO\\',1)\">1</a></td>" +
                "<td colspan=\"4\">" +
                "</td>" +
                "</tr>" +
                "<tr>" +
                "<td >3rd John</td>" +
                "<td ><a onclick=\"goToChapter(\\'3JO\\',1)\">1</a></td>" +
                "<td colspan=\"4\">" +
                "</td>" +
                "</tr>";
    }

    protected Cursor getBibleText(String bookName, String selectedChapter) {

        //The Android's default system path of your application database.
        String DB_PATH = "/data/data/com.av7bible.av7bibleappv2/databases/";

        String DB_NAME = "newDb";

        SQLiteDatabase myDataBase;

        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        if (selectedChapter.length() == 1) selectedChapter = "0" + selectedChapter;

        Cursor resultSet = myDataBase.rawQuery("Select * from Bible where Book = \"" + bookName + "\" and chapter = \"" + selectedChapter + "\"", null);
        return resultSet;
    }

    protected Cursor getNumberOfChapters(String bookName) {

        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        Cursor numberOfChapters = myDataBase.rawQuery("Select Max(chapter+0) from Bible where  Book = \"" + bookName + "\"", null);

        return numberOfChapters;
    }

    public class JavaScriptInterface {
        Context context;

        /**
         * Instantiate the interface and set the context
         */
        JavaScriptInterface(Context c) {
            context = c;
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void showToast() {
            Toast.makeText(context, "Works!", Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goBack() {
            webView.post(new Runnable() {
                public void run() {
                    finish();
                }
            });
        }


        @JavascriptInterface   // must be added for API 17 or higher
        public void openOptionsMenu() {
            showHelpPopup(BibleContent.this);
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goToChapter(String bookName, String chapterNumber) {

            Intent intent = new Intent(BibleContent.this, BibleContent.class);

            intent.putExtra("BookName", bookName);
            intent.putExtra("SelectedChapter", chapterNumber);

            startActivity(intent);
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void openHelpPage() {

            Intent intent = new Intent(BibleContent.this, HelpPage.class);

            startActivity(intent);
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void loadNextChapter(final String bookName, final String chapterNumber) {
            webView.post(new Runnable() {
                public void run() {

                    if (nextChapterCounter.equals("-1") || nextChapterCounter.equals("-2")) {
                        return;
                    }

                    nextChapterCounter = Integer.toString(Integer.parseInt(nextChapterCounter) + 1);

                    Cursor resultSet = getBibleText(extras.getString("BookName"), nextChapterCounter);

                    if (resultSet.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Swipe Up to See Next Chapter", Toast.LENGTH_LONG).show();
                        webView.loadUrl("javascript:insertFunction('<br><br>')");
                    }

                    InsertBibleTxt(resultSet);

                }
            });
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void loadPreviousChapter(final String bookName, final String chapterNumber) {
            webView.post(new Runnable() {
                public void run() {

                    previousChapterCounter = Integer.toString(Integer.parseInt(previousChapterCounter) - 1);

                    Cursor resultSet = getBibleText(extras.getString("BookName"), previousChapterCounter);

                    if (resultSet.getCount() > 0 && Integer.parseInt(previousChapterCounter) > 0) {
                        Toast.makeText(getApplicationContext(), "Swipe Up to See Previous Chapter", Toast.LENGTH_LONG).show();
                        InsertBibleTxtBefore(resultSet);
                        webView.loadUrl("javascript:scrollToAnchor()");
                    }

                }
            });
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void sendShareEmail() {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_SUBJECT, "'Enter Your Name' has " +
                    "shared 'How to Compare Bible Versions' with you!"
            );

            i.putExtra(Intent.EXTRA_TEXT,
                    "'Enter Your Name' has invited you to click this link" +
                            " to download a free copy of the book, 'How " +
                            " http://www.vsiq.com/avx/compare.php " +
                            "to Compare Bible Versions'"
            );

            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getApplicationContext(), "NO email client installed", Toast.LENGTH_SHORT).show();
            }


        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goToTitleScreen() {
            Intent intent = new Intent(BibleContent.this, MainActivity.class);
            startActivity(intent);
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goToTableOfContents() {
            Intent intent = new Intent(BibleContent.this, TableOfContents.class);
            startActivity(intent);
        }


        @JavascriptInterface
        public void clearCachedExtras() {
            getIntent().removeExtra("BookName");
            getIntent().removeExtra("SelectedChapter");
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void searchForText(final String searchString1, final String searchString2) {

            //Toast.makeText(context, "SearchForText!", Toast.LENGTH_LONG).show();
            if (!searchString1.isEmpty() || !searchString2.isEmpty()) {
                webView.post(new Runnable() {
                    public void run() {
                        Cursor resultSet = getSearchResults(searchString1, searchString2);

                        String bookResult;
                        String chapterResult;
                        String verseResult;
                        String textResult;
                        String combinedResult;

                        int newTestamentCount = 0;
                        int oldTestamentCount = 0;

                        Log.d("DeubugTag", "Result Count:" + resultSet.getCount());
                        //Get OT/NT Result Counts
                        while (resultSet.moveToNext()) {
                            int BookOrder = resultSet.getInt(resultSet.getColumnIndex("BookOrder"));

                            bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
                            Log.d("Result Book ", bookResult + " BookOrder: " + BookOrder);

                            if (BookOrder < 28) {
                                newTestamentCount++;
                            } else {
                                oldTestamentCount++;
                            }
                        }


                        webView.loadUrl("javascript:insertBody('<p><b>New Testament Results: <span id=\"verseNumber\" style=\"font-size: large;\">" + newTestamentCount + "</span></b></p>')");
                        webView.loadUrl("javascript:insertBody('<p><b>Old Testament Results: <span id=\"verseNumber\" style=\"font-size: large;\">" + oldTestamentCount + "</span></b></p>')");

                        int resultCountLimit = 1000;


                        if (resultSet.getCount() > resultCountLimit) {
                            webView.loadUrl("javascript:insertBody('<p><span id=\"verseNumber\"> Only displaying the first " + resultCountLimit + " results</span></p>')");
                        }

                        int i = 0;

                        //Read Text Results
                        resultSet.moveToPosition(-1);
                        while (resultSet.moveToNext() && i < resultCountLimit) {
                            i++;

                            //We want to exclude search results that snagged on something inside the HTML (class name, id name etc)
                            String resultWithoutHTML = android.text.Html.fromHtml(resultSet.getString(resultSet.getColumnIndex("Text"))).toString();
                            if (!resultWithoutHTML.toUpperCase().contains(searchString1.toUpperCase()) || !resultWithoutHTML.toUpperCase().contains(searchString2.toUpperCase())) {
                                continue;
                            }


                            bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
                            chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
                            if (chapterResult.substring(0, 1).equals("0")) {
                                chapterResult = chapterResult.substring(1);
                            }
                            verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
                            textResult = resultSet.getString(resultSet.getColumnIndex("Text"));

                            combinedResult = "<span id=\"verseNumber\">" + bookResult + " " + chapterResult + ":" + verseResult + "</span>" + textResult;


                            combinedResult = combinedResult.replace("=\'", "=\"");
                            combinedResult = combinedResult.replace("'>", "\">");
                            combinedResult = combinedResult.replace("'", "&quot;");

                            // combinedResult =  combinedResult.substring(1, combinedResult.length()-1);

                            Log.d("InsertText", combinedResult);
                            // textResult = "water";
                            webView.loadUrl("javascript:insertBody('<p>" + combinedResult + "</p>')");


                        }

                        //webView.loadUrl("javascript:insertNTText('<p>testing</p>')");

                        //hide keyboard
                        View view = webView;
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                        resultSet.close();
                    }
                });
            }

        }


        @JavascriptInterface   // must be added for API 17 or higher
        public void sendEmailJSInterface() {
            Toast.makeText(context, "sendEmailJSInterface!", Toast.LENGTH_LONG).show();

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("application/image");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"testemail"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Test Subject");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "From My App");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///mnt/sdcard/Myimage.jpeg"));
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        }

    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }


    // The method that displays the popup.
    private void showHelpPopup(final Activity context) {

        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);

        LayoutInflater layoutInflater;

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup); //

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.setAnimationStyle(R.style.Animation);

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, 100, 100);

        final Spinner spinner = (Spinner) layout.findViewById(R.id.fontOptionsSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(BibleContent.this,
                R.array.font_options_array, R.layout.spinner_properties);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        // Apply the adapter to the spinner


        spinner.setAdapter(adapter);
        //End Spinner
        spinner.setSelection(adapter.getPosition(currentSelectedFont));
        spinner.setOnItemSelectedListener(new SpinnerActivity());
        Button close = (Button) layout.findViewById(R.id.close);

        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("TextResult", "Selected Font: " + spinner.getSelectedItem());

                        webView.loadUrl("javascript:adjustFont('" + spinner.getSelectedItem() + "')");
                    }
                });
                popup.dismiss();
            }
        });


    }

    String generateChapterSelectTableHTML(String bookName, int numberOfChapters) {

        StringBuilder sb = new StringBuilder();
        // adds 9 character string at beginning

        Log.i("TextResult", Integer.toString(numberOfChapters));
        int i = 1;
        int j = 1;

        while (i <= numberOfChapters) {

            if (j == 1) sb.append("<tr>");

            //not all books have Why and Keys pages yet so we have to check
            if (i == 1 && hasWhyKeys(bookName)) {
                sb.append("<td class=\"whyPage\" ><a onclick=\"goToChapter(\\'" + bookName + "\\'," + -2 + ")\">Why</a></td>");
                sb.append("<td class=\"keysPage\" ><a onclick=\"goToChapter(\\'" + bookName + "\\'," + -1 + ")\">Keys</a></td>");
                j = j + 2;
            }

            sb.append("<td ><a onclick=\"goToChapter(\\'" + bookName + "\\'," + i + ")\">" + i + "</a></td>");

            if (j == 7) {
                sb.append("</tr>");
                j = 0;
            }
            i++;
            j++;
        }

        //make sure we end with a closing </tr>
        if (sb.toString().endsWith("</tr>") != true) {

            sb.append("</tr>");
        }


        return sb.toString();
    }

    private boolean hasWhyKeys(String bookName) {

        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        Cursor hasWhyKeys = myDataBase.rawQuery("SELECT EXISTS(SELECT 1 FROM bible WHERE book=\"" + bookName + "\" and chapter = '-2' LIMIT 1);", null);

        hasWhyKeys.moveToFirst();

        return Integer.parseInt(hasWhyKeys.getString(0)) != 0;
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using

            currentSelectedFont = parent.getItemAtPosition(pos).toString();

            SharedPreferences.Editor editor = savedSettings.edit();
            editor.putString("savedFont", currentSelectedFont.toString());
            //Toast.makeText(parent.getContext(),	"OnItemSelectedListener : " + currentSelectedFont.toString(),	Toast.LENGTH_SHORT).show();
            // Commit the edits!
            editor.commit();

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }

    }


    protected Cursor getSearchResults(String searchString1, String searchString2) {

        //The Android's default system path of your application database.
        String DB_PATH = "/data/data/com.av7bible.av7bibleappv2/databases/";

        String DB_NAME = "newDb";

        SQLiteDatabase myDataBase;

        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);


        String[] a = new String[5];
        //   a[0]       = "%one's%";
        a[0] = "%" + searchString1 + "%";
        a[1] = "%" + searchString2 + "%";
        a[2] = "00";
        a[3] = "%<p>%";
        a[4] = "";
        // String query = "SELECT * FROM bible WHERE text LIKE ?";
        //  String query = "SELECT * FROM bible WHERE text LIKE ? AND text LIKE ? AND Chapter > ? AND NOT text LIKE ? ORDER BY rowid DESC";
        String query = "SELECT * FROM bible WHERE text LIKE ? AND text LIKE ? AND Chapter > ? AND NOT text LIKE ? AND NOT verse = ? ORDER BY BookOrder";

        Cursor resultSet = myDataBase.rawQuery(query, a);


        return resultSet;
    }


}


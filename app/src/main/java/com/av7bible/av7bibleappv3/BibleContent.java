package com.av7bible.av7bibleappv3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
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


/**
 * Created by Brady on 6/1/2015.
 */
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
        // Toast.makeText(getApplicationContext(), "Current Select Font " + description, Toast.LENGTH_SHORT).show();
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
        //loading wrong file!!

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

        //webView.loadUrl("file:///android_asset/NT/templatePage.htm");
        webView.loadUrl(urlToBeLoaded);

    }

    void BuildPage(String bookNameParam, String chapterNumberParam) {
        //Toast.makeText(getApplicationContext(), "Height: " + webView.getContentHeight(), Toast.LENGTH_LONG).show();

        //Get selected chapter passed here from TableOfContents.java through the chapter picker
        Bundle extras = getIntent().getExtras();
        String bookName = "";
        String selectedChapter = "";
        String searchString = "";

        if (bookNameParam != "") {
            bookName = bookNameParam;
            selectedChapter = chapterNumberParam;
        } else {
            if(extras != null) {
                bookName = extras.getString("BookName");
                selectedChapter = extras.getString("SelectedChapter");
            }
        }


        //single digit chapters are represented as "01" not "1" so they must be appended
        if (selectedChapter.length() == 1) {

            selectedChapter = "0" + selectedChapter;
        }
        Cursor resultSet = null;
        //resultSet = getBibleText(selectedChapter);
        //Toast.makeText(getApplicationContext(), "Book: '" + bookName + "'  Chap: '" + selectedChapter + "'", Toast.LENGTH_LONG).show();
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
        String bookName = "";

        webView.loadUrl("javascript:insertPreviousChapterAnchor()");

        while (resultSet.moveToNext()) {

            bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
            chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
            verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
            textResult = resultSet.getString(resultSet.getColumnIndex("Text"));

            //   if(textResult.contains("'")) {
            textResult = textResult.replaceAll("'", "\\\\'");
            textResult = textResult.replaceAll(",", "\\\\,");
            //    }

            //combinedResult = verseResult + "   " + textResult + "\n";
            verseNumber = verseResult;
            verseResult = "<span id=\"verseNumber\">" + verseResult + "</span>";
            combinedResult = verseResult + textResult;
            //combinedResult = "knight";

            if (chapterResult.equals("00")) {

                //urlToBeLoaded = "file:///android_asset/NT/mat00test.htm";

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
                    // textResult = "slaughter of the infants, and the beginning of Jesus's ministry.";
                    // textResult = "<ul><li>Genealogy and birth 1:1-2:23</li><li>John the Baptist\\'s ministry 3:1-12</li><li>Jesus baptism and temptation 3:13-4:11</li><li>Jesus public ministry in Galilee 4:12-18:35</li><";
                    webView.loadUrl("javascript:insertBody('" + textResult + "')");
                }

            } else if (chapterResult.equals("-1") || chapterResult.equals("-2")) {
                webView.loadUrl("javascript:insertBody('<p>" + textResult + "</p>')");
            } else {
                webView.loadUrl("javascript:insertFunction('" + combinedResult + "')");
            }

            //Set font size to previously used font
            webView.loadUrl("javascript:adjustFont('" + currentSelectedFont + "')");

            //webView.loadUrl("javascript:insertFunction('" + combinedResult + "')");
            //  webView.loadUrl("javascript:insertFunction2('" + verseResult +"','"+ textResult + "')");


            //  webView.loadUrl("javascript:insertFunction('header','table','body')");
            // Log.i("db path", "javascript:insertFunction('" + verseResult + "','" + textResult + "')");
        }

        //Add a little space to the end of each chapter but not needed onthe summary pages
        //if(!chapterResult.equals("00"))   webView.loadUrl("javascript:insertFunction('<br><br>')");

    }

    protected void InsertBibleTxtBefore(Cursor resultSet) {

        String bookResult = "";
        String chapterResult = "";
        String verseResult = "";
        String textResult = "";
        String verseNumber = "";
        String combinedResult = "";
        String bookName = "";

        //We need to get the distance from the top so we can
        // webView.loadUrl("javascript:getDistanceFromTop()");

        //Insert Spacer

        webView.loadUrl("javascript:insertBeforeFunction('<br><br>')");
        webView.loadUrl("javascript:insertBeforePreviousChapterAnchor()");
        resultSet.moveToLast();
        while (resultSet.moveToPrevious()) {

            bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
            chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
            verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
            textResult = resultSet.getString(resultSet.getColumnIndex("Text"));
            //combinedResult = verseResult + "   " + textResult + "\n";
            verseNumber = verseResult;
            verseResult = "<span id=\"verseNumber\">" + verseResult + "</span>";
            combinedResult = verseResult + textResult;
            //combinedResult = "knight";

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
                    // textResult = "slaughter of the infants, and the beginning of Jesus's ministry.";
                    // textResult = "<ul><li>Genealogy and birth 1:1-2:23</li><li>John the Baptist\\'s ministry 3:1-12</li><li>Jesus baptism and temptation 3:13-4:11</li><li>Jesus public ministry in Galilee 4:12-18:35</li><";

                    webView.loadUrl("javascript:insertBody('" + textResult + "')");
                }

            } else {
                webView.loadUrl("javascript:insertBeforeFunction('" + combinedResult + "')");
            }


            //webView.loadUrl("javascript:insertFunction('" + combinedResult + "')");
            //  webView.loadUrl("javascript:insertFunction2('" + verseResult +"','"+ textResult + "')");


            //  webView.loadUrl("javascript:insertFunction('header','table','body')");
            // Log.i("db path", "javascript:insertFunction('" + verseResult + "','" + textResult + "')");
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

    protected Cursor getBibleTextOld(String bookName, String selectedChapter) {

        File dbfile = this.getDatabasePath("mydb");

        if (!dbfile.exists()) {
            Toast.makeText(getApplicationContext(), "No DB!", Toast.LENGTH_SHORT).show();
        }

        SQLiteDatabase mydatabase = openOrCreateDatabase("mydb", MODE_PRIVATE, null);

        //Cursor resultSet = mydatabase.rawQuery("Select * from Bible limit 1",null);
        Cursor resultSet = mydatabase.rawQuery("Select * from Bible where Book = \"" + bookName + "\" and chapter = \"" + selectedChapter + "\"", null);


        String bookResult = "";
        String chapterResult = "";
        String verseResult = "";
        String textResult = "";
/*
        while (resultSet.moveToNext()) {
            bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
            chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
            verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
            textResult = resultSet.getString(resultSet.getColumnIndex("Text"));
            Log.i ("info12345 bookResult",bookResult);            Log.i ("info12345 textResult", textResult);

            Log.i ("info12345 chapterResult",chapterResult );
            Log.i ("info12345 verseResult", verseResult);
        }
        resultSet.close();
        */

        return resultSet;
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
            //Toast.makeText(context, "New Thing!", Toast.LENGTH_SHORT).show();
            webView.post(new Runnable() {
                public void run() {
                    finish();
                    // getActivity().onBackPressed();
                }
            });
        }


        @JavascriptInterface   // must be added for API 17 or higher
        public void openOptionsMenu() {
            //  Toast.makeText(context, "New Thing3!", Toast.LENGTH_SHORT).show();
            //   showHelpPopup((Activity) context);
            showHelpPopup(BibleContent.this);
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void searchForText(final String searchString1, final String searchString2) {

            //Toast.makeText(context, "SearchForText!", Toast.LENGTH_LONG).show();

            webView.post(new Runnable() {
                public void run() {
                    Cursor resultSet = getSearchResults(searchString1, searchString2);

                    String bookResult;
                    String chapterResult;
                    String verseResult;
                    String textResult;
                    String combinedResult;


                    while (resultSet.moveToNext())

                    {
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

                    resultSet.close();
                }
            });
            //InsertBibleTxt(resultSet);

        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goToChapter(String bookName, String chapterNumber) {

            //Toast.makeText(context, "test function: "+ chapterNumber, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BibleContent.this, BibleContent.class);

            intent.putExtra("BookName", bookName);
            intent.putExtra("SelectedChapter", chapterNumber);
            //Toast.makeText(getApplicationContext(), "Chapter?:  " + np.getValue(), Toast.LENGTH_SHORT).show();
            startActivity(intent);

        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void openHelpPage() {

            //Toast.makeText(context, "Open Help", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BibleContent.this, HelpPage.class);

            startActivity(intent);

        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void loadNextChapter(final String bookName, final String chapterNumber) {
            webView.post(new Runnable() {
                public void run() {


                    if (nextChapterCounter.equals("-1") || nextChapterCounter.equals("-2")) {
                        //
                        return;
                    }

                    // int nextChapterInt =    Integer.parseInt(extras.getString("SelectedChapter"))+1;
                    nextChapterCounter = Integer.toString(Integer.parseInt(nextChapterCounter) + 1);

                    Cursor resultSet = getBibleText(extras.getString("BookName"), nextChapterCounter);
                    //Toast.makeText(getApplicationContext(), "Chapter?:  "+ resultSet.getCount(), Toast.LENGTH_SHORT).show();

                    if (resultSet.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "Swipe Up to See Next Chapter", Toast.LENGTH_LONG).show();
                        webView.loadUrl("javascript:insertFunction('<br><br>')");
                        // webView.loadUrl("javascript:insertBeforeFunction('atlantaFalcons')");
                    }

                    InsertBibleTxt(resultSet);

                }
            });
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void loadPreviousChapter(final String bookName, final String chapterNumber) {
            webView.post(new Runnable() {
                public void run() {

                    // int nextChapterInt =    Integer.parseInt(extras.getString("SelectedChapter"))+1;
                    previousChapterCounter = Integer.toString(Integer.parseInt(previousChapterCounter) - 1);

                    Cursor resultSet = getBibleText(extras.getString("BookName"), previousChapterCounter);
                    //Toast.makeText(getApplicationContext(), "Chapter?:  "+ previousChapterCounter, Toast.LENGTH_SHORT).show();

                    if (resultSet.getCount() > 0 && Integer.parseInt(previousChapterCounter) > 0) {
                        Toast.makeText(getApplicationContext(), "Swipe Up to See Previous Chapter", Toast.LENGTH_LONG).show();
                        //   webView.loadUrl("javascript:insertFunction('<br><br>')");
                        //  webView.loadUrl("javascript:insertBeforeFunction('atlantaFalcons')");
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
            //i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"rocket452@hotmail.com"});
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

            //Toast.makeText(context, "Open Help", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BibleContent.this, MainActivity.class);

            startActivity(intent);

        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goToTableOfContents() {

            //Toast.makeText(context, "Open Help", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(BibleContent.this, TableOfContents.class);

            startActivity(intent);

        }


        @JavascriptInterface
        public void clearCachedExtras() {

          //  Bundle extras = getIntent().getExtras();

            getIntent().removeExtra("BookName");
            getIntent().removeExtra("SelectedChapter");


        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void sendEmailJSInterface() {
              Toast.makeText(context, "sendEmailJSInterface!", Toast.LENGTH_LONG).show();

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("application/image");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"testemail"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Test Subject");
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


        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);

        LayoutInflater layoutInflater;

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup); //

        //layout.setAnimation(AnimationUtils.loadAnimation(this, R.style.Animation));


        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.setAnimationStyle(R.style.Animation);


        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        int OFFSET_X = 30;
        int OFFSET_Y = 30;

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, 100, 100);

        //Spinner (dropdown)
        final Spinner spinner = (Spinner) layout.findViewById(R.id.fontOptionsSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(BibleContent.this,
                R.array.font_options_array, R.layout.spinner_properties);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        // Apply the adapter to the spinner


        spinner.setAdapter(adapter);
        //End Spinner
        //  spinner.setSelection(4);
        spinner.setSelection(adapter.getPosition(currentSelectedFont));
        //     spinner.setSelection(adapter.getPosition("11"));
        spinner.setOnItemSelectedListener(new SpinnerActivity());
        // Getting a reference to Close button, and close the popup when clicked.
        Button close = (Button) layout.findViewById(R.id.close);


        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getApplicationContext(), "Font Selected: "+(Integer.parseInt(spinner.getSelectedItem().toString())+2), Toast.LENGTH_SHORT).show();
                        // webView.loadUrl("javascript:" + "var elems = document.getElementsByTagName('p');" + "for (var i = 0; i < elems.length; i++) {" + "		document.getElementsByTagName('p')[i].style.fontSize = '" + spinner.getSelectedItem() + "pt';" + "}" + "elems = document.getElementsByTagName('h1');" + "for (var i = 0; i < elems.length; i++) {" + "		document.getElementsByTagName('h1')[i].style.fontSize = '" + (Integer.parseInt(spinner.getSelectedItem().toString()) + 2) + "pt';" + "}" + "elems = document.getElementsByClassName('italic');" + "for (var i = 0; i < elems.length; i++) {" + "		document.getElementsByClassName('italic')[i].style.fontSize = '" + (Integer.parseInt(spinner.getSelectedItem().toString()) - 3) + " +pt';" + "}");
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
            //   }

            // sb.append("<td ><a onclick=\\'goToChapter(\\'MAT\\'," + i + ")\\'>" + i + "</a></td>");
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

        String strItem;


        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using

            // Toast.makeText(parent.getContext(),	"OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),	Toast.LENGTH_SHORT).show();
            currentSelectedFont = parent.getItemAtPosition(pos).toString();

            SharedPreferences.Editor editor = savedSettings.edit();
            editor.putString("savedFont", currentSelectedFont.toString());
            //Toast.makeText(parent.getContext(),	"OnItemSelectedListener : " + currentSelectedFont.toString(),	Toast.LENGTH_SHORT).show();
            // Commit the edits!
            editor.commit();

			/* Font Comparisons
             * 4.5vmin = 14pt
			 * 5.0vmin = 16pt
			 *
			 * */

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


        String[] a = new String[4];
        //   a[0]       = "%one's%";
        a[0] = "%" + searchString1 + "%";
        a[1] = "%" + searchString2 + "%";
        a[2] = "00";
        a[3] = "%<p>%";
        // String query = "SELECT * FROM bible WHERE text LIKE ?";
        String query = "select * from bible where text like ? and text like ? and Chapter > ? and not text like ?";
        Cursor resultSet = myDataBase.rawQuery(query, a);


        return resultSet;
    }


}



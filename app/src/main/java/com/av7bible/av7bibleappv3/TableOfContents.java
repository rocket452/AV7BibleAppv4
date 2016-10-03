package com.av7bible.av7bibleappv3;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;


public class TableOfContents extends Activity implements NumberPicker.OnValueChangeListener {

    private WebView webView;
    String tableOfContentsURL = "file:///android_asset/av7toc.htm";



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        JavaScriptInterface JSInterface = new JavaScriptInterface(this);

       webView.addJavascriptInterface(JSInterface, "JSInterface");

        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl(tableOfContentsURL);

    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            //Toast.makeText(getApplicationContext(),"Can go Back!",Toast.LENGTH_SHORT).show();
            webView.goBack();
        }

     /*   else if(!webView.getUrl().equals(tableOfContentsURL)){

            webView.loadUrl(tableOfContentsURL);
        }
        */
        else {
            //Toast.makeText(getApplicationContext(),"No: "+webView.getUrl() ,Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }

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
        public void goToTitleScreen(){
            //Toast.makeText(context, "New Thing!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(TableOfContents.this, MainActivity.class);
            startActivity(intent);

        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goToTableOfContents(){

            finish();
            Intent intent = new Intent(TableOfContents.this, TableOfContents.class);
            startActivity(intent);

        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void generateChapterPage(){

            //finish();
            Toast.makeText(context, "New Thing2!", Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(TableOfContents.this, BibleContent.class);
            startActivity(intent);


        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void showSelectChapterMenu(){

            openChapterSelectMenu();
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void goToChapter(String bookName,String chapterNumber){

            //Toast.makeText(context, "test function: "+ chapterNumber, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TableOfContents.this, BibleContent.class);

            intent.putExtra("BookName",bookName);
            intent.putExtra("SelectedChapter",chapterNumber);
            //Toast.makeText(getApplicationContext(), "Chapter?:  " + np.getValue(), Toast.LENGTH_SHORT).show();
            startActivity(intent);

        }



        @JavascriptInterface   // must be added for API 17 or higher
        public void searchForText(final String searchString) {

            Toast.makeText(context, "SearchForTextTOC!", Toast.LENGTH_LONG).show();

           webView.post(new Runnable() {
                public void run() {
                     Cursor resultSet = getSearchResults(searchString);

                    String bookResult;
                    String chapterResult;
                    String verseResult;
                    String textResult;
                    String combinedResult;


                    while(resultSet.moveToNext())

                    {
                        bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
                        chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
                        if (chapterResult.substring(0,1).equals("0")){
                           chapterResult = chapterResult.substring(1);
                        }
                        verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
                        textResult = resultSet.getString(resultSet.getColumnIndex("Text"));

                        combinedResult = "<span id=\"verseNumber\">" + bookResult + " " + chapterResult + ":" + verseResult + "</span>" + textResult;


                       // textResult = "water";
                        webView.loadUrl("javascript:insertBody('<p>" + combinedResult + "</p>')");
                    }


                    resultSet.close();
                }
                });
                //InsertBibleTxt(resultSet);

            }

        }



    public void openChapterSelectMenu()
    {

        final Dialog d = new Dialog(TableOfContents.this);
        d.setTitle("Slide to Select Chapter");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(28);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TableOfContents.this, BibleContent.class);

                intent.putExtra("SelectedChapter",String.valueOf(np.getValue()));
                //Toast.makeText(getApplicationContext(), "Chapter?:  " + np.getValue(), Toast.LENGTH_SHORT).show();
                startActivity(intent);

            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();


    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i2) {

    }


    protected Cursor getSearchResults(String searchString) {

        //The Android's default system path of your application database.
        String DB_PATH = "/data/data/com.av7bible.av7bibleappv2/databases/";

        String DB_NAME = "newDb";

        SQLiteDatabase myDataBase;

        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        //  Cursor resultSet = myDataBase.rawQuery("Select * from Bible where Book = \"" + bookName + "\" and chapter = \"" + selectedChapter + "\"", null);
        Cursor resultSet = myDataBase.rawQuery("select * from bible where text like '%" + searchString + "%' and Chapter <> '00'",null);

        return resultSet;
    }


}





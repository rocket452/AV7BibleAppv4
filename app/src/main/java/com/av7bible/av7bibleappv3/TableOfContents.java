package com.av7bible.av7bibleappv3;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;


public class TableOfContents extends Activity implements NumberPicker.OnValueChangeListener {

    private WebView webView;
    String tableOfContentsURL = "file:///android_asset/av7toc.htm";
    CharSequence currentSelectedFont;
    SharedPreferences savedSettings;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        savedSettings = this.getSharedPreferences("AV7BibleAppPreferences", Context.MODE_PRIVATE);
        currentSelectedFont = savedSettings.getString("savedFont", "12");

        JavaScriptInterface JSInterface = new JavaScriptInterface(this);

       webView.addJavascriptInterface(JSInterface, "JSInterface");

        webView.setWebChromeClient(new WebChromeClient());

        webView.loadUrl(tableOfContentsURL);

        //prevents FileUriExposedException
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

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

        @JavascriptInterface
        public void goBack() {

            Intent intent = new Intent(TableOfContents.this, TableOfContents.class);
            startActivity(intent);
        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void openHelpPage() {

            //Toast.makeText(context, "Open Help", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(TableOfContents.this, HelpPage.class);

            startActivity(intent);

        }

        @JavascriptInterface   // must be added for API 17 or higher
        public void openOptionsMenu() {
            //  Toast.makeText(context, "New Thing3!", Toast.LENGTH_SHORT).show();
            //   showHelpPopup((Activity) context);
            showHelpPopup(TableOfContents.this);
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(TableOfContents.this,
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
        String query = "SELECT * FROM bible WHERE text LIKE ? AND text LIKE ? AND Chapter > ? AND NOT text LIKE ? ORDER BY rowid DESC";

        Cursor resultSet = myDataBase.rawQuery(query, a);


        return resultSet;
    }


}





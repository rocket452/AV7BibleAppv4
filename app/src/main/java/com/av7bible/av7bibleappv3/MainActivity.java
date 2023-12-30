package com.av7bible.av7bibleappv3;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.sql.SQLException;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mainImageBtn = (Button) findViewById(R.id.mainImageButton);
        Button readBtn = (Button) findViewById(R.id.readBtn);

        mainImageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TableOfContents.class);
                startActivity(intent);
            }

        });

        readBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TableOfContents.class);
                startActivity(intent);
            }

        });

        //send emails
        Button sendEmails = (Button) findViewById(R.id.shareBtn);

        sendEmails.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //todo need to fix this with new email host that is used by http://av7bible.com/shareContactForm/shareAV7.php
             //   Intent intent = new Intent(MainActivity.this, SharePage.class);
              //  startActivity(intent);

                //todo for now lets just use the same page as the website
                Uri uriUrl = Uri.parse("http://av7bible.com/shareContactForm/shareAV7.php");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);


            }});

        Button goToAmazonBtn = (Button) findViewById(R.id.goToAmazonBtn);

        goToAmazonBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GoToAmazon.class);
                startActivity(intent);
            }

        });

        this.deleteDatabase("newDb");

        File dbfile = this.getDatabasePath("mydb");



       //   File  dbfile = new File("Database/mydb");

       // Toast.makeText(getApplicationContext(), this.getDatabasePath("mydb").toString(), Toast.LENGTH_LONG).show();

        if(!dbfile.exists()) {
          //  Toast.makeText(getApplicationContext(), "Creating Database", Toast.LENGTH_LONG).show();
  //          createBibleDB();
        }
        else {
            Log.i ("info123456","Database Already exists");
        }

        SQLiteDatabase mydatabase = openOrCreateDatabase("mydb", MODE_PRIVATE,null);

//        Cursor resultSet = mydatabase.rawQuery("Select * from Bible",null);

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
            Log.i ("info12345 bookResult",bookResult);
            Log.i ("info12345 chapterResult",chapterResult );
            Log.i ("info12345 verseResult", verseResult);
            Log.i ("info12345 textResult", textResult);
        }
        resultSet.close();
        */

        this.deleteDatabase("mydb");

        DataBaseHelper myDbHelper = new DataBaseHelper(this);


        try {

            myDbHelper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            myDbHelper.openDataBase();

        }catch(SQLException sqle){



        }


    }

    private void createBibleDB() {
        Toast.makeText(getApplicationContext(), "Starting DB Creation: ", Toast.LENGTH_LONG).show();

        // File file = new File ("mydb");


        SQLiteDatabase mydatabase = openOrCreateDatabase("mydb", MODE_PRIVATE, null);
        //SQLiteDatabase mydatabase = openOrCreateDatabase("/data/data/"+ Context.getApplicationContext().getPackageName() +"/databases/" + File.separator + "mydb", MODE_PRIVATE,null);
        File dbfile = this.getDatabasePath("mydb");

        if (dbfile.exists()) {
            Log.i("info1234", dbfile.toString() + " here");
        } else {

            Log.i("info1234", dbfile.toString() + " not here");
        }


        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Bible(Book VARCHAR,Chapter VARCHAR,Verse VARCHAR,Text VARCHAR);");


         /*
        InputStream is;
        try {
            is = getAssets().open("AV7x2");
            int size = is.available(); //size of the file in bytes
            buffer = new byte[size]; //declare the size of the byte array with size of the file
            is.read(buffer); //read file
            is.close(); //close file

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Store text file data in the string variable
        String str_data = new String(buffer);
*/

        String book = "";
        String chapter = "";
        String verse = "";
        String text = "";

        /////////////////////////////////////////////////////////////////////////////////////////
        //InputStream in;
        InputStream instream = null;
        try {
// open the file for reading
            //instream = new FileInputStream("myfilename.txt");
            instream = this.getAssets().open("Database/AV7x");

// if file the available for reading
            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line;

                // read every line of the file into the line-variable, on line at the time
                do {
                    line = buffreader.readLine();
                    //handle apostrophe in Bible text
                    line = line.replace("'", "''");

                    book = line.substring(2, line.indexOf("-"));
                    chapter = line.substring(line.indexOf("-") + 1, line.indexOf(":"));
                    verse = line.substring(line.indexOf(":") + 1, line.indexOf(" "));
                    text = line.substring(line.indexOf(" "), line.length() - 1);

                    Log.i("info1234", chapter);
                    Log.i("info1234", verse);
                    Log.i("info1234", text);

                    mydatabase.execSQL("INSERT INTO Bible VALUES('" + book + "','" + chapter + "','" + verse + "','" + text + "');");
                } while (line != null);
                instream.close();
            }
        } catch (Exception ex) {
            // print stack trace.
        }


        ////////////////////////////////////////////////////////////////////////////////////////////

        //works but only for API 19
        /*
        AssetManager assManager = getApplicationContext().getAssets();


        try (InputStream inputStream = assManager.open("Database/AV7x")) {
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
            String str_data = "";
            int i = 0;
            while ((str_data = bfr.readLine()) != null) {
                //handle apostrophe in Bible text
                str_data = str_data.replace("'", "''");

                book = str_data.substring(2, str_data.indexOf("-"));
                chapter = str_data.substring(str_data.indexOf("-") + 1, str_data.indexOf(":"));
                verse = str_data.substring(str_data.indexOf(":") + 1, str_data.indexOf(" "));
                text = str_data.substring(str_data.indexOf(" "), str_data.length() - 1);
                Log.i("info1234 i", Integer.toString(i));
                Log.i("info1234", chapter);
                Log.i("info1234", verse);
                Log.i("info1234", text);

                mydatabase.execSQL("INSERT INTO Bible VALUES('" + book + "','" + chapter + "','" + verse + "','" + text + "');");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        // Toast.makeText(getApplicationContext(), "Stuff: "+str_data.substring(0,str_data.indexOf("\n")), Toast.LENGTH_LONG).show();
        // mydatabase.execSQL("INSERT INTO Bible VALUES('admin','admin');");

        Cursor resultSet = mydatabase.rawQuery("Select * from Bible", null);
        //resultSet.moveToFirst();

        String bookResult;
        String chapterResult;
        String verseResult;
        String textResult;

        while (resultSet.moveToNext()) {
            bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
            chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
            verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
            textResult = resultSet.getString(resultSet.getColumnIndex("Text"));
            Log.i("info12345 bookResult", bookResult);
            Log.i("info12345 chapterResult", chapterResult);
            Log.i("info12345 verseResult", verseResult);
            Log.i("info12345 textResult", textResult);
        }
        resultSet.close();

        Toast.makeText(getApplicationContext(), "End DB Creation: ", Toast.LENGTH_LONG).show();
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

        void copyFile(){
            File dbfile = this.getDatabasePath("mydb");

           try (FileInputStream inStream = new FileInputStream(dbfile)){
            FileOutputStream outStream = new FileOutputStream("destinationFile");
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();

        } catch (Exception ex) {
        // print stack trace.
    }

        }


    }

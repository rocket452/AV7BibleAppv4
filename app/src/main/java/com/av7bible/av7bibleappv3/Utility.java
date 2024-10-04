package com.av7bible.av7bibleappv3;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {

    public static void retrieveAndDisplaySearchResults(final String searchString1, final String searchString2, final WebView webView, final Context context){
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
                         //   continue;
                        }


                        bookResult = resultSet.getString(resultSet.getColumnIndex("Book"));
                        chapterResult = resultSet.getString(resultSet.getColumnIndex("Chapter"));
                        if (chapterResult.substring(0, 1).equals("0")) {
                            chapterResult = chapterResult.substring(1);
                        }
                        verseResult = resultSet.getString(resultSet.getColumnIndex("Verse"));
                        textResult = resultSet.getString(resultSet.getColumnIndex("Text"));

                        // Highlight searched terms in red
                        textResult = highlightSearchTerms(resultWithoutHTML, searchString1, searchString2);

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
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    resultSet.close();
                }
            });
        }

    }

    private static String highlightSearchTerms(String text, String searchTerm1, String searchTerm2) {

        // Create a pattern that matches either search term, case-insensitive
        Pattern pattern = Pattern.compile("(" + Pattern.quote(searchTerm1.trim()) + "|" + Pattern.quote(searchTerm2.trim()) + ")", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, "<span style=\"color: red;\">$1</span>");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    public static String stripHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        // Remove HTML tags
        String strippedHtml = HTML_TAG_PATTERN.matcher(html).replaceAll("");

        // Replace common HTML entities
        strippedHtml = strippedHtml.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ");

        // Remove extra whitespace
        return strippedHtml.replaceAll("\\s+", " ").trim();
    }



    public static Cursor getSearchResults(String searchString1, String searchString2) {
        Log.d("SearchTag", "getSearchResults");
        searchString1 = searchString1.trim();
        searchString2 = searchString2.trim();

        // The Android's default system path of your application database.
        String DB_PATH = "/data/data/com.av7bible.av7bibleappv2/databases/";
        String DB_NAME = "newDb";
        SQLiteDatabase myDataBase;
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM bible WHERE ");

        // Function to remove content within <>
        queryBuilder.append("(");
        queryBuilder.append("  WITH RECURSIVE");
        queryBuilder.append("  remove_tags(str, res) AS (");
        queryBuilder.append("    SELECT text, ''");
        queryBuilder.append("    UNION ALL");
        queryBuilder.append("    SELECT");
        queryBuilder.append("      substr(str, instr(str, '>')+1),");
        queryBuilder.append("      res || substr(str, 1, instr(str, '<')-1)");
        queryBuilder.append("    FROM remove_tags");
        queryBuilder.append("    WHERE instr(str, '<') > 0");
        queryBuilder.append("  )");
        queryBuilder.append("  SELECT res || str AS cleaned_text");
        queryBuilder.append("  FROM remove_tags");
        queryBuilder.append("  WHERE instr(str, '<') = 0");
        queryBuilder.append("  LIMIT 1");
        queryBuilder.append(") LIKE ?");

        // Second search term (if provided)
        if (!searchString2.isEmpty()) {
            queryBuilder.append(" AND (");
            queryBuilder.append("  WITH RECURSIVE");
            queryBuilder.append("  remove_tags(str, res) AS (");
            queryBuilder.append("    SELECT text, ''");
            queryBuilder.append("    UNION ALL");
            queryBuilder.append("    SELECT");
            queryBuilder.append("      substr(str, instr(str, '>')+1),");
            queryBuilder.append("      res || substr(str, 1, instr(str, '<')-1)");
            queryBuilder.append("    FROM remove_tags");
            queryBuilder.append("    WHERE instr(str, '<') > 0");
            queryBuilder.append("  )");
            queryBuilder.append("  SELECT res || str AS cleaned_text");
            queryBuilder.append("  FROM remove_tags");
            queryBuilder.append("  WHERE instr(str, '<') = 0");
            queryBuilder.append("  LIMIT 1");
            queryBuilder.append(") LIKE ?");
        }

        queryBuilder.append(" AND Chapter > '00'");
        queryBuilder.append(" AND verse != ''");
        queryBuilder.append(" ORDER BY BookOrder");

        String query = queryBuilder.toString();

        List<String> argsList = new ArrayList<>();
        argsList.add("%" + searchString1.toLowerCase() + "%");
        if (!searchString2.isEmpty()) {
            argsList.add("%" + searchString2.toLowerCase() + "%");
        }

        String[] selectionArgs = argsList.toArray(new String[0]);

        return myDataBase.rawQuery(query, selectionArgs);
    }

    public static String buildFullQuery(String query, String[] args) {
        for (String arg : args) {
            int index = query.indexOf("?");
            if (index != -1) {
                query = query.substring(0, index) + "'" + arg.replace("'", "''") + "'" + query.substring(index + 1);
            }
        }
        return query;
    }

    public static void printFullQuery(String query, String[] args) {
        System.out.println("Full SQL Query:");
        System.out.println(buildFullQuery(query, args));
    }


}

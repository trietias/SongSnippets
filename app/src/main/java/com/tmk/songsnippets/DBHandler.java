package com.tmk.songsnippets;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by triet on 12/20/2016.
 */
public class DBHandler extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.tmk.songsnippets/databases/";

    private static String DB_NAME = "songDB.sqlite";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DBHandler(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // method override required for extending SQLite database, but not used in this application
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // method override required for extending SQLite database, but not used in this application
    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

    public List<String> getAllData(String text) {
        List<String> dataList = new ArrayList<String>();
        String selectQuery = "SELECT " + text + " FROM " + "Songs";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding the text to list
                dataList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return data list
        return dataList;
    }

    public List<String> getCategoryData(String category) {
        List<String> dataList = new ArrayList<String>();
        String selectQuery = "SELECT SongTitle FROM Songs WHERE Category1 = '" + category +
                "' OR Category2 = '" + category + "' OR Category3 = '" + category + "' OR Category4 = '"
                + category + "' OR Category5 = '" + category + "' OR Category6 = '" + category +
                "' OR Category7 = '" + category + "' OR Category8 = '" + category +
                "' OR Category9 = '" + category + "' OR Category10 = '" + category +"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding the text to list
                dataList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return data list
        return dataList;
    }

    public String getResourceValue(String title) {
        String selectQuery = "SELECT MAX(ResourceValue) FROM Songs WHERE SongTitle = '" + title + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        String resource = "";
        if (cursor.moveToFirst()) {
            resource = cursor.getString(0);
            cursor.close();
            return resource;
        } else {
            return resource;
        }
    }

    public String getArtist(String title) {
        String selectQuery = "SELECT MAX(SongArtist) FROM Songs WHERE SongTitle = '" + title + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        String resource = "";
        if (cursor.moveToFirst()) {
            resource = cursor.getString(0);
            cursor.close();
            return resource;
        } else {
            return resource;
        }
    }

    public String getCategory(String title) {
        String selectQuery = "SELECT MAX(Category2) FROM Songs WHERE SongTitle = '" + title + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        String resource = "";
        if (cursor.moveToFirst()) {
            resource = cursor.getString(0);
            cursor.close();
            return resource;
        } else {
            return resource;
        }
    }

    public List<String> getCategories() {
        List<String> dataList = new ArrayList<String>();
        String selectQuery = "SELECT Distinct Category1 FROM Songs WHERE Category1 IS NOT NULL " +
                "UNION SELECT Distinct Category2 FROM Songs WHERE Category2 IS NOT NULL " +
                "UNION SELECT Distinct Category3 FROM Songs WHERE Category3 IS NOT NULL " +
                "UNION SELECT Distinct Category4 FROM Songs WHERE Category4 IS NOT NULL ";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding the text to list
                dataList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        // return data list
        return dataList;
    }

}
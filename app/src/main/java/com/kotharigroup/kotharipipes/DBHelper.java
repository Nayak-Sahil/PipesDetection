package com.kotharigroup.kotharipipes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_Name = "KothariPipes";
    private static final int DB_Version = 1;
    private static final String Table_name = "PipesTrack";
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_Name, null, DB_Version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ Table_name + " (pipeId INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, pipeImg BLOB, createdDate VARCHAR, createdTime VARCHAR, innerPipes INTEGER, totalPipes INTEGER, detectedPipes INTEGER)");
    }

    public void onPipesAnalyze(String name, byte[] img, String createdDate, String createdTime, int innerPipes, int totalPipes, int detectedPipes){
        SQLiteDatabase db = this.getWritableDatabase();

        // Use ContentValues to insert data into the database
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("pipeImg", img);
        contentValues.put("createdDate", createdDate);
        contentValues.put("createdTime", createdTime);
        contentValues.put("innerPipes", innerPipes);
        contentValues.put("totalPipes", totalPipes);
        contentValues.put("detectedPipes", detectedPipes);

        // Insert the data
        db.insert(Table_name, null, contentValues);

        // Close the database
        db.close();
    }

    public Cursor getHistory(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ Table_name, null);
        return cursor;
    }

    public Cursor getPipeInsight(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ Table_name + " WHERE id="+ id, null);
        return cursor;
    }

    public void deleteEverything(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+ Table_name);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

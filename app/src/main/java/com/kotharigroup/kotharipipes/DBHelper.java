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
        db.execSQL("CREATE TABLE "+ Table_name + " (pipeId INTEGER PRIMARY KEY AUTOINCREMENT, truckNo VARCHAR, pipeImg BLOB, createdDate VARCHAR, createdTime VARCHAR, innerPipes INTEGER, totalPipes INTEGER, detectedPipes INTEGER, adjustedPipes INTEGER, isRemovedAdjust BOOLEAN, specialNote VARCHAR, pipesInputRecord VARCHAR)");
    }

    public void onPipesAnalyze(String truckNo, byte[] img, String createdDate, String createdTime, int innerPipes, int totalPipes, int detectedPipes, int finalAdjustedPipes, Boolean isRemovedAdjust, String specialNote, String pipesInputRecord){
        SQLiteDatabase db = this.getWritableDatabase();

        // Use ContentValues to insert data into the database
        ContentValues contentValues = new ContentValues();
        contentValues.put("truckNo", truckNo);
        contentValues.put("pipeImg", img);
        contentValues.put("createdDate", createdDate);
        contentValues.put("createdTime", createdTime);
        contentValues.put("innerPipes", innerPipes);
        contentValues.put("totalPipes", totalPipes);
        contentValues.put("detectedPipes", detectedPipes);
        contentValues.put("adjustedPipes", finalAdjustedPipes);
        contentValues.put("isRemovedAdjust", (isRemovedAdjust) ? 0 : 1);
        contentValues.put("specialNote", specialNote);
        contentValues.put("pipesInputRecord", pipesInputRecord);

        // Insert the data
        db.insert(Table_name, null, contentValues);

        // Close the database
        db.close();
    }

    public Cursor getJustMetaData(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT pipeId, truckNo, createdDate, totalPipes FROM "+ Table_name, null);
        return cursor;
    }

    public Cursor getPipeInsight(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ Table_name + " WHERE pipeId="+ id, null);
        return cursor;
    }

    public void deleteEverything(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP Table "+ Table_name);
        onCreate(db);
        db.close();
    }

    public Cursor getRecordsCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) AS Count FROM " + Table_name, null);
        return cursor;
    }

    public Cursor getRecentRecords(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Table_name + " ORDER BY pipeId DESC LIMIT 1", null);
        return cursor;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Table_name);
        onCreate(db);
    }
}

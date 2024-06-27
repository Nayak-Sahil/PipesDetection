package com.kotharigroup.kotharipipes;

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

    public void onPipesAnalyze(String name, Bitmap img, String createdDate, String createdTime, int innerPipes, int totalPipes, int detectedPipes){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO "+ Table_name + " (name, pipeImg, createdDate, createdTime, innerPipes, totalPipes, detectedPipes) VALUES ('"+ name +"', "+ img +", '"+ createdDate +"', '"+ createdTime +"', "+ innerPipes +", "+ totalPipes +", "+ detectedPipes +")");
        db.close();
    }

    public Cursor getHistory(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ Table_name, null);
        db.close();
        return cursor;
    }

    public Cursor getPipeInsight(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ Table_name + " WHERE id="+ id, null);
        db.close();
        return cursor;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

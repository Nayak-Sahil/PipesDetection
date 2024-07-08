package com.kotharigroup.kotharipipes;

import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWindow;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class History extends AppCompatActivity {
    DBHelper dbHelper;
    ListView insightRecordListView;
    List<Integer> pipesIdList;
    File tempImageFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect With Database
        dbHelper = new DBHelper(getApplicationContext(), null, null, 1);

        insightRecordListView = findViewById(R.id.insightRecordListView);
        ArrayList<InsightListView> recordList = new ArrayList<>();

//        dbHelper.deleteEverything();

        // ArrayList of pipesID
        pipesIdList = new ArrayList<>();

        Cursor c = dbHelper.getJustMetaData();
        if(c == null || !c.moveToFirst()){
            Toast.makeText(this, "No past activity found!", Toast.LENGTH_SHORT).show();
        }else{
            do {
                pipesIdList.add(c.getInt(0));
                recordList.add(new InsightListView(c.getString(1), c.getString(2), c.getInt(3)));
            }while(c.moveToNext());

            Collections.reverse(recordList);
            Collections.reverse(pipesIdList);
            InsightListAdapter listAdap = new InsightListAdapter(this, recordList);
            insightRecordListView.setAdapter(listAdap);
        }

        insightRecordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Make Intent to move
                Intent iDetectionView = new Intent(getApplicationContext(), PipesDetection.class);

                // Get pipeId from selected list
                int recordId = pipesIdList.get(position);

                try {
                    Field field = CursorWindow.class.getDeclaredField("sCursorWindowSize");
                    field.setAccessible(true);
                    field.set(null, 100 * 1024 * 1024); //the 100MB is the new size
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Get record of pipeId
                Cursor c = dbHelper.getPipeInsight(recordId);

                if(c == null || !c.moveToFirst()){
                    Toast.makeText(getApplicationContext(), "Sorry, Not Found!", Toast.LENGTH_SHORT).show();
                }else{
                    iDetectionView.putExtra("truck_no", c.getString(1));
                    tempImageFile = saveToTemporary(getBitmapFromBlob(c.getBlob(2)));
                    iDetectionView.putExtra("pipes_img", tempImageFile.getAbsolutePath().toString());
                    iDetectionView.putExtra("pipes_img_temp_file", tempImageFile);
                    iDetectionView.putExtra("created_date", c.getString(3));
                    iDetectionView.putExtra("created_time", c.getString(4));
                    iDetectionView.putExtra("inner_pipes", c.getInt(5));
                    iDetectionView.putExtra("total_pipes", c.getInt(6));
                    iDetectionView.putExtra("detected_outer_pipes", c.getInt(7));
                    iDetectionView.putExtra("adjusted_pipes", c.getInt(8));
                    iDetectionView.putExtra("isRemovedAdjust", c.getInt(9));
                    iDetectionView.putExtra("extra_note", c.getString(10));
                    iDetectionView.putExtra("pipes_datalist", c.getString(11));

                    iDetectionView.putExtra("MODE", "VIEW_MODE");

                    if (tempImageFile == null){
                        Toast.makeText(History.this, "Something went wrong, Please Try again later.", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(History.this, "Please, Wait for few seconds.", Toast.LENGTH_SHORT).show();
                        startActivity(iDetectionView);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        try{
            tempImageFile.deleteOnExit();
        }catch (Exception e){
            Log.d("FILE_DELETION", "SOMETHING WENT WRONG IN FILE DELETION! " + e);
        }
        super.onResume();
    }

    protected File saveToTemporary(Bitmap imgBitmap){
        // Save the bitmap to a temporary file
        FileOutputStream fos = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp", ".png", getCacheDir());
            fos = new FileOutputStream(tempFile);
            imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return tempFile;
    }

    protected Bitmap getBitmapFromBlob(byte[] blobData){
        return BitmapFactory.decodeByteArray(blobData, 0, blobData.length);
    }
}
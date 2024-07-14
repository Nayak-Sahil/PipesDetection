package com.kotharigroup.kotharipipes;


import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button viewHistoryBtn, takePhotoBtn, chooseImgBtn;
    ImageView openRecentRecordDetectionViewBtn;
    TextView recordCountLbl, recentPipesCountLbl;
    DBHelper dbHelper;
    int recentRecordAdjustPipes, recentRecordIsRemovedAdjust;
    int recentRecordInnerPipes, recentRecordOuterPipes, recentRecordTotalPipes;
    String recentRecordCreatedDate, recentRecordCreatedTime, recentRecordNote, recentRecordTruckNo, recentRecordInnerPipeDataList;
    File tempImageFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** EdgeToEdge can make your app display edge-to-edge—using the entire width and height of the display—by
         *  drawing behind the system bars.
         *  The system bars are the {status bar and the navigation bar}.
        */
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // To apply padding so that view can't overlap in systembars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recentPipesCountLbl = findViewById(R.id.recentCountLbl);
        recordCountLbl = findViewById(R.id.recordCountLbl);

        // 1. Establish DB Connection
        dbHelper = new DBHelper(getApplicationContext(), null, null, 1);

        // 2. Update Dashboard Invoked in onResume()

        // 3. Initialize other component.
        viewHistoryBtn = findViewById(R.id.viewHistoryBtn);
        takePhotoBtn = findViewById(R.id.takePhotoBtn);
        chooseImgBtn = findViewById(R.id.chooseImageBtn);
        openRecentRecordDetectionViewBtn = findViewById(R.id.openRecentRecordDetectionViewBtn);

        viewHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent HistoryIntent = new Intent(MainActivity.this, History.class);
                startActivity(HistoryIntent);
            }
        });

        ActivityResultLauncher<Intent> takeImagelauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == Activity.RESULT_OK) {
                    Intent data = o.getData();
                    Bitmap img = (Bitmap) data.getExtras().get("data");
                    Intent detectionIntent = new Intent(getApplicationContext(), PreDetection.class);
                    detectionIntent.putExtra("pipes_img", img);
                    startActivity(detectionIntent);
                }
            }
        });

        ActivityResultLauncher<Intent> chooseImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == Activity.RESULT_OK) {
                    Intent data = o.getData();
                    Intent detectionIntent = new Intent(getApplicationContext(), PreDetection.class);
                    detectionIntent.putExtra("pipes_uri", data.getData());
                    startActivity(detectionIntent);
                }
            }
        });

        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraI = new Intent();
                cameraI.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                takeImagelauncher.launch(cameraI);
            }
        });

        chooseImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraI = new Intent();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    cameraI.setAction(MediaStore.ACTION_PICK_IMAGES);
                }else{
                    cameraI.setAction(Intent.ACTION_PICK);
                }
                chooseImageLauncher.launch(cameraI);
            }
        });

        openRecentRecordDetectionViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recentRecordTotalPipes == 0) return;
                
                Intent viewDetectedRecordI = new Intent(getApplicationContext(), PipesDetection.class);

                viewDetectedRecordI.putExtra("truck_no", recentRecordTruckNo);
                viewDetectedRecordI.putExtra("pipes_img", tempImageFile.getAbsoluteFile().toString());
                viewDetectedRecordI.putExtra("created_date", recentRecordCreatedDate);
                viewDetectedRecordI.putExtra("created_time", recentRecordCreatedTime);
                viewDetectedRecordI.putExtra("inner_pipes", recentRecordInnerPipes);
                viewDetectedRecordI.putExtra("total_pipes", recentRecordTotalPipes);
                viewDetectedRecordI.putExtra("detected_outer_pipes", recentRecordOuterPipes);
                viewDetectedRecordI.putExtra("adjusted_pipes", recentRecordAdjustPipes);
                viewDetectedRecordI.putExtra("isRemovedAdjust", recentRecordIsRemovedAdjust);
                viewDetectedRecordI.putExtra("extra_note", recentRecordNote);
                viewDetectedRecordI.putExtra("pipes_datalist", recentRecordInnerPipeDataList);

                viewDetectedRecordI.putExtra("MODE", "VIEW_MODE");

                Toast.makeText(getApplicationContext(), "Please, Wait for few seconds.", Toast.LENGTH_SHORT).show();

                startActivity(viewDetectedRecordI);
            }
        });

    }

    protected Bitmap getBitmapFromBlob(byte[] blobData){
        return BitmapFactory.decodeByteArray(blobData, 0, blobData.length);
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

            return tempFile;
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

    protected void updateDashboard(){
        // 1. Fetch All Records Count
        int totalHistory = fetchHistoryCount();
        recordCountLbl.setText(String.valueOf(totalHistory));


        // 2. Fetch Last Recent Records
        int recentPipesCount = fetchLastRecentRecord();
        recentPipesCountLbl.setText(String.valueOf(recentPipesCount));
    }

    @Override
    protected void onResume() {
        updateDashboard();
        super.onResume();
    }

    protected int fetchHistoryCount(){
        try{
            Cursor c = dbHelper.getRecordsCount();
            if(c == null || !c.moveToFirst()){
                return 0;
            }else{
                return c.getInt(0);
            }
        }catch (Exception e){
            Toast.makeText(this, "Sorry, Unable to fetch records count.", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    protected int fetchLastRecentRecord(){
        try{
            Cursor c = dbHelper.getRecentRecords();
            if(c == null || !c.moveToFirst()){
                return 0;
            }else{
                recentRecordInnerPipes = c.getInt(5);
                recentRecordOuterPipes = c.getInt(7);
                recentRecordTotalPipes = c.getInt(6);

                recentRecordAdjustPipes = c.getInt(8);
                recentRecordIsRemovedAdjust = c.getInt(9);

                recentRecordCreatedDate = c.getString(3);
                recentRecordCreatedTime = c.getString(4);
                recentRecordNote = c.getString(10);
                recentRecordTruckNo = c.getString(1);
                recentRecordInnerPipeDataList = c.getString(11);

                tempImageFile = saveToTemporary(getBitmapFromBlob(c.getBlob(2)));
            }
        }catch (Exception e){
            Toast.makeText(this, "Sorry, Unable to fetch recent records.", Toast.LENGTH_SHORT).show();
            return 0;
        }

        return recentRecordTotalPipes;
    }

}
package com.kotharigroup.kotharipipes;


import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
    ImageView homeSettingBtn;
    TextView recordCountLbl, recentPipesCountLbl, dialogTitle;
    DBHelper dbHelper;
    Dialog defaultInnerPipesDialog;
    Button cancelDialogBtn, setInnerPipesCountDialogBtn;
    EditText inptDialog;
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

        // Open Settings - Default_Inner_Pipes_Count Dialog
        homeSettingBtn = findViewById(R.id.homeSettingBtn);

        // Initialize Dialog
        defaultInnerPipesDialog = new Dialog(MainActivity.this);

        // Set Custome Dialog Layout
        defaultInnerPipesDialog.setContentView(R.layout.dialog);

        // Set layout
        defaultInnerPipesDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        homeSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultInnerPipesDialog.show();
            }
        });

        // Dialog Action-Button Operation
        cancelDialogBtn = defaultInnerPipesDialog.findViewById(R.id.cancelDialogBtn);
        setInnerPipesCountDialogBtn = defaultInnerPipesDialog.findViewById(R.id.analyzeDialogBtn);
        inptDialog = defaultInnerPipesDialog.findViewById(R.id.innerPipesInptDialog);
        dialogTitle = defaultInnerPipesDialog.findViewById(R.id.dialogTitle);

        // Set Dialog title
        dialogTitle.setText("Default " + dialogTitle.getText());
        cancelDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultInnerPipesDialog.dismiss();
            }
        });

        setInnerPipesCountDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Taking input from user.
                int defaultInnerPipesCount = Integer.parseInt(inptDialog.getText().toString());

                // making SharedPreference to store small amount of data in key-value pair.
                SharedPreferences defaultPreferences = getApplicationContext().getSharedPreferences("User_Preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = defaultPreferences.edit();

                // put data in it.
                editor.putInt("default_inner_pipes", defaultInnerPipesCount);

                // save data in it.
                editor.apply();

                // close the dialog.
                defaultInnerPipesDialog.dismiss();
            }
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
                cameraI.setAction(MediaStore.ACTION_PICK_IMAGES);
                chooseImageLauncher.launch(cameraI);
            }
        });

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
                return c.getInt(0);
            }
        }catch (Exception e){
            Toast.makeText(this, "Sorry, Unable to fetch recent records.", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

}
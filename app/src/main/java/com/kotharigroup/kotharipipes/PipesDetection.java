package com.kotharigroup.kotharipipes;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.Buffer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class PipesDetection extends AppCompatActivity {

    ImageView imgView, closeDtlDialogBtn;
    LinearLayout reTakeBtn, viewMoreBtn, editInputPipesBtn, correctDetectionBtn;
    Boolean isFromCapture;
    ActivityResultLauncher<Intent> launcher;
    Intent retakeIntent;
    TextView innerPipesCountLbl, insightDateLbl, insightTimeLbl, totalPipesCountLbl, detectedPipesCountLbl, curnInsightNameLbl;
    TextView truckNumDialogLbl, noteDialogLbl, dateDialogLbl, timeDialogLbl, outerPipesCntDialogLbl, innerPipesCntDialogLbl, ttlPipesCntDialogLbl, adjustedPipeCountViewDialogLbl;

    TextView detectedOuterPipeCrctDialogLbl, innerPipeCrctDialogLbl, ttlPipeCrctDialogLbl, finalAdjustedPipeCrctDialogLbl;
    EditText adjustPipeCountCrctDialogInpt;
    RadioGroup adjustActionPipeCntCrctDialogRdgrp;
    Button cancelCrctDialogBtn, saveCrctDialogBtn;
    int innerPipesCount = 0, detectedPipesCount = 0, totalPipesCount = 0, adjustedPipeCont = 0;
    Boolean isRemovedAdjust = false;
    Dialog inptDialog, viewMoreDialog, correctDataDialog;
    String dateNow = "", timeNow = "";
    private String currentInsightName = "";
    DBHelper dbHelper;
    Bitmap bitmapOfPipes;
    OkHttpClient client;
    SharedPreferences userPreferences;
    List<Map<String, String>> pipesList;
    String truck_no, specialNote;
    ListView pipeDataDialogList;
    String intentMode;
    int isRemovedAdjustFromDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pipes_detection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get first Intent Mode;
        intentMode = getIntent().getStringExtra("MODE");

        // First retrieve Default Pipes count from sharedPreference;
        userPreferences = getApplicationContext().getSharedPreferences("User_Preferences", MODE_PRIVATE);
        int defaultInnerPipesCount = userPreferences.getInt("default_inner_pipes", 0);

        // Set defaultInnerPipesCount to innerPipesCount which is a count for current detection-activity.
        innerPipesCount = defaultInnerPipesCount;

        // Initialize HttpConnection
        client = new OkHttpClient();
        /**
         * // For log: Request Body
         * OkHttpClient client = new OkHttpClient.Builder()
         *         .addInterceptor(new LoggingInterceptor())
         *         .build();
         */

        // Establish DB Connection
        dbHelper = new DBHelper(getApplicationContext(), null, null, 1);

        // 1. Get Date and Time Labels
        insightDateLbl = findViewById(R.id.insightDateLbl);
        insightTimeLbl = findViewById(R.id.insightTimeLbl);

        // 2. Get Meta-Data label to show data on it.
        totalPipesCountLbl = findViewById(R.id.totalPipesCountLbl);
        detectedPipesCountLbl = findViewById(R.id.detectedPipesCountLbl);
        curnInsightNameLbl = findViewById(R.id.curnInsightNameLbl);
        innerPipesCountLbl = findViewById(R.id.innerPipesCountLbl);

        // 3. Get Required Data from previous activity-Intent
        truck_no = getIntent().getStringExtra("truck_no").toString();
        specialNote = getIntent().getStringExtra("extra_note").toString();

        Type type = new TypeToken<List<Map<String, String>>>() {}.getType();
        pipesList = (List<Map<String, String>>) new Gson().fromJson(getIntent().getStringExtra("pipes_datalist"), type);

        if(intentMode.equals("VIEW_MODE")){
            dateNow = getIntent().getStringExtra("created_date");
            timeNow = getIntent().getStringExtra("created_time");
            innerPipesCount = getIntent().getIntExtra("inner_pipes", 0);
            detectedPipesCount = getIntent().getIntExtra("detected_outer_pipes", 0);
            totalPipesCount = getIntent().getIntExtra("total_pipes", 0);
            adjustedPipeCont = getIntent().getIntExtra("adjusted_pipes", 0);
            isRemovedAdjustFromDB = getIntent().getIntExtra("isRemovedAdjust", 0);
            isRemovedAdjust = (isRemovedAdjustFromDB == 0) ? true : false;
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dateNow = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM, YYYY")).toString();
                timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("hh : mm a")).toString().toUpperCase();
            }
            innerPipesCount = pipesList.size() - 1;
        }

        // Update Initial Necessary Details as per given data
        currentInsightName = truck_no;
        insightDateLbl.setText(dateNow);
        insightTimeLbl.setText(timeNow);

        // Show frequently changed details based on detection
        computePipeCountAndShow();

        // 4. View More Dialog
        viewMoreDialog = new Dialog(PipesDetection.this);
        viewMoreDialog.setContentView(R.layout.metadata_view_dialog);
        viewMoreDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        viewMoreBtn = findViewById(R.id.viewMoreBtn);
        viewMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttlPipesCntDialogLbl.setText(String.valueOf(totalPipesCount));

                viewMoreDialog.show();
            }
        });

        // 5. Initialize View-More-Dialog's component
        truckNumDialogLbl = viewMoreDialog.findViewById(R.id.truckNumLbl);
        noteDialogLbl = viewMoreDialog.findViewById(R.id.specialNoteLbl);
        dateDialogLbl = viewMoreDialog.findViewById(R.id.detectionDateLbl);
        timeDialogLbl = viewMoreDialog.findViewById(R.id.detectionTimeLbl);
        outerPipesCntDialogLbl = viewMoreDialog.findViewById(R.id.ttlDetctdOuterPipeCountLbl);
        innerPipesCntDialogLbl = viewMoreDialog.findViewById(R.id.ttlInnerPipeCountLbl);
        ttlPipesCntDialogLbl = viewMoreDialog.findViewById(R.id.ttlPipeCountLbl);
        pipeDataDialogList = viewMoreDialog.findViewById(R.id.pipeDataDialogList);
        closeDtlDialogBtn = viewMoreDialog.findViewById(R.id.closeDtlDialogBtn);
        adjustedPipeCountViewDialogLbl = viewMoreDialog.findViewById(R.id.adjustedPipeCountLbl);

        closeDtlDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMoreDialog.dismiss();
            }
        });

        // 6. Show all received details to Dialog
        showAllDtlsToViewMoreDialog();

        editInputPipesBtn = findViewById(R.id.editInputPipesBtn);

        editInputPipesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to previous activity
                finish(); // finish() destroys the current activity and returns to the previous activity with saved details.
            }
        });

        // 7. Correct Detection Dialog
        correctDataDialog = new Dialog(PipesDetection.this);
        correctDataDialog.setContentView(R.layout.dialog);
        correctDataDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // 8. Correct Detection Dialog's Component
        detectedOuterPipeCrctDialogLbl = correctDataDialog.findViewById(R.id.outerPipesCountLbl);
        innerPipeCrctDialogLbl = correctDataDialog.findViewById(R.id.innerPipesCountLbl);
        ttlPipeCrctDialogLbl = correctDataDialog.findViewById(R.id.totalPipesCountLbl);
        finalAdjustedPipeCrctDialogLbl = correctDataDialog.findViewById(R.id.correctedTotalPipesCountLbl);

        adjustPipeCountCrctDialogInpt = correctDataDialog.findViewById(R.id.innerPipesInptDialog);
        adjustActionPipeCntCrctDialogRdgrp = correctDataDialog.findViewById(R.id.adjustActionRdoGrp);
        cancelCrctDialogBtn = correctDataDialog.findViewById(R.id.cancelDialogBtn);
        saveCrctDialogBtn = correctDataDialog.findViewById(R.id.analyzeDialogBtn);

        cancelCrctDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correctDataDialog.dismiss();
            }
        });

        saveCrctDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalPipesCountLbl.setText(String.valueOf(totalPipesCount));

                String adjustStr = (isRemovedAdjust) ? " - " : " + ";
                adjustedPipeCountViewDialogLbl.setText(adjustStr + String.valueOf(adjustedPipeCont));

                saveThisPoint(getImageByte(bitmapOfPipes));

                Toast.makeText(getApplicationContext(), "Adjusted count updated", Toast.LENGTH_SHORT).show();

                correctDataDialog.dismiss();
            }
        });
        
        adjustActionPipeCntCrctDialogRdgrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String adjustPipeCountStr = adjustPipeCountCrctDialogInpt.getText().toString();
                if (adjustPipeCountStr.length() == 0){
                    Toast.makeText(getApplicationContext(), "Please, Adjust pipe count.", Toast.LENGTH_SHORT).show();
                    RadioButton checkedRadio = correctDataDialog.findViewById(checkedId);
                    checkedRadio.setChecked(false);
                }else{
                    adjustedPipeCont = Integer.parseInt(adjustPipeCountCrctDialogInpt.getText().toString());
                    if(checkedId == R.id.addPipeCountRdo){
                        totalPipesCount += adjustedPipeCont;
                        finalAdjustedPipeCrctDialogLbl.setText(String.valueOf(adjustedPipeCont));
                        isRemovedAdjust = false;
                    }else{
                        totalPipesCount -= adjustedPipeCont;
                        finalAdjustedPipeCrctDialogLbl.setText(String.valueOf(adjustedPipeCont));
                        isRemovedAdjust = true;
                    }
                }
            }
        });

        // Show Correct Detection Dialog
        correctDetectionBtn = findViewById(R.id.correctDetectionBtn);
        correctDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show Updated Data while opening Correct Dialog, Although while post request we update all the labels but still it's required to call here as well.
                showDataOnCorrectDialog();

                correctDataDialog.show();
            }
        });

        // Re-Take Functionality
        imgView = findViewById(R.id.imgView);
        reTakeBtn = findViewById(R.id.reTakeBtn);
        retakeIntent = new Intent();

        // Taking image from intent data.
        if(intentMode.equals("VIEW_MODE")){
            String filePath = getIntent().getStringExtra("pipes_img").toString();
            bitmapOfPipes = BitmapFactory.decodeFile(filePath);
            imgView.setImageBitmap(bitmapOfPipes);
        }else{
            try{
                Uri uri = getIntent().getParcelableExtra("pipes_uri");
                Bitmap img = getIntent().getParcelableExtra("pipes_img");
                if(img != null) {
                    isFromCapture = true;
                    launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if (o.getResultCode() == Activity.RESULT_OK) {
                                Intent data = o.getData();
                                Bitmap imgBitmap = (Bitmap) data.getExtras().get("data");
                                setImageToView(imgBitmap);
                                bitmapOfPipes = imgBitmap;

                                // Re-Detect Image: Since Image Gets Changed.
                                detectNStore();
                            }
                        }
                    });

                    retakeIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    setImageToView(img);
                    bitmapOfPipes = img;
                } else if(uri != null) {
                    isFromCapture = false;
                    launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if (o.getResultCode() == Activity.RESULT_OK) {
                                Intent data = o.getData();
                                Uri uri = data.getData();
                                setImageToView(uri);
                                try {
                                    bitmapOfPipes = getBitMapImage(uri);

                                    // Re-Detect Image: Since Image Gets Changed.
                                    detectNStore();
                                } catch (IOException e) {
                                    Toast.makeText(PipesDetection.this, "Something went wrong in IMAGE.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        retakeIntent.setAction(MediaStore.ACTION_PICK_IMAGES);
                    }else{
                        retakeIntent.setAction(Intent.ACTION_PICK);
                    }
                    setImageToView(uri);
                    bitmapOfPipes = getBitMapImage(uri);
                }
            }catch(Exception e){
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }

        // Recomputing again based on new image.
        reTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(intentMode.equals("DETECTION_MODE")){
                    launcher.launch(retakeIntent);
                }else{
                    finish();
                }
            }
        });

        // Send Post Request and store analyzed data in database when bitmap image assigned, therefore we have to call this function after try..catch because at there bitmap image is assigned
        if(intentMode.equals("DETECTION_MODE")){
            detectNStore();
        }else{
            TextView retakeBtnText = findViewById(R.id.retakeBtnText);
            retakeBtnText.setText("Back");
            editInputPipesBtn.setVisibility(View.GONE);
        }
    }

    protected void showDataOnCorrectDialog(){
        detectedOuterPipeCrctDialogLbl.setText(String.valueOf(detectedPipesCount));
        innerPipeCrctDialogLbl.setText(String.valueOf(innerPipesCount));
        ttlPipeCrctDialogLbl.setText(String.valueOf(totalPipesCount));
        finalAdjustedPipeCrctDialogLbl.setText(String.valueOf(totalPipesCount));
    }

    protected void showAllDtlsToViewMoreDialog(){
        // Set Data to respective labels
        truckNumDialogLbl.setText(truck_no);
        noteDialogLbl.setText(specialNote.toString().length() == 0 ? "-" : specialNote);
        dateDialogLbl.setText(dateNow);
        timeDialogLbl.setText(timeNow);
        outerPipesCntDialogLbl.setText(String.valueOf(detectedPipesCount));
        innerPipesCntDialogLbl.setText(String.valueOf(innerPipesCount));
        ttlPipesCntDialogLbl.setText(String.valueOf(totalPipesCount));

        String adjustStr = (isRemovedAdjust) ? " - " : " + ";
        adjustedPipeCountViewDialogLbl.setText(adjustStr + String.valueOf(adjustedPipeCont));

        // Set Input-Pipes-Specification to ListView
        setViewToList();
    }

    protected void setViewToList(){
        String[] from = {"pipeSize", "pipeCount"};

        // Ids of views in the layout (android.R.layout.simple_list_item_2)
        int[] to = {R.id.pipeSizeListLbl, R.id.pipeQuantListLbl};

        // Create the SimpleAdapter
        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), pipesList, R.layout.input_parameter_list, from, to);

        // Set the adapter to the ListView
        pipeDataDialogList.setAdapter(adapter);
    }

    protected byte[] getImageByte(Bitmap img){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] imgBytes = new byte[0];
        try {
            //bitmap to byte[] stream
            img.compress(Bitmap.CompressFormat.PNG, 100, stream);
            imgBytes = stream.toByteArray();

        } catch (Exception e){
            Toast.makeText(this, "Sorry, Unable to create Byte-Image, Try again.", Toast.LENGTH_SHORT).show();
        } finally {
            // Close the stream
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return imgBytes;
    }

    protected Bitmap getBitMapImage(Uri uri) throws IOException {
        return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
    }

    protected void computePipeCountAndShow(){
        // Update Current InsightPipe Label Name
        curnInsightNameLbl.setText(currentInsightName);

        // Show Inner Pipes Count
        innerPipesCountLbl.setText(String.valueOf(innerPipesCount));

        // Show Detected Outer Pipes Count
        detectedPipesCountLbl.setText(String.valueOf(detectedPipesCount) + " Outer Pipes");

        // Compute Total Pipes Count
        if(!intentMode.equals("VIEW_MODE")){
            if(innerPipesCount == 0){
                totalPipesCount = detectedPipesCount;
            }else if(innerPipesCount == 1){
                totalPipesCount = detectedPipesCount + detectedPipesCount;
            }else{
                totalPipesCount = innerPipesCount * detectedPipesCount;
            }
        }

        // Show Total No. Of Pipes Count
        totalPipesCountLbl.setText(String.valueOf(totalPipesCount));
    }

    protected void setImageToView(Uri uri){ imgView.setImageURI(uri); }

    protected void setImageToView(Bitmap img){
        imgView.setImageBitmap(img);
    }

    protected void postRequest(byte[] imageData) {
        Toast.makeText(this, "Processing Your Image...", Toast.LENGTH_SHORT).show();

        // Custome Content-Type Set "image/jpg" (*Not "Image/Jpeg")
        RequestBody reqBody = RequestBody.create(MediaType.parse("image/jpg"), imageData);

        Request request = new Request.Builder()
                .url(getResources().getString(R.string.DETECTION_POST_URL))
                .post(reqBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endActivity("Failed To Analyze Image. Please Try Again.");
                    }
                });
                Log.e("#FAILED_POST_REQUEST", e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String jsonStrResponse = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            // Fetch Detected Pipes Count
                            JSONObject responseJson = new JSONObject(jsonStrResponse);
                            detectedPipesCount = Integer.parseInt(String.valueOf(responseJson.get("result")));

                            String base64Image = responseJson.get("image").toString();
                            byte[] imageByte = Base64.decode(base64Image, Base64.DEFAULT);
                            bitmapOfPipes = getBitmapFromBlob(imageByte);
                            imgView.setImageBitmap(bitmapOfPipes);

                            // Compute Total Pipes Count and Show to Labels
                            computePipeCountAndShow();

                            // Update ViewMoreDialog Details
                            showAllDtlsToViewMoreDialog();

                            // Insert data into the database
                            saveThisPoint(imageByte);

                            // Set Visibility to Correct-Detection-Button
                            correctDetectionBtn.setVisibility(View.VISIBLE);

                            // Show data on Correct-Detection Dialog
                            showDataOnCorrectDialog();
                        }catch (Exception e){
                            endActivity("Unable to save it. Please Try Again.");
                            Log.d("ERROR WHILE SHOWING DETECTED VALUE", e.toString());
                        }
                    }
                });

                Log.d("#SUCCESS_POST_REQUEST", jsonStrResponse);
            }
        });

    }


    protected Bitmap getBitmapFromBlob(byte[] blobData){
        return BitmapFactory.decodeByteArray(blobData, 0, blobData.length);
    }

    protected void detectNStore(){
        // Get Bytes of Bitmap, To send in post request.
        byte[] imgBytes = getImageByte(bitmapOfPipes);

        // Send Post Request
        postRequest(imgBytes);
    }

    protected void endActivity(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        finish();
    }

    protected void saveThisPoint(byte[] imageData){
        try{
            Gson gson = new Gson();
            String pipeInputListRecord = gson.toJson(pipesList);

            dbHelper.onPipesAnalyze(currentInsightName, imageData, dateNow, timeNow, innerPipesCount, totalPipesCount, detectedPipesCount, adjustedPipeCont, isRemovedAdjust, specialNote, pipeInputListRecord);

            Toast.makeText(this, "Saved Insights.", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, "Sorry, Unable to store your data!", Toast.LENGTH_SHORT).show();
        }
    }
}
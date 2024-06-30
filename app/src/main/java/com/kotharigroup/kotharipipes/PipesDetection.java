package com.kotharigroup.kotharipipes;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PipesDetection extends AppCompatActivity {

    ImageView imgView;
    LinearLayout reTakeBtn, editInnerPipesBtn;
    Boolean isFromCapture;
    ActivityResultLauncher<Intent> launcher;
    Intent retakeIntent;
    Button cancelDialogBtn, analyzeDialogBtn;
    EditText innerPipesInptDialog;
    TextView innerPipesCountLbl, insightDateLbl, insightTimeLbl, totalPipesCountLbl, detectedPipesCountLbl, curnInsightNameLbl;
    int innerPipesCount = 0, detectedPipesCount = 0, totalPipesCount = 0;
    Dialog inptDialog;
    String dateNow = "", timeNow = "";
    private String InsightPrefix = "PIPE";
    private String currentInsightName = "";
    DBHelper dbHelper;
    Bitmap bitmapOfPipes;
    OkHttpClient client;
    SharedPreferences userPreferences;

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

        // First retrieve Default Pipes count from sharedPreference;
        userPreferences = getApplicationContext().getSharedPreferences("User_Preferences", MODE_PRIVATE);
        int defaultInnerPipesCount = userPreferences.getInt("default_inner_pipes", 0);

        // Set defaultInnerPipesCount to innerPipesCount which is a count for current detection-activity.
        innerPipesCount = defaultInnerPipesCount;

        // Initialize HttpConnection
        client = new OkHttpClient();

        // Establish DB Connection
        dbHelper = new DBHelper(getApplicationContext(), null, null, 1);

        // 1. Show Date and current time
        insightDateLbl = findViewById(R.id.insightDateLbl);
        insightTimeLbl = findViewById(R.id.insightTimeLbl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dateNow = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM, YYYY")).toString();
            timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("hh : mm a")).toString().toUpperCase();
            insightDateLbl.setText(dateNow);
            insightTimeLbl.setText(timeNow);
        }

        // 2. Get Meta-Data label to show data on it.
        totalPipesCountLbl = findViewById(R.id.totalPipesCountLbl);
        detectedPipesCountLbl = findViewById(R.id.detectedPipesCountLbl);
        curnInsightNameLbl = findViewById(R.id.curnInsightNameLbl);
        innerPipesCountLbl = findViewById(R.id.innerPipesCountLbl);

        // 3. Inner Pipes Dialog Create
        inptDialog = new Dialog(PipesDetection.this);
        inptDialog.setContentView(R.layout.dialog);
        inptDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        editInnerPipesBtn = findViewById(R.id.editInnerPipesBtn);
        editInnerPipesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInnerPipesDialog();
            }
        });

//        showInnerPipesDialog();

        // Dialog Action-Button Operation
        cancelDialogBtn = inptDialog.findViewById(R.id.cancelDialogBtn);
        analyzeDialogBtn = inptDialog.findViewById(R.id.analyzeDialogBtn);
        innerPipesInptDialog = inptDialog.findViewById(R.id.innerPipesInptDialog);
        cancelDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just close dialog, because onDismiss Listener logic is written.
                inptDialog.dismiss();
            }
        });

        analyzeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                innerPipesCount = Integer.parseInt(innerPipesInptDialog.getText().toString());

                // first recompute as updated parameter then save it in DB.
                computePipeCountAndShow();

                // create new record for every new update computation even when image is same.
                saveThisPoint(getImageByte(bitmapOfPipes));

                // Just close dialog, because onDismiss Listener logic is written.
                inptDialog.dismiss();
            }
        });

        imgView = findViewById(R.id.imgView);
        reTakeBtn = findViewById(R.id.reTakeBtn);
        retakeIntent = new Intent();

        // Taking image from intent data.
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
                            bitmapOfPipes = img;

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
                            Uri uri = (Uri) data.getData();
                            setImageToView(uri);
                            try {
                                bitmapOfPipes = getBitMapImage(uri);

                                // Re-Detect Image: Since Image Gets Changed.
                                detectNStore();
                            } catch (IOException e) {
                                Toast.makeText(PipesDetection.this, "Something went wrong in IMAGE.", Toast.LENGTH_SHORT).show();;
                            }
                        }
                    }
                });

                retakeIntent.setAction(MediaStore.ACTION_PICK_IMAGES);
                setImageToView(uri);
                bitmapOfPipes = getBitMapImage(uri);
            }
        }catch(Exception e){
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        // Recomputing again based on new image.
        reTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launcher.launch(retakeIntent);
            }
        });

        // Send Post Request and store analyzed data in database when bitmap image assigned, therefore we have to call this function after try..catch because at there bitmap image is assigned
        detectNStore();
    }

    protected void showInnerPipesDialog(){
        try{
            inptDialog.show();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Cannot open dialog. Try again.", Toast.LENGTH_SHORT).show();
            Log.e("#INNER_PIPE_DIALOG", e.toString());
        }
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
        totalPipesCount = (innerPipesCount == 0) ? detectedPipesCount : innerPipesCount * detectedPipesCount;

        // Show Total No. Of Pipes Count
        totalPipesCountLbl.setText(String.valueOf(totalPipesCount));
    }

    protected void setImageToView(Uri uri){ imgView.setImageURI(uri); }

    protected void setImageToView(Bitmap img){
        imgView.setImageBitmap(img);
    }

    protected void postRequest(byte[] imageData){
        Toast.makeText(this, "Processing Your Image...", Toast.LENGTH_SHORT).show();
        RequestBody reqBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "image.png", RequestBody.create(MediaType.parse("image/png"), imageData))
                .build();

        Request request = new Request.Builder()
                .url(getResources().getString(R.string.DETECTION_POST_URL))
                .post(reqBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
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
                            detectedPipesCount = Integer.parseInt(jsonStrResponse.substring(jsonStrResponse.indexOf(" ") + 1, jsonStrResponse.indexOf("}")));

                            // Compute Total Pipes Count and Show to Labels
                            computePipeCountAndShow();

                            // Insert data into the database
                            saveThisPoint(imageData);
                        }catch (Exception e){
                            Log.d("ERROR WHILE SHOWING DETECTED VALUE", e.toString());
                        }
                    }
                });

                Log.d("#SUCCESS_POST_REQUEST", jsonStrResponse);
            }
        });

    }

    protected void detectNStore(){
        // Get Bytes of Bitmap, To send in post request.
        byte[] imgBytes = getImageByte(bitmapOfPipes);

        // Send Post Request
        postRequest(imgBytes);

        // Close Dialog
        inptDialog.dismiss();
    }

    protected void saveThisPoint(byte[] imageData){
        try{
            // Set ID to Current Detection Activity.
            currentInsightName = InsightPrefix + "-" + dateNow.substring(0, dateNow.indexOf(' '));
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                currentInsightName += LocalTime.now().format(DateTimeFormatter.ofPattern("hmmss")).toString();
            }

            dbHelper.onPipesAnalyze(this.currentInsightName, imageData, dateNow, timeNow, innerPipesCount, totalPipesCount, detectedPipesCount);

            Toast.makeText(this, "✨ Saved Current Insight.", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, "Sorry, Unable to store your data!", Toast.LENGTH_SHORT).show();
        }
    }
}
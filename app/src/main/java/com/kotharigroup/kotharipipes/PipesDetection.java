package com.kotharigroup.kotharipipes;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
    Bitmap pipesDetectionImg;

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


        dbHelper = new DBHelper(getApplicationContext(), null, null, 1);

        totalPipesCountLbl = findViewById(R.id.totalPipesCountLbl);
        detectedPipesCountLbl = findViewById(R.id.detectedPipesCountLbl);
        curnInsightNameLbl = findViewById(R.id.curnInsightNameLbl);

        insightDateLbl = findViewById(R.id.insightDateLbl);
        insightTimeLbl = findViewById(R.id.insightTimeLbl);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dateNow = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM, YYYY")).toString();
            timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("hh : mm a")).toString().toUpperCase();
            insightDateLbl.setText(dateNow);
            insightTimeLbl.setText(timeNow);
        }

        imgView = findViewById(R.id.imgView);
        reTakeBtn = findViewById(R.id.reTakeBtn);
        retakeIntent = new Intent();

        editInnerPipesBtn = findViewById(R.id.editInnerPipesBtn);
        innerPipesCountLbl = findViewById(R.id.innerPipesCountLbl);
        editInnerPipesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditInnerPipesDialog();
            }
        });

        inptDialog = new Dialog(PipesDetection.this);
        inptDialog.setContentView(R.layout.dialog);
        inptDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        showEditInnerPipesDialog();


        cancelDialogBtn = inptDialog.findViewById(R.id.cancelDialogBtn);
        analyzeDialogBtn = inptDialog.findViewById(R.id.analyzeDialogBtn);
        innerPipesInptDialog = inptDialog.findViewById(R.id.innerPipesInptDialog);
        cancelDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make Record of Default-0 InnerPipesCount and store it in DB.
                currentInsightName = InsightPrefix + "-" + dateNow.substring(0, dateNow.indexOf(' '));
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currentInsightName += LocalTime.now().format(DateTimeFormatter.ofPattern("hmmss")).toString();
                }

                // Display Default InnerPipes Count-0 to User
                computePipeCountAndShow();

                createInsightRecord(currentInsightName, pipesDetectionImg, dateNow, timeNow, innerPipesCount, totalPipesCount, detectedPipesCount);

                // Close Dialog
                inptDialog.dismiss();
            }
        });

        analyzeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Take InnerPipes Count from User
                innerPipesCount = (innerPipesInptDialog.getText().length() != 0) ? Integer.parseInt(innerPipesInptDialog.getText().toString()) : 0;

                // Make Record and store it in DB.
                currentInsightName = InsightPrefix + "-" + dateNow.substring(0, dateNow.indexOf(' '));
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currentInsightName += LocalTime.now().format(DateTimeFormatter.ofPattern("hmmss")).toString();
                }

                // Display InnerPipes Count to User
                computePipeCountAndShow();

                createInsightRecord(currentInsightName + dateNow.substring(0, dateNow.indexOf(' ')), pipesDetectionImg, dateNow, timeNow, innerPipesCount, totalPipesCount, detectedPipesCount);

                // Close Dialog
                inptDialog.dismiss();
            }
        });

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
                            pipesDetectionImg = img;
                        }
                    }
                });

                retakeIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                setImageToView(img);
                pipesDetectionImg = img;
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
                                pipesDetectionImg = getBitMapImage(uri);
                            } catch (IOException e) {
                                Toast.makeText(PipesDetection.this, "Something went wrong in IMAGE.", Toast.LENGTH_SHORT).show();;
                            }
                        }
                    }
                });

                retakeIntent.setAction(MediaStore.ACTION_PICK_IMAGES);
                setImageToView(uri);
                pipesDetectionImg = getBitMapImage(uri);
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

    }

    protected void showEditInnerPipesDialog(){
        try{
            inptDialog.show();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            Log.d("error", e.toString());
        }
    }

    protected void createInsightRecord(String name, Bitmap img, String createdDate, String createdTime, int innerPipes, int totalPipes, int detectedPipes){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            //bitmap to byte[] stream
            img.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imgBytes = stream.toByteArray();

            // Insert data into the database
            dbHelper.onPipesAnalyze(name, imgBytes, createdDate, createdTime, innerPipes, totalPipes, detectedPipes);
        } catch (Exception e){
            Toast.makeText(this, "Something went wrong while storing!", Toast.LENGTH_SHORT).show();
        } finally {
            // Close the stream
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        detectedPipesCountLbl.setText(String.valueOf(detectedPipesCount));

        // Compute Total Pipes Count
        totalPipesCount = innerPipesCount * detectedPipesCount;

        // Show Total No. Of Pipes Count
        totalPipesCountLbl.setText(String.valueOf(totalPipesCount));
    }

    protected void setImageToView(Uri uri){ imgView.setImageURI(uri); }

    protected void setImageToView(Bitmap img){
        imgView.setImageBitmap(img);
    }
}
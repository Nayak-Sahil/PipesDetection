package com.kotharigroup.kotharipipes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class PipesDetection extends AppCompatActivity {

    ImageView imgView;
    LinearLayout reTakeBtn;
    Boolean isFromCapture;
    ActivityResultLauncher<Intent> launcher;
    Intent retakeIntent;
    Button cancelDialogBtn, analyzeDialogBtn;
    EditText innerPipesInptDialog;
    int innerPipesCount;
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

        imgView = findViewById(R.id.imgView);
        reTakeBtn = findViewById(R.id.reTakeBtn);
        retakeIntent = new Intent();

        Dialog inptDialog = new Dialog(PipesDetection.this);
        inptDialog.setContentView(R.layout.dialog);
        inptDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        try{
            inptDialog.show();
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            Log.d("error", e.toString());
        }

        cancelDialogBtn = inptDialog.findViewById(R.id.cancelDialogBtn);
        analyzeDialogBtn = inptDialog.findViewById(R.id.analyzeDialogBtn);
        innerPipesInptDialog = inptDialog.findViewById(R.id.innerPipesInptDialog);
        cancelDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inptDialog.dismiss();
            }
        });

        analyzeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                innerPipesCount = Integer.parseInt(innerPipesInptDialog.getText().toString());
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
                        }
                    }
                });
                retakeIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                setImageToView(img);
            } else if(uri != null) {
                isFromCapture = false;
                launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        if (o.getResultCode() == Activity.RESULT_OK) {
                            Intent data = o.getData();
                            setImageToView((Uri) data.getData());
                        }
                    }
                });

                retakeIntent.setAction(MediaStore.ACTION_PICK_IMAGES);
                setImageToView(uri);
            }
        }catch(Exception e){
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        // Recomputing again based on new image.
        reTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PipesDetection.this, "Clicked " + isFromCapture, Toast.LENGTH_SHORT).show();
                launcher.launch(retakeIntent);
            }
        });

    }

    protected void setImageToView(Uri uri){
        imgView.setImageURI(uri);
    }

    protected void setImageToView(Bitmap img){
        imgView.setImageBitmap(img);
    }
}
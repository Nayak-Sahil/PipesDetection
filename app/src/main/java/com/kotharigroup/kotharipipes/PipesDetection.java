package com.kotharigroup.kotharipipes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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
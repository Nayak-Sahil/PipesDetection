package com.kotharigroup.kotharipipes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {

    ImageButton optionBtn;
    Button viewHistoryBtn, takePhotoBtn, chooseImgBtn;
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

        optionBtn = findViewById(R.id.optionBtn);

        viewHistoryBtn = findViewById(R.id.viewHistoryBtn);
        takePhotoBtn = findViewById(R.id.takePhotoBtn);
        chooseImgBtn = findViewById(R.id.chooseImageBtn);

        optionBtn.setOnCreateContextMenuListener(this);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == Activity.RESULT_OK) {
                    Intent data = o.getData();
                    Bitmap img = (Bitmap) data.getExtras().get("data");
                    Intent detectionIntent = new Intent(getApplicationContext(), PipesDetection.class);
                    detectionIntent.putExtra("pipes_img", img);
                    startActivity(detectionIntent);
                }
            }
        });

        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraI = new Intent();
                cameraI.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                launcher.launch(cameraI);
            }
        });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }
}
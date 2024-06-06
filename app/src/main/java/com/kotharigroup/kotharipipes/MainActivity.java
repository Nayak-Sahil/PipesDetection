package com.kotharigroup.kotharipipes;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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
    }
}
package com.kotharigroup.kotharipipes;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;

public class History extends AppCompatActivity {
    DBHelper dbHelper;
    ListView insightRecordListView;
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

        Cursor c = dbHelper.getJustMetaData();
        if(c == null || !c.moveToFirst()){
            Toast.makeText(this, "No past activity found!", Toast.LENGTH_SHORT).show();
        }else{
            do {
                recordList.add(new InsightListView(c.getString(0), c.getString(1), c.getInt(2)));
            }while(c.moveToNext());

            Collections.reverse(recordList);
            InsightListAdapter listAdap = new InsightListAdapter(this, recordList);
            insightRecordListView.setAdapter(listAdap);
        }
    }
}
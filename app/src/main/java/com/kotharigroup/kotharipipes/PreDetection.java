package com.kotharigroup.kotharipipes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreDetection extends AppCompatActivity {

    EditText truckNumInpt, noteInpt, pipeSizeInpt, pipeCountInpt;
    LinearLayout copyFromLastRecordBtn, addPipeRecordBtn;
    Button analyzePipeBtn;
    TextView errorLbl, truckNumLbl, pipeDetailsLbl;
    Intent detectionIntent;
    List<Map<String, String>> pipesList;
    ListView pipeDataListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pre_detection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        truckNumInpt = findViewById(R.id.truckNumInpt);
        noteInpt = findViewById(R.id.noteInpt);
        pipeSizeInpt = findViewById(R.id.pipeSizeInpt);
        pipeCountInpt = findViewById(R.id.pipeCountInpt);
        errorLbl = findViewById(R.id.errorLbl);
        pipeDataListView = findViewById(R.id.pipeDataListView);
        truckNumLbl = findViewById(R.id.truckNumLbl);
        pipeDetailsLbl = findViewById(R.id.pipeDetailsLbl);

        copyFromLastRecordBtn = findViewById(R.id.copyFromLastRecordBtn);
        addPipeRecordBtn = findViewById(R.id.addPipeRecordBtn);
        analyzePipeBtn = findViewById(R.id.analyzeBtn);

        pipesList = new ArrayList<>();
        addPipeRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidMeasure()){
                    Map<String, String> map = new HashMap<>();

                    String pipeSize = pipeSizeInpt.getText().toString();
                    String pipeCount = pipeCountInpt.getText().toString();
                    map.put("pipeSize", pipeSize + " mm");
                    map.put("pipeCount", pipeCount);

                    pipesList.add(map);

                    setViewToList();
                }else{
                    Toast.makeText(PreDetection.this, getString(R.string.required_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        pipeDataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pipesList.remove(position);
                setViewToList();
            }
        });

        analyzePipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidData()){

                    Toast.makeText(PreDetection.this, "Please, wait for few seconds.", Toast.LENGTH_SHORT).show();
                    
                    detectionIntent = new Intent(getApplicationContext(), PipesDetection.class);

                    // 1. Add received image into next intent
                    try{
                        Uri uri = getIntent().getParcelableExtra("pipes_uri");
                        Bitmap img = getIntent().getParcelableExtra("pipes_img");
                        if(uri != null){
                            detectionIntent.putExtra("pipes_uri", uri);
                        }else{
                            detectionIntent.putExtra("pipes_img", img);
                        }
                    }catch (Exception e){
                        errorLbl.setText(getString(R.string.image_intent_error));
                        Toast.makeText(PreDetection.this, getString(R.string.image_intent_error), Toast.LENGTH_SHORT).show();
                    }

                    // 2. Add other filled metadata into next intent
                    detectionIntent.putExtra("truck_no", truckNumInpt.getText().toString());
                    detectionIntent.putExtra("extra_note", noteInpt.getText().toString());
                    detectionIntent.putExtra("pipes_datalist", (Serializable) pipesList);

                    startActivity(detectionIntent);
                }else{
                    errorLbl.setText(getString(R.string.required_error));
                    Toast.makeText(PreDetection.this, getString(R.string.required_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void setViewToList(){


        String[] from = {"pipeSize", "pipeCount"};

        // Ids of views in the layout (android.R.layout.simple_list_item_2)
        int[] to = {R.id.pipeSizeListLbl, R.id.pipeQuantListLbl};

        // Create the SimpleAdapter
        SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), pipesList, R.layout.input_parameter_list, from, to);

        // Set the adapter to the ListView
        pipeDataListView.setAdapter(adapter);
    }

    protected boolean isValidMeasure(){
        if(pipeSizeInpt.getText().length() == 0 || pipeCountInpt.getText().length() == 0){
            return false;
        }

        return true;
    }
    protected boolean isValidData(){
        if(truckNumInpt.getText().length() == 0){
            truckNumLbl.setTextColor(getColor(R.color.warning));
            return false;
        }
        truckNumLbl.setTextColor(getColor(R.color.primary_text));

        if(pipesList.isEmpty()){
            pipeDetailsLbl.setTextColor(getColor(R.color.warning));
            return false;
        }
        pipeDetailsLbl.setTextColor(getColor(R.color.primary_text));

        return true;
    }
}
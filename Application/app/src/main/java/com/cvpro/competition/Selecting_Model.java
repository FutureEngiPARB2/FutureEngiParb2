package com.cvpro.competition;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.cvpro.competition.ModelManagement.Model;
import com.cvpro.competition.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Selecting_Model extends AppCompatActivity  {
    ArrayList<Model> allModels;
    ArrayList<String> savedModels;
    ArrayList<String> cachemodels;
    SharedPreferences prefs;
    Spinner spModles;
    Button Back;
    public static String Label_teach,modelMngtName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecting_model);

        spModles = findViewById(R.id.spinner);
        Back = findViewById(R.id.button);
        Back.setOnClickListener(v -> {
            Toast.makeText(this, "Selected  "+ modelMngtName, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        });
        savedModels = new ArrayList<>();
        cachemodels = new ArrayList<>();
        spModles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String selectedItem = parent.getItemAtPosition(position).toString();

                    modelMngtName = selectedItem + ".tflite";
                    Log.d("Selected Model in Setting   ", modelMngtName);
                    Label_teach = selectedItem+".txt";
//                    Label_teach ="Autonomous.txt";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        allModels = new ArrayList<>();
        listModels();
    }

    public void listModels() {
        allModels.clear();
        prefs = getSharedPreferences("CV-PRO", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Model>>() {}.getType();
        if (prefs.getString("models", null) != null) {
            allModels = gson.fromJson(prefs.getString("models", null), type);
        }

        final ArrayList<String> modelsList = new ArrayList<>();
        String modelType = "Image Classification";
        File dir = getBaseContext().getCacheDir();
        if (dir.exists()) {
            for (File file: Objects.requireNonNull(dir.listFiles())) {
                Log.d("List Files:", String.valueOf(file));
                for (Model model: allModels) {
                    if ((model.getName().equals(file.getName()) && model.getType().equals(modelType)) || model.getType().equals("All")) {
                        modelsList.add(file.getName().replace(".tflite", ""));
                        Log.d("List Models:", String.valueOf(modelsList));
                    }
                }
            }
        }

        listAssetFiles("");
        listCacheFiles("");

        modelsList.addAll(savedModels);
        modelsList.addAll(cachemodels);
        Collections.sort(modelsList);

        ArrayAdapter<String> modelsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, modelsList);
        modelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spModles.setAdapter(modelsAdapter);

        Log.d("Model Path:", String.valueOf(modelsList));
        modelsAdapter.notifyDataSetChanged();
        Log.d("Selected ImageClassification:", String.valueOf(modelsAdapter));
    }

    @SuppressLint("NotifyDataSetChanged")
    private boolean listAssetFiles(String path) {
        String[] list;
        try {
            list = getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!listAssetFiles(path + "/" + file))
                        return false;
                    else {
                        // This is a file
                        // TODO: add file name to an array list
                        if (file.endsWith(".tflite")) {
                            Log.d("ASSET Files - Add the TFLITE :", file);
                            savedModels.add(file.replace(".tflite", ""));
                            allModels.add(new Model(file, "assets", "working_labels.txt", null, "All", 640, 480, false));
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private boolean listCacheFiles(String path) {
        File directory = new File(getCacheDir() + File.separator + path);
        if (!directory.exists()) {
            return false;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!listCacheFiles(path + File.separator + file.getName())) {
                        return false;
                    }
                } else {
                    if (file.getName().endsWith(".tflite")) {
                        Log.d("Cache Files - Add the TFLITE :", file.getName());
                        cachemodels.add(file.getName().replace(".tflite", ""));
                        allModels.add(new Model(file.getName(), " /data/user/0/com.cvpro.competition/cache/", "working_labels.txt", null, "All", 640, 480, false));
                        Log.d("Show the all models:", String.valueOf(allModels));
                    }
                }
            }
        }
        return true;
    }
}
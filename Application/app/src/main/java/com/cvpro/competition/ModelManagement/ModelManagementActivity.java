package com.cvpro.competition.ModelManagement;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.cvpro.competition.R;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

public class ModelManagementActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> startActivityForResult;
    Spinner spType;
    RelativeLayout btnAddModel;
    private ModelAdapter listAdapter;
    private ArrayList<Model> modelsList = new ArrayList<>();
    private RecyclerView rvModels;
    private String modelType = "All";
    SharedPreferences prefs;
    ArrayList<Model> allModels;
    private ActivityResultLauncher<Intent> mStartForResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_managment);

        spType = findViewById(R.id.sp_model_type);
        rvModels = findViewById(R.id.view_list_models);
        btnAddModel = findViewById(R.id.btn_add_model);
        allModels = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvModels.setLayoutManager(layoutManager);
        listAdapter = new ModelAdapter(modelsList, this);
        rvModels.setAdapter(listAdapter);
        rvModels.addItemDecoration(new DividerItemDecoration(rvModels.getContext(), DividerItemDecoration.VERTICAL));

        listFiles();
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Called Change Event:", "YES");
                modelType = spType.getSelectedItem().toString();
                listFiles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // modelType = spType.getSelectedItem().toString();
                // listFiles();
            }
        });

        startActivityForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        try {
                            Uri sUri = data.getData();
                            if (sUri != null) {
                                String path = sUri.getPath().replace("/external_files", "");

                                File cDir = getBaseContext().getCacheDir();
                                String displayName = path.substring(path.lastIndexOf("/") + 1).trim();
                                if (cDir.exists()) {
                                    for (File file : Objects.requireNonNull(cDir.listFiles())) {
                                        if (file.getName().equals(displayName)) {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(this);
                                            alert.setTitle("File Already Added");
                                            alert.setMessage("This file has already been added.");
                                            alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // continue with delete
                                                }
                                            });
                                            alert.show();
                                            return;
                                        }
                                    }
                                }
                                startActivity(new Intent(ModelManagementActivity.this, AddModelActivity.class).putExtra("selected_file", path));
                            }
                        } catch (Exception ex) {
                            Log.d("Exception :", ex.toString());
                        }
                    }
                }
            }
        );

        mStartForResult =
            registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the Intent
                        if (data != null) {
                            try {
                                Uri sUri = data.getData();
                                if (sUri != null) {
                                    String path = sUri.getPath().replace("/external_files", "");

                                    File cDir = getBaseContext().getCacheDir();
                                    String displayName = path.substring(path.lastIndexOf("/") + 1).trim();
                                    if (cDir.exists()) {
                                        for (File file : Objects.requireNonNull(cDir.listFiles())) {
                                            if (file.getName().equals(displayName)) {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                                                alert.setTitle("File Already Added");
                                                alert.setMessage("This file has already been added.");
                                                alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // continue with delete
                                                    }
                                                });
                                                alert.show();
                                                return;
                                            }
                                        }
                                    }
                                    startActivity(new Intent(ModelManagementActivity.this, AddModelActivity.class).putExtra("selected_file", path));
                                }
                            } catch (Exception ex) {
                                Log.d("Exception :", ex.toString());
                            }
                        }
                    }
                }
            );

        btnAddModel.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        //chooseFile.setType("text/plain");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult.launch(chooseFile);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void listFiles() {
        modelsList.clear();
        allModels.clear();
        prefs = getSharedPreferences("CV-PRO", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Model>>() {}.getType();
        if (prefs.getString("models", null) != null) {
            allModels = gson.fromJson(prefs.getString("models", null), type);
        }
        Log.d("List Files:", "Called");

        File dir = getBaseContext().getCacheDir();
        if (dir.exists()) {
            for (File file: Objects.requireNonNull(dir.listFiles())) {
                if (modelType.equals("All")) {
                    modelsList.add(new Model(file.getName().replace(".tflite", ""), null, file.getName().replace(".tflite", "") + "-label.txt", null,  null, 0, 0, true));
                } else {
                    Log.d("Cache Model Size:", String.valueOf(allModels.size()));
                    for (Model model: allModels) {
                        Log.d("Cache Model:", model.getName() + " ---------> " + model.getType());
                        if ((model.getName().equals(file.getName()) && model.getType().equals(modelType)) || model.getType().equals("All")) {
                            modelsList.add(new Model(file.getName().replace(".tflite", ""), null, file.getName().replace(".tflite", "") + "-label.txt", null, null, 0, 0, true));
                        }
                    }
                }
            }
        }
        listAssetFiles("");
        listAdapter.notifyDataSetChanged();
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
                            Log.d("TFLITE:", file);
                            modelsList.add(new Model(file.replace(".tflite", ""), null, file.replace(".tflite", "") + "-label.txt", null, null, 0, 0, false));
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        listFiles();
    }
}
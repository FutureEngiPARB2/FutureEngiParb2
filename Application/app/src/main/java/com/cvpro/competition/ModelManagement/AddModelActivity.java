package com.cvpro.competition.ModelManagement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.cvpro.competition.R;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;

public class AddModelActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> startActivityForResult;
    TextView tvFileName, tvLabelFileName;
    ImageView icLabelAttachment;
    Button btnCancel, btnDone;
    EditText etWidth, etHeight;
    Spinner spType;
    SharedPreferences prefs;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_model);

        tvFileName = findViewById(R.id.tv_add_model_file_name);
        tvLabelFileName = findViewById(R.id.tv_add_model_file_label);
        icLabelAttachment = findViewById(R.id.ic_label_attachment);
        spType = findViewById(R.id.sp_add_model_type);
        etWidth = findViewById(R.id.et_width_add_model);
        etHeight = findViewById(R.id.et_height_add_model);
        btnCancel = findViewById(R.id.btn_add_model_cancel);
        btnDone = findViewById(R.id.btn_add_model_done);

        etWidth.setText("640");
        etHeight.setText("480");

        icLabelAttachment.setOnClickListener(view -> openFilePicker());

        prefs = getSharedPreferences("CV-PRO", Context.MODE_PRIVATE);

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE
                }, 1
        );
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("selected_file");

        File myFile = new File(Environment.getExternalStorageDirectory(), filePath);
        String displayName = filePath.substring(filePath.lastIndexOf("/") + 1).trim();

        tvFileName.setText(displayName);
        Log.d("Selected File", displayName);

        btnCancel.setOnClickListener(v -> onBackPressed());

        btnDone.setOnClickListener(v -> {
            if (spType.getSelectedItem().toString().equals("Choose")) {
                Toast.makeText(this, "Select Model Type", Toast.LENGTH_LONG).show();
                return;
            }

            if (etWidth.getText().toString().isEmpty() || etHeight.getText().toString().isEmpty()) {
                Toast.makeText(this, "Enter Size", Toast.LENGTH_LONG).show();
                return;
            }

            String modelType = spType.getSelectedItem().toString();
            int width = Integer.parseInt(etWidth.getText().toString());
            int height = Integer.parseInt(etHeight.getText().toString());

            if (modelType.equals("Choose")) {
                Toast.makeText(AddModelActivity.this, "Select Model Type", Toast.LENGTH_SHORT).show();
                return;
            }

            File cDir = getBaseContext().getCacheDir();
            try {
                File destinationFile = new File(cDir, displayName);
//                boolean fileCreated = destinationFile.createNewFile();
                if (!destinationFile.exists()) {
                    Log.d("File Exists:", "NO");
                    return;
                } else {
                    String labelFileName = displayName.replace(".tflite", "") + "-label.txt";
                    File destinationLabelFile = new File(cDir, labelFileName);
//                    boolean labelFileCreated = destinationLabelFile.createNewFile();
                    if (!destinationLabelFile.exists()) {
                        return;
                    }
                    copyContent(myFile, destinationFile);
                    Toast.makeText(getApplicationContext(), "Model Added", Toast.LENGTH_SHORT).show();

                    ArrayList<Model> modelsList = new ArrayList<>();

                    SharedPreferences.Editor editor = prefs.edit();
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<Model>>() {
                    }.getType();
                    if (prefs.getString("models", null) != null) {
                        modelsList = gson.fromJson(prefs.getString("models", null), type);
                    }
                    modelsList.add(new Model(displayName, null, labelFileName, null, modelType, width, height, true));
                    String json = gson.toJson(modelsList);
                    editor.putString("models", json);
                    editor.apply();

                    Log.d("Added Model Size:", String.valueOf(modelsList.size()));

                    onBackPressed();
                }
            } catch (Exception e) {
                Log.d("File Exception:", e.toString());
                e.printStackTrace();
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
                                String labelFilePath = sUri.getPath().replace("/external_files", "");
                                String labelDisplayName = labelFilePath.substring(labelFilePath.lastIndexOf("/") + 1).trim();
                                tvLabelFileName.setText(labelDisplayName);
                            }
                        } catch (Exception ex) {
                            Log.d("Exception :", ex.toString());
                        }
                    }
                }
            }
        );
    }

    private void copyContent(File source, File dest) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = Files.newInputStream(source.toPath());
            os = Files.newOutputStream(dest.toPath());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception ex) {
            Log.d("File Copy Exception:", ex.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void openFilePicker() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("text/plain");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult.launch(chooseFile);
    }
}

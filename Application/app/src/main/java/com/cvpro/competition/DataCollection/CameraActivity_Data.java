package com.cvpro.competition.DataCollection;

import static android.os.Build.VERSION.SDK_INT;
import static com.cvpro.competition.DataCollection.DataCollection.msg;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.cvpro.competition.Camera_Usage.CameraConnectionFragment;
import com.cvpro.competition.Camera_Usage.ImageUtils;
import com.cvpro.competition.MainActivity;
import com.cvpro.competition.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity_Data extends AppCompatActivity implements ImageReader.OnImageAvailableListener{
    Button stop;
    AlertDialog.Builder builder;
    static int count = 0;
    int n = 0;
    static ExecutorService threadPool;
    Switch swit,zip;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_data);
        stop = findViewById(R.id.btDisconnect);
        TextView textView = findViewById(R.id.tvac);
        textView.setText(msg);
        threadPool = Executors.newFixedThreadPool(2);
        final MediaPlayer logon = MediaPlayer.create(this, R.raw.datastart);
        final MediaPlayer logff = MediaPlayer.create(this, R.raw.datastop);

        zip= findViewById(R.id.zip);

        zip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                if (isChecked) {
                    ZipFiles.main();
                    Toast.makeText(this, "Zip Finished", Toast.LENGTH_SHORT).show();
                } else {
                    DeleteDirectory.main();
                    Toast.makeText(this, "Folder Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
            } catch (NullPointerException e) {
                // Handle the NullPointerException and show a Toast message
                Toast.makeText(this, "Oops! Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();

                // Turn off the switch if an error occurs
                zip.setChecked(false);
            }
        });


        swit = findViewById(R.id.logSwitch);
        swit.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                logon.start();
                count++;
            } else {
                logff.start();
                n=0;
            }
        });

        builder = new AlertDialog.Builder(this);
        stop.setOnClickListener(view -> {

            builder.setMessage("Do you want to close the Data Collection ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

//                            finish();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("EXIT", true);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        });

        //TODO ask for camera permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA}, 121);
            } else {
                //TODO show live camera footage
                setFragment();
            }
        } else {
            setFragment();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //TODO show live camera footage
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //TODO show live camera footage
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    setFragment();

                } else {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", CameraActivity_Data.this.getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
            setFragment();

        } else {
            finish();
        }
    }

    //TODO fragment which show live footage from camera
    int previewHeight = 0, previewWidth = 0;
    int sensorOrientation;

    protected void setFragment() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        CameraConnectionFragment fragment;
        CameraConnectionFragment camera2Fragment =
                CameraConnectionFragment.newInstance(
                        (size, rotation) -> {
                            previewHeight = size.getHeight();
                            previewWidth = size.getWidth();
                            Log.d("tryOrientation", "rotation: " + rotation + "   orientation: " + getScreenOrientation() + "  " + previewWidth + "   " + previewHeight);
                            sensorOrientation = rotation - getScreenOrientation();
                        },
                        this,
                        R.layout.camera_fragment,
                        new Size(640, 480));

        camera2Fragment.setCamera(cameraId);
        fragment = camera2Fragment;
        getFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, fragment).commit();
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
            case Surface.ROTATION_0:
                break;
        }
        return 0;
    }

    //TODO getting frames of live camera footage and passing them to model
    private boolean isProcessingFrame = false;
    private final byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private Bitmap rgbFrameBitmap;

    public void onImageAvailable(ImageReader reader) {
        // We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            if (isProcessingFrame) {
                image.close();
                return;
            }
            isProcessingFrame = true;
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                    () -> ImageUtils.convertYUV420ToARGB8888(
                            yuvBytes[0],
                            yuvBytes[1],
                            yuvBytes[2],
                            previewWidth,
                            previewHeight,
                            yRowStride,
                            uvRowStride,
                            uvPixelStride,
                            rgbBytes);

            postInferenceCallback =
                    () -> {
                        image.close();
                        isProcessingFrame = false;
                    };

            processImage();

        } catch (final Exception e) {
            Log.d("tryError", e.getMessage());

        }
    }

    private void processImage() {
        imageConverter.run();
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        //Do your work here

        if (swit.isChecked()) {
            System.out.println("Switch is ON");
            SaveImage(rgbFrameBitmap);
            threadPool.execute(new TextThread());
        }
        postInferenceCallback.run();
    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    public static String image_path;
    private void SaveImage(Bitmap finalBitmap) {
        //long timestamp = SystemClock.elapsedRealtime();
        File root = new File(Environment.getExternalStorageDirectory() + File.separator + "CVPRO_Competition/Dataset/" + count + "/Images");
        root.mkdirs();
        String fname = "Image-" + n + ".jpg";
        n++;
        File file = new File(root, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        image_path = fname;
        Log.i("image path", image_path);
    }

    static class TextThread implements Runnable {
        @Override
        public void run() {
            File formatter = new File("Frame");
            //long timestamp = SystemClock.elapsedRealtimeNanos();
            String fileName = formatter + ".csv";//like 2016_01_12.txt
            try {
                String cv_path = "CVPRO_Competition/" + "Dataset/";
                File root = new File(Environment.getExternalStorageDirectory() + File.separator + cv_path + count, "Files");
                if (!root.exists()) {
                    root.mkdirs();
                }
                File text_file = new File(root, fileName);
                FileWriter writer = new FileWriter(text_file, true);
                if (Objects.equals(msg, "o")) {
                    writer.flush();
                } else
                    writer.append(String.valueOf(count)).append("/Images/").append(image_path).append(",").append(msg).append("\n");                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

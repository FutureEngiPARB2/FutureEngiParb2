package com.cvpro.competition.ImageClassification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import static com.cvpro.competition.ImageClassification.Image_Classification.firstelement;
import static com.cvpro.competition.MainActivity.usbService;
import static com.cvpro.competition.Selecting_Model.modelMngtName;
import com.cvpro.competition.ImageClassification.env.ImageUtils;
import com.cvpro.competition.ImageClassification.env.Logger;
import com.cvpro.competition.Selecting_Model;
import com.cvpro.competition.R;
import com.cvpro.competition.tflite.Classifier.Device;
import com.cvpro.competition.tflite.Classifier.Model;
import com.cvpro.competition.tflite.Classifier.Recognition;
import java.nio.ByteBuffer;
import java.util.List;

public abstract class CameraActivity extends AppCompatActivity
        implements OnImageAvailableListener,
        Camera.PreviewCallback,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener{
    private static final Logger LOGGER = new Logger();
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    protected int previewWidth = 640;
    protected int previewHeight = 480;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean useCamera2API;
    private boolean isProcessingFrame = false;
    private final byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    protected TextView tvObject, tvAccuracy;
    private ImageView plusImageView, minusImageView;
    private Spinner modelSpinner;
    private Spinner deviceSpinner;
    private TextView threadsTextView;
    private Model model = Model.FLOAT;
    private Device device = Device.CPU;
    private int numThreads = -1;
    private boolean flash = false;
    private boolean frontCamera = false;
    Thread usbThread = null;
    ImageButton model_setting;
    TextView showingvalues;
    private TextView tvMessage;
    public static Recognition recognition ;
    public static ToggleButton cameraSwitch, flashLightSwitch;
    private boolean offButtonPressed = false;
    private volatile boolean usbThreadRunning = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        LOGGER.d("onCreate " + this);
        super.onCreate(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_second_challenge);
        tvMessage = findViewById(R.id.txt);

        if (hasPermission()) {
            setFragment();
        } else {
            requestPermission();
        }

        showingvalues = findViewById(R.id.modelnamesgetting);
        showingvalues.setText(modelMngtName);
        threadsTextView = findViewById(R.id.threads);
        plusImageView = findViewById(R.id.plus);
        minusImageView = findViewById(R.id.minus);
        modelSpinner = findViewById(R.id.model_spinner);
        deviceSpinner = findViewById(R.id.device_spinner);
        cameraSwitch = findViewById(R.id.cameraSwitchImageClassification);
        flashLightSwitch = findViewById(R.id.flashlightSwitchImageClassification);

        cameraSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            frontCamera = compoundButton.isChecked();
            setFragment();
        });
        flashLightSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            flash = compoundButton.isChecked();
            setFragment();
        });

        tvObject = findViewById(R.id.objectText);
        tvAccuracy = findViewById(R.id.accuracyText);

        modelSpinner.setOnItemSelectedListener(this);

        model = Model.valueOf(modelSpinner.getSelectedItem().toString().toUpperCase());
        device = Device.valueOf(deviceSpinner.getSelectedItem().toString());
        numThreads = Integer.parseInt(threadsTextView.getText().toString().trim());

        model_setting = findViewById(R.id.img1);
        model_setting.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Selecting_Model.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        });

        Button onButton = findViewById(R.id.ON);
        Button offButton = findViewById(R.id.OFF);

        onButton.setOnClickListener(v -> {
            // Handle the ON button click event
            // Start sending data via USB
            startSendingData();
        });

        offButton.setOnClickListener(v -> {
            // Handle the OFF button click event
            // Stop sending data via USB and send "0,0"
            stopSendingData();
        });
    }

    private void startSendingData() {
        // Start a thread to continuously send data through USB
        usbThreadRunning = true;
        usbThread = new Thread(() -> {
            while (usbThreadRunning) {
                synchronized (usbService) {
                    // Check if the OFF button was pressed
                    if (!offButtonPressed) {
                        usbService.write(firstelement.getBytes());
                    }
                }
                Log.d("USB Values", firstelement);
                try {
                    Thread.sleep(100); // Adjust the sleep duration as needed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(() -> tvMessage.setText(firstelement));
            }
        });
        usbThread.start();
    }

    private void stopSendingData() {
        // Set the flag to signal the usbThread to stop
        usbThreadRunning = false;
        // Send "0,0" through USB
        synchronized (usbService) {
            String end_process = "0,0";
            usbService.write(end_process.getBytes());
        }
        runOnUiThread(() -> tvMessage.setText("0,0"));
        // Wait for the usbThread to finish
        try {
            if (usbThread != null && usbThread.isAlive()) {
                usbThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            LOGGER.w("Dropping frame!");
            return;
        }
        try {
            // Initialize the storage bitmaps once when the resolution is known.
            if (rgbBytes == null) {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                rgbBytes = new int[previewWidth * previewHeight];
                onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
            }
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            return;
        }
        isProcessingFrame = true;
        yuvBytes[0] = bytes;
        yRowStride = previewWidth;

        imageConverter =
            new Runnable() {
                @Override
                public void run() {
                    ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
                }
            };

        postInferenceCallback =
            new Runnable() {
                @Override
                public void run() {
                    camera.addCallbackBuffer(bytes);
                    isProcessingFrame = false;
                }
            };
        processImage();
    }

    /**
     * Callback for Camera2 API
     */
    @Override
    public void onImageAvailable(final ImageReader reader) {
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
            Trace.beginSection("imageAvailable");
            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();

            imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420ToARGB8888(
                                yuvBytes[0],
                                yuvBytes[1],
                                yuvBytes[2],
                                previewWidth,
                                previewHeight,
                                yRowStride,
                                uvRowStride,
                                uvPixelStride,
                                rgbBytes);
                    }
                };

            postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        image.close();
                        isProcessingFrame = false;
                    }
                };
            processImage();
        } catch (final Exception e) {
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }
        Trace.endSection();
    }

    @Override
    public synchronized void onStart() {
        LOGGER.d("onStart " + this);
        super.onStart();
    }

    @Override
    public synchronized void onResume() {
        LOGGER.d("onResume " + this);
        super.onResume();
        try {
            handlerThread = new HandlerThread("inference");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onPause() {
        LOGGER.d("onPause " + this);
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            LOGGER.e(e, "Exception!");
        }
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public synchronized void onStop() {
        LOGGER.d("onStop " + this);
        super.onStop();
    }

    @Override
    public synchronized void onDestroy() {
        LOGGER.d("onDestroy " + this);
        super.onDestroy();
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try{
            if (requestCode == PERMISSIONS_REQUEST) {
                if (allPermissionsGranted(grantResults)) {
                    setFragment();
                } else {
                    requestPermission();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean allPermissionsGranted(final int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission() {
        return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
            Toast.makeText(
                    CameraActivity.this,
                    "Camera permission is required for this demo",
                    Toast.LENGTH_LONG)
            .show();
        }
        requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }

    // Returns true if the device supports the required hardware level, or better.
    private boolean isHardwareLevelSupported(
            CameraCharacteristics characteristics, int requiredLevel) {
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
            return requiredLevel == deviceLevel;
        }
        // deviceLevel is not LEGACY, can use numerical sort
        return requiredLevel <= deviceLevel;
    }

    private String chooseCamera() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId: manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                final Integer facing;
                facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    if (!frontCamera) {
                        continue;
                    }
                }

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    if (frontCamera) {
                        continue;
                    }
                }

                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (map == null) {
                    continue;
                }
                // Fallback to camera1 API for internal cameras that don't have full support.
                // This should help with legacy situations where using the camera2 API causes
                // distorted or otherwise broken previews.
                useCamera2API =
                        (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                                || isHardwareLevelSupported(
                                characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
                LOGGER.i("Camera API lv2?: %s", useCamera2API);
                return cameraId;
            }
        } catch (CameraAccessException e) {
            LOGGER.e(e, "Not allowed to access camera");
        }
        return null;
    }

    protected void setFragment() {
        String cameraId = chooseCamera();
        Fragment fragment;
        if (useCamera2API) {
            CameraConnectionFragment camera2Fragment =
                    CameraConnectionFragment.newInstance(
                            new CameraConnectionFragment.ConnectionCallback() {
                                @Override
                                public void onPreviewSizeChosen(final Size size, final int rotation) {
                                    previewHeight = size.getHeight();
                                    previewWidth = size.getWidth();
                                    CameraActivity.this.onPreviewSizeChosen(size, rotation);
                                }
                            },
                            this,
                            getLayoutId(),
                            getDesiredPreviewFrameSize(),
                            flash,
                            frontCamera);

            camera2Fragment.setCamera(cameraId);
            fragment = camera2Fragment;
        } else {
            fragment =
                    new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
        }

        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
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
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @UiThread
    protected void showResultsInBottomSheet(List<Recognition> results) {

        if (results != null && results.size() >= 2) {
            recognition = results.get(0);
            if (recognition != null) {
                if (recognition.getTitle() != null)
                    tvObject.setText(recognition.getTitle());

                if (recognition.getConfidence() != null)
                    tvAccuracy.setText(String.format("%.2f", (100 * recognition.getConfidence())) + "%");

                Log.d("Showing", recognition.getTitle());
            }

        }
    }

    protected void showFrameInfo(String frameInfo) {
        //frameValueTextView.setText(frameInfo);
    }

    protected void showCropInfo(String cropInfo) {
        //cropValueTextView.setText(cropInfo);
    }

    protected void showCameraResolution(String cameraInfo) {
        //cameraResolutionTextView.setText(cameraInfo);
    }
    protected void showRotationInfo(String rotation) {
        //rotationTextView.setText(rotation);
    }
    protected void showInference(String inferenceTime) {
        //inferenceTimeTextView.setText(inferenceTime);
    }

    protected Model getModel() {
        return model;
    }

    private void setModel(Model model) {
        if (this.model != model) {
            LOGGER.d("Updating  model: " + model);
            this.model = model;
            onInferenceConfigurationChanged();
        }
    }

    protected Device getDevice() {
        return device;
    }

    private void setDevice(Device device) {
        if (this.device != device) {
            LOGGER.d("Updating  device: " + device);
            this.device = device;
            final boolean threadsEnabled = device == Device.CPU;
            plusImageView.setEnabled(threadsEnabled);
            minusImageView.setEnabled(threadsEnabled);
            threadsTextView.setText(threadsEnabled ? String.valueOf(numThreads) : "N/A");
            onInferenceConfigurationChanged();
        }
    }

    protected int getNumThreads() {
        return numThreads;
    }

    private void setNumThreads(int numThreads) {
        if (this.numThreads != numThreads) {
            LOGGER.d("Updating  numThreads: " + numThreads);
            this.numThreads = numThreads;
            onInferenceConfigurationChanged();
        }
    }

    protected abstract void processImage();

    protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

    protected abstract int getLayoutId();

    protected abstract Size getDesiredPreviewFrameSize();

    protected abstract void onInferenceConfigurationChanged();

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.plus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads >= 9) return;
            setNumThreads(++numThreads);
            threadsTextView.setText(String.valueOf(numThreads));
        } else if (v.getId() == R.id.minus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads == 1) {
                return;
            }
            setNumThreads(--numThreads);
            threadsTextView.setText(String.valueOf(numThreads));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == modelSpinner) {
            setModel(Model.valueOf(parent.getItemAtPosition(pos).toString().toUpperCase()));
        } else if (parent == deviceSpinner) {
            setDevice(Device.valueOf(parent.getItemAtPosition(pos).toString()));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }
}



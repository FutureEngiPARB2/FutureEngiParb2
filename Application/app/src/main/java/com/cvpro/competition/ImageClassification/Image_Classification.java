package com.cvpro.competition.ImageClassification;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.ImageReader;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;
import com.cvpro.competition.ImageClassification.env.Logger;
import com.cvpro.competition.ImageClassification.env.BorderedText;
import com.cvpro.competition.R;
import com.cvpro.competition.tflite.Classifier;
import com.cvpro.competition.tflite.Classifier.Model;
import java.io.IOException;
import java.util.List;

public class Image_Classification extends CameraActivity implements ImageReader.OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();
    private static final Size DESIRED_PREVIEW_SIZE = new Size(320, 240);
    private static final float TEXT_SIZE_DIP = 10;
    private Bitmap rgbFrameBitmap = null;
    private long lastProcessingTimeMs;
    private Integer sensorOrientation;
    public Classifier classifier;
    private BorderedText borderedText;

    /** Input image size of the model along x axis. */
    private int imageSizeX;
    /** Input image size of the model along y axis. */
    private int imageSizeY;
    public static String out,in ,firstelement;

    @Override
    protected int getLayoutId() {return R.layout.camera_fragment;}
    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        recreateClassifier(getModel(), getDevice(), getNumThreads());
        if (classifier == null) {
            LOGGER.e("No classifier on preview!");
            return;
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);
        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final int cropSize = Math.min(previewWidth, previewHeight);
        runInBackground(
            () -> {
                if (classifier != null) {
                    final long startTime = SystemClock.uptimeMillis();
                    final List<Classifier.Recognition> results =
                            classifier.recognizeImage(rgbFrameBitmap, sensorOrientation);
                    lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                    out = String.valueOf(results);
                    Log.d("Process Image",out);
                    int startindex = out.indexOf("[")+2;
                    int endindex= out.indexOf("]");
                    firstelement = out.substring(startindex,endindex);
                    Log.d("Final Out",firstelement);
                    runOnUiThread(
                        () -> {
                            showResultsInBottomSheet(results);
                            showFrameInfo(previewWidth + "x" + previewHeight);
                            showCropInfo(imageSizeX + "x" + imageSizeY);
                            showCameraResolution(cropSize + "x" + cropSize);
                            showRotationInfo(String.valueOf(sensorOrientation));
                            showInference(lastProcessingTimeMs + "ms");
                        });
                }
                readyForNextImage();
            });
    }

    @Override
    protected void onInferenceConfigurationChanged() {
        if (rgbFrameBitmap == null) {
            // Defer creation until we're getting camera frames.
            return;
        }
        final Classifier.Device device = getDevice();
        final Model model = getModel();
        final int numThreads = getNumThreads();
        runInBackground(() -> recreateClassifier(model, device, numThreads));
    }

    private void recreateClassifier(Model model, Classifier.Device device, int numThreads) {
        if (classifier != null) {
            LOGGER.d("Closing classifier.");
            classifier.close();
            classifier = null;
        }
        if (device == Classifier.Device.GPU && model == Model.QUANTIZED) {
            LOGGER.d("Not creating classifier: GPU doesn't support quantized models.");
            runOnUiThread(
                    () -> {
                        Toast.makeText(this, "GPU does not yet supported quantized models.", Toast.LENGTH_LONG)
                                .show();
                    });
            return;
        }
        try {
            LOGGER.d(
                    "Creating classifier (model=%s, device=%s, numThreads=%d)", model, device, numThreads);
            classifier = Classifier.create(this, model, device, numThreads);
            // Updates the input image size.
            imageSizeX = classifier.getImageSizeX();
            imageSizeY = classifier.getImageSizeY();
        } catch (IOException e) {
            Log.d("Classifier Error : ", "Not Created");
            LOGGER.e(e, "Failed to create classifier.");
        } catch (Exception ex) {
            Log.d("Exception :", "Not Created");
        }
    }
}
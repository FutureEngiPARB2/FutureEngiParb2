package com.cvpro.competition.tflite;

import android.app.Activity;
import android.util.Log;
import com.cvpro.competition.Selecting_Model;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import java.io.IOException;

/** This TensorFlowLite classifier works with the float MobileNet model. */
public class ClassifierFloatMobileNet extends Classifier {

    /** Float MobileNet requires additional normalization of the used input. */
    private static final float IMAGE_MEAN = 127.5f;

    private static final float IMAGE_STD = 127.5f;

    /**
     * Float model does not need dequantization in the post-processing. Setting mean and std as 0.0f
     * and 1.0f, repectively, to bypass the normalization.
     */
    private static final float PROBABILITY_MEAN = 0.0f;

    private static final float PROBABILITY_STD = 1.0f;

    /**
     * Initializes a {@code ClassifierFloatMobileNet}.
     *
     * @param activity
     */
    public ClassifierFloatMobileNet(Activity activity ,Device device, int numThreads)
            throws IOException {
        super(activity, device, numThreads);
    }

    @Override

    protected @NonNull String getModelPath() {

        Log.d("Not Quantized","modelManagementName");

        return Selecting_Model.modelMngtName;
    }

    @Override
    protected String getLabelPath() {
        return Selecting_Model.Label_teach;
    }

    @Override
    protected TensorOperator getPreprocessNormalizeOp() {
        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
    }

    @Override
    protected TensorOperator getPostprocessNormalizeOp() {
        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
    }
}


package com.google.mlkit.vision.demo.java.posedetector.posevalidator;

public interface PoseValidationResultListener {

    void onValidationError(int count);

    void onTimerStart(int count);

    void onPoseComplete(int count);
}

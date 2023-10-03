package com.google.mlkit.vision.demo.java.posedetector.posevalidator;

import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.Pose1Analyzer;
import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.PoseAnalyzer;
import com.google.mlkit.vision.demo.java.texttospeech.TTSMessageListener;

public abstract class PoseValidator {

    protected final PoseAnalyzer poseAnalyzer;
    protected final TTSMessageListener ttsMessageListener;

    protected final float horizontalViewAngle;
    protected final float verticalViewAngle;
    protected final float focalLength;



    public PoseValidator(PoseAnalyzer poseAnalyzer, TTSMessageListener ttsMessageListener, float horizontalViewAngle, float verticalViewAngle, float focalLength) {
        this.poseAnalyzer = poseAnalyzer;
        this.ttsMessageListener = ttsMessageListener;
        this.horizontalViewAngle = horizontalViewAngle;
        this.verticalViewAngle = verticalViewAngle;
        this.focalLength = focalLength;
    }

    public abstract boolean validatePose();
}

package com.google.mlkit.vision.demo.java.posedetector.posevalidator;

import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.Pose1Analyzer;
import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.PoseAnalyzer;
import com.google.mlkit.vision.demo.java.texttospeech.TTSMessageListener;

public class Pose1Validator extends PoseValidator {

    public  final PoseValidationResultListener poseValidationResultListener;
    private int timerCount;

    public Pose1Validator(PoseAnalyzer poseAnalyzer, TTSMessageListener ttsMessageListener, float horizontalViewAngle, float verticalViewAngle, float focalLength, PoseValidationResultListener poseValidationResultListener, int timerCount) {
        super(poseAnalyzer, ttsMessageListener, horizontalViewAngle, verticalViewAngle, focalLength);
        this.poseValidationResultListener = poseValidationResultListener;
        this.timerCount =timerCount;
    }

    public boolean validatePose() {
        double leftBodyAngle = poseAnalyzer.leftBodyAngle();
        double rightBodyAngle = poseAnalyzer.rightBodyAngle();

        double distanceBetweenCameraAndPersonInFeet = poseAnalyzer.distanceBetweenCameraAndPerson(horizontalViewAngle, verticalViewAngle, focalLength);
        double distanceBetweenLegs = poseAnalyzer.distanceBetweenLegs(distanceBetweenCameraAndPersonInFeet);

        if (!poseAnalyzer.isWholeBodyVisible()) {
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("Your full body is not visible in the camera screen. Please step back.", false);
            return false;
        }

        if(distanceBetweenCameraAndPersonInFeet < 4){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("You are standing too close to the device. Maintain a distance of 5-6 feet from the camera.", false);
            return false;
        }

        if(distanceBetweenCameraAndPersonInFeet > 6){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("You are too far from the device. Stand closer to the camera within the 5-6 feet range.", false);
            return false;
        }

        if(distanceBetweenLegs < 11){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("Legs are too close together. Please spread your legs apart by approximately 1 foot.", false);
            return false;
        }

        if(!poseAnalyzer.areLegsStraight()){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("Legs are not straight. Please straighten both legs, ensuring no bending at the knees.", false);
            return false;
        }

        if(!poseAnalyzer.areHandsStraight()){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("The hands are not straight. Please straighten both hands.", false);
            return false;
        }

        if(leftBodyAngle > 50 || rightBodyAngle >50){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("Hands are not at a 45-degree angle. Please lower hands slightly.", false);
            return false;
        }

        if(leftBodyAngle < 40 || rightBodyAngle < 40){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("Hands are not at a 45-degree angle. Please raise hands slightly.", false);
            return false;
        }

        if(timerCount > 0){
            ttsMessageListener.messageReceived(String.valueOf(timerCount), true);
            poseValidationResultListener.onTimerStart(timerCount);
            return false;
        }

        if(timerCount == 0){
            ttsMessageListener.messageReceived("First Image Captured successfully. Now, turn 90 degrees to capture second", true);
            poseValidationResultListener.onPoseComplete(5);
            return true;
        }

        return true;
    }
}



package com.google.mlkit.vision.demo.java.posedetector.posevalidator;

import android.util.Log;

import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.Pose2Analyzer;
import com.google.mlkit.vision.demo.java.texttospeech.TTSMessageListener;

public class Pose2Validator extends PoseValidator{

    public  final PoseValidationResultListener poseValidationResultListener;
    private int timerCount;

    public Pose2Validator(Pose2Analyzer poseAnalyzer, TTSMessageListener ttsMessageListener, float horizontalViewAngle, float verticalViewAngle, float focalLength, PoseValidationResultListener poseValidationResultListener, int timerCount) {
        super(poseAnalyzer, ttsMessageListener,horizontalViewAngle, verticalViewAngle, focalLength);
        this.poseValidationResultListener = poseValidationResultListener;
        this.timerCount = timerCount;
    }

    @Override
    public boolean validatePose() {

        /*
  validate if there is a human in the frame - if fails TTS an error
- validate if all body parts are visible in frame - if fails TTS an error
- validate human is standing within 4-6 feet range - if fails TTS an error
- validate human hands are close to the body - if fails TTS an error
- validate legs are close
- validate left shoulder and right shoulder are in same line.
         */

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

//        if(distanceBetweenCameraAndPersonInFeet > 8){
//            poseValidationResultListener.onValidationError(5);
//            ttsMessageListener.messageReceived("You are too far from the device. Stand closer to the camera within the 5-6 feet range.", false);
//            return false;
//        }

        if(!poseAnalyzer.isPersonTurned90Degree()){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("Please make sure you have turned 90 degree angle", false);
            return false;
        }

        if(distanceBetweenLegs > 10){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("Legs are not close together. Please keep your legs close.", false);
            return false;
        }

        if(!poseAnalyzer.areHandsStraight()){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("The hands are not straight. Please straighten both hands.", false);
            return false;
        }

        if(!poseAnalyzer.areLegsStraight()){
            poseValidationResultListener.onValidationError(5);
            ttsMessageListener.messageReceived("Legs are not straight. Please straighten both legs, ensuring no bending at the knees.", false);
            return false;
        }

        if(timerCount > 0){
            ttsMessageListener.messageReceived(String.valueOf(timerCount), true);
            poseValidationResultListener.onTimerStart(timerCount);
            return false;
        }

        if(timerCount == 0){
            ttsMessageListener.messageReceived("Pose 2 Complete", true);
        }

        return true;
    }
}

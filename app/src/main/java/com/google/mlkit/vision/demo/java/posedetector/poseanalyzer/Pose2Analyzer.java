package com.google.mlkit.vision.demo.java.posedetector.poseanalyzer;

import android.graphics.PointF;
import android.util.Log;

import com.google.mlkit.vision.demo.java.LivePreviewActivity;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.Objects;

public class Pose2Analyzer extends PoseAnalyzer {

    Pose pose;

    public Pose2Analyzer(Pose pose){
        super(pose);
        this.pose = pose;
    }

    @Override
    public double leftBodyAngle() {
        return 0;
    }

    @Override
    public double rightBodyAngle() {
        return 0;
    }

    public double distanceBetweenCameraAndPerson(float horizontalViewAngle, float verticalViewAngle, float focalLength){
        float sensorX = (float) (Math.tan(Math.toRadians(horizontalViewAngle / 2)) * 2 * focalLength);
        float sensorY = (float) (Math.tan(Math.toRadians(verticalViewAngle / 2)) * 2 * focalLength);

        PointF leftEyePosition = Objects.requireNonNull(pose.getPoseLandmark(PoseLandmark.LEFT_EYE)).getPosition();
        PointF rightEyePosition = Objects.requireNonNull(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)).getPosition();

        float deltaX = Math.abs(leftEyePosition.x - rightEyePosition.x);
        float deltaY = Math.abs(leftEyePosition.y - rightEyePosition.y);

        float distanceInMillimeters;
        float AVERAGE_EYE_DISTANCE = 63;

        if (deltaX >= deltaY) {
            distanceInMillimeters = focalLength * (AVERAGE_EYE_DISTANCE / sensorX) * (LivePreviewActivity.width / deltaX);
        } else {
            distanceInMillimeters = focalLength * (AVERAGE_EYE_DISTANCE / sensorY) * (LivePreviewActivity.height / deltaY);
        }

        double distanceInFeet = distanceInMillimeters/304.8;
        Log.d("DIST BET CAM AND PER", String.valueOf(distanceInFeet));

        return distanceInFeet;
    }

    public double distanceBetweenLegs(double distanceBetweenCameraAndPersonInFeet){
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        float sideDistance = Math.abs(leftAnkle.getPosition3D().getX()) - Math.abs(rightAnkle.getPosition3D().getX());

        Log.d("DIST BET LEGS", String.valueOf(sideDistance));

        return sideDistance;
    }

    public boolean areHandsStraight(){
        boolean isLeftHandAngleValid = angleOfLeftHand() >= 150 && angleOfLeftHand() <=180;
        boolean isRightHandAngleValid = angleOfRightHand() >= 150 && angleOfRightHand() <=180;
        return isLeftHandAngleValid && isRightHandAngleValid;
    }

    public boolean areLegsStraight(){
        boolean isLeftLegAngleValid = angleOfLeftLeg() >= 160 && angleOfLeftLeg() <=180;
        boolean isRightLegAngleValid = angleOfRightLeg() >= 160 && angleOfRightLeg() <=180;
        return isLeftLegAngleValid && isRightLegAngleValid;
    }

    public boolean isPersonTurned90Degree(){
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);

        boolean isXValid = leftShoulder.getPosition().x - rightShoulder.getPosition().x < 30  && leftShoulder.getPosition().x - rightShoulder.getPosition().x > -10;
        boolean isYValid = leftShoulder.getPosition().y - rightShoulder.getPosition().y < 30 && leftShoulder.getPosition().y - rightShoulder.getPosition().y > -10;

        Log.d("ANGLE X & Y", leftShoulder.getPosition().x - rightShoulder.getPosition().x  + " " + (leftShoulder.getPosition().y - rightShoulder.getPosition().y));

        return isXValid && isYValid;
    }

}

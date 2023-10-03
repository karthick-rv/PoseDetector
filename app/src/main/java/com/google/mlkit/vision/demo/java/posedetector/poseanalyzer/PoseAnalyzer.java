package com.google.mlkit.vision.demo.java.posedetector.poseanalyzer;

import static java.lang.Math.atan2;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public abstract class PoseAnalyzer {
    Pose pose;

    public PoseAnalyzer(Pose pose){
        this.pose = pose;
    }

    public abstract double leftBodyAngle();

    public abstract double rightBodyAngle();

    public boolean isWholeBodyVisible(){
        PoseLandmark leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
        PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);
        PoseLandmark leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR);
        PoseLandmark rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR);
        PoseLandmark leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
        PoseLandmark rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        float accuracy = 0.7f;

        boolean isLeftEyeVisible = leftEye !=null && leftEye.getInFrameLikelihood() > accuracy;
        boolean isRightEyeVisible = rightEye !=null && rightEye.getInFrameLikelihood() > accuracy;
        boolean isLeftEarVisible = leftEar !=null && leftEar.getInFrameLikelihood() > accuracy;
        boolean isRightEarVisible = rightEar !=null && rightEar.getInFrameLikelihood() > accuracy;
        boolean isLeftFootIndexVisible = leftFootIndex !=null && leftFootIndex.getInFrameLikelihood() > accuracy;
        boolean isRightFootIndexVisible = rightFootIndex !=null && rightFootIndex.getInFrameLikelihood() > accuracy;
        boolean isLeftWristVisible = leftWrist !=null && leftWrist.getInFrameLikelihood() > accuracy;
        boolean isRightWristVisible = rightWrist !=null && rightWrist.getInFrameLikelihood() > accuracy;

        return isLeftEyeVisible && isRightEyeVisible && isLeftEarVisible && isRightEarVisible && isLeftFootIndexVisible && isRightFootIndexVisible && isLeftWristVisible && isRightWristVisible;
    }

    public abstract double distanceBetweenCameraAndPerson(float horizontalViewAngle, float verticalViewAngle, float focalLength);

    public abstract double distanceBetweenLegs(double distanceBetweenCameraAndPersonInFeet);

    public abstract boolean areHandsStraight();

    public abstract boolean areLegsStraight();


    public abstract boolean isPersonTurned90Degree();

    protected double angleOfLeftHand(){
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        return getAngle(leftShoulder, leftElbow, leftWrist);
    }


    protected double angleOfRightHand(){
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        return getAngle(rightShoulder, rightElbow, rightWrist);
    }

    protected double angleOfLeftLeg(){
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        return getAngle(leftHip, leftKnee, leftAnkle);
    }

    protected double angleOfRightLeg(){
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
        return getAngle(rightHip, rightKnee, rightAnkle);
    }

    public double getAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        double result =
                Math.toDegrees(
                        atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                                lastPoint.getPosition().x - midPoint.getPosition().x)
                                - atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                                firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result); // Angle should never be negative
        if (result > 180) {
            result = (360.0 - result); // Always get the acute representation of the angle
        }
        return result;
    }

}

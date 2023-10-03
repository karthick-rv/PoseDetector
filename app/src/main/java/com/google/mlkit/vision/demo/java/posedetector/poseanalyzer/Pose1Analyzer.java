package com.google.mlkit.vision.demo.java.posedetector.poseanalyzer;

import static java.lang.Math.atan2;

import android.graphics.PointF;

import com.google.mlkit.vision.demo.java.LivePreviewActivity;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.Objects;


public class Pose1Analyzer extends PoseAnalyzer {
    LeftBody leftBody;
    RightBody rightBody;

    Pose pose;

    public Pose1Analyzer(Pose pose, LeftBody leftBodyLandmarks, RightBody rightBodyLandmarks){
        super(pose);
        this.pose = pose;
        this.leftBody = leftBodyLandmarks;
        this.rightBody = rightBodyLandmarks;
    }

    public double leftBodyAngle(){
        return angleBetweenHandAndLegs(leftBody);
    }

    public double rightBodyAngle(){
        return angleBetweenHandAndLegs(rightBody);
    }



    private double angleBetweenHandAndLegs(Body body){
        double wristX = body.wrist.getPosition().x;
        double wristY = body.wrist.getPosition().y;

        double shoulderX = body.shoulder.getPosition().x;
        double shoulderY = body.shoulder.getPosition().y;

        double hipX = body.hip.getPosition().x;
        double hipY = body.hip.getPosition().y;

        double ankleX = body.ankle.getPosition().x;
        double ankleY = body.ankle.getPosition().y;

        double angle = atan2(wristY - shoulderY, wristX - shoulderX) - atan2(hipY - ankleY, hipX - ankleX);
        double angleDegrees = Math.toDegrees(angle);
        if (angleDegrees < 0) {
            angleDegrees += 360;
        }

        if(body instanceof LeftBody){
            angleDegrees = 180-angleDegrees;
        }else{
            angleDegrees = angleDegrees - 180;
        }

        return angleDegrees;
    }


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

        return distanceInFeet;
    }


    public double distanceBetweenLegs(double distanceBetweenCameraAndPersonInFeet){
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        PointF leftFeet = leftAnkle.getPosition(); // You need to implement this method to get the landmark position.
        PointF rightFeet = rightAnkle.getPosition();

        double deltaX = leftFeet.x - rightFeet.x;
        double deltaY = leftFeet.y - rightFeet.y;

        // Use the Pythagorean theorem to calculate the distance
        double distanceBetweenFeet = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        double pixelsPerFoot = distanceBetweenCameraAndPersonInFeet / (0.206);
        double actualDistanceBetweenFeet = distanceBetweenFeet * (distanceBetweenCameraAndPersonInFeet/pixelsPerFoot);

        return actualDistanceBetweenFeet;
    }

    public boolean areHandsStraight(){
        boolean isLeftHandAngleValid = angleOfLeftHand() >= 160 && angleOfLeftHand() <=180;
        boolean isRightHandAngleValid = angleOfRightHand() >= 160 && angleOfRightHand() <=180;
        return isLeftHandAngleValid && isRightHandAngleValid;
    }

    public boolean areLegsStraight(){
        boolean isLeftLegAngleValid = angleOfLeftLeg() >= 160 && angleOfLeftLeg() <=180;
        boolean isRightLegAngleValid = angleOfRightLeg() >= 160 && angleOfRightLeg() <=180;
        return isLeftLegAngleValid && isRightLegAngleValid;
    }

    @Override
    public boolean isPersonTurned90Degree() {
        return false;
    }

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

    public static class LeftBody extends Body{
        public LeftBody(PoseLandmark wrist,PoseLandmark shoulder,PoseLandmark hip,PoseLandmark ankle){
            this.wrist = wrist;
            this.shoulder = shoulder;
            this.hip = hip;
            this.ankle = ankle;
        }
    }

    public static class RightBody extends Body{
        public RightBody(PoseLandmark wrist,PoseLandmark shoulder,PoseLandmark hip,PoseLandmark ankle){
            this.wrist = wrist;
            this.shoulder = shoulder;
            this.hip = hip;
            this.ankle = ankle;
        }
    }
}


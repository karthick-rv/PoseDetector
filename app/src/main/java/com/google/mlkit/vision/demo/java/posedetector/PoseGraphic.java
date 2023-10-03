/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.java.posedetector;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.GraphicOverlay.Graphic;
import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.Pose1Analyzer;
import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.Pose2Analyzer;
import com.google.mlkit.vision.demo.java.texttospeech.TTSMessageListener;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import java.util.List;
import java.util.Locale;

/** Draw the detected pose in preview. */
public class PoseGraphic extends Graphic {

  private static final float DOT_RADIUS = 8.0f;
  private static final float IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f;
  private static final float STROKE_WIDTH = 15.0f;
  private static final float POSE_CLASSIFICATION_TEXT_SIZE = 60.0f;

  private final Pose pose;
  private final boolean showInFrameLikelihood;
  private final boolean visualizeZ;
  private final boolean rescaleZForVisualization;
  private float zMin = Float.MAX_VALUE;
  private float zMax = Float.MIN_VALUE;

  private final List<String> poseClassification;

  private final TTSMessageListener ttsMessageListener;
  private final Paint classificationTextPaint;
  private final Paint leftPaint;
  private final Paint rightPaint;
  private final Paint whitePaint;
  private final Paint anglePaint;
  private final Paint distancePaint;
  private final Paint msgPaint;

  PoseLandmark leftShoulder;
  PoseLandmark rightShoulder;
  PoseLandmark leftWrist;
  PoseLandmark rightWrist;
  PoseLandmark leftHip;
  PoseLandmark rightHip;
  PoseLandmark leftAnkle;
  PoseLandmark rightAnkle;


  PoseGraphic(
          GraphicOverlay overlay,
          Pose pose,
          boolean showInFrameLikelihood,
          boolean visualizeZ,
          boolean rescaleZForVisualization,
          List<String> poseClassification,
          TTSMessageListener ttsMessageListener) {
    super(overlay);
    this.pose = pose;
    this.showInFrameLikelihood = showInFrameLikelihood;
    this.visualizeZ = visualizeZ;
    this.rescaleZForVisualization = rescaleZForVisualization;

    this.poseClassification = poseClassification;
    this.ttsMessageListener = ttsMessageListener;
    classificationTextPaint = new Paint();
    classificationTextPaint.setColor(Color.WHITE);
    classificationTextPaint.setTextSize(POSE_CLASSIFICATION_TEXT_SIZE);
    classificationTextPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK);

    whitePaint = new Paint();
    whitePaint.setStrokeWidth(STROKE_WIDTH);
    whitePaint.setColor(Color.parseColor("#0af2d3"));
    whitePaint.setTextSize(IN_FRAME_LIKELIHOOD_TEXT_SIZE);
    leftPaint = new Paint();
    leftPaint.setStrokeWidth(STROKE_WIDTH);
    leftPaint.setColor(Color.parseColor("#0af2d3"));
    rightPaint = new Paint();
    rightPaint.setStrokeWidth(STROKE_WIDTH);
    rightPaint.setColor(Color.parseColor("#0af2d3"));

    anglePaint = new Paint();
    anglePaint.setStrokeWidth(STROKE_WIDTH);
    anglePaint.setColor(Color.RED);
    anglePaint.setTextSize(300);

    distancePaint = new Paint();
    distancePaint.setStrokeWidth(STROKE_WIDTH);
    distancePaint.setColor(Color.BLUE);
    distancePaint.setTextSize(150);

    msgPaint = new Paint();
    msgPaint.setStrokeWidth(STROKE_WIDTH);
    msgPaint.setColor(Color.BLACK);
    msgPaint.setTextSize(120);
    msgPaint.setTextAlign(Paint.Align.CENTER);

  }

  @Override
  public void draw(Canvas canvas) {
    List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
    if (landmarks.isEmpty()) {
      return;
    }

    // Draw pose classification text.
//    float classificationX = POSE_CLASSIFICATION_TEXT_SIZE * 0.5f;
//    for (int i = 0; i < poseClassification.size(); i++) {
//      float classificationY =
//          (canvas.getHeight()
//              - POSE_CLASSIFICATION_TEXT_SIZE * 1.5f * (poseClassification.size() - i));
//      canvas.drawText(
//          poseClassification.get(i), classificationX, classificationY, classificationTextPaint);
//    }

    // Draw all the points
    for (PoseLandmark landmark : landmarks) {
      drawPoint(canvas, landmark, whitePaint);
      if (visualizeZ && rescaleZForVisualization) {
        zMin = min(zMin, landmark.getPosition3D().getZ());
        zMax = max(zMax, landmark.getPosition3D().getZ());
      }
    }

    PoseLandmark nose = pose.getPoseLandmark(PoseLandmark.NOSE);
    PoseLandmark lefyEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER);
    PoseLandmark lefyEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
    PoseLandmark leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER);
    PoseLandmark rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER);
    PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);
    PoseLandmark rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER);
    PoseLandmark leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR);
    PoseLandmark rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR);
    PoseLandmark leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH);
    PoseLandmark rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH);

    PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
    PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
    PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
    PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
    PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
    PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
    PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
    PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
    PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
    PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
    PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
    PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

    PoseLandmark leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY);
    PoseLandmark rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY);
    PoseLandmark leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX);
    PoseLandmark rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
    PoseLandmark leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB);
    PoseLandmark rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
    PoseLandmark leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL);
    PoseLandmark rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL);
    PoseLandmark leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
    PoseLandmark rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);

    // Face
    drawLine(canvas, nose, lefyEyeInner, whitePaint);
    drawLine(canvas, lefyEyeInner, lefyEye, whitePaint);
    drawLine(canvas, lefyEye, leftEyeOuter, whitePaint);
    drawLine(canvas, leftEyeOuter, leftEar, whitePaint);
    drawLine(canvas, nose, rightEyeInner, whitePaint);
    drawLine(canvas, rightEyeInner, rightEye, whitePaint);
    drawLine(canvas, rightEye, rightEyeOuter, whitePaint);
    drawLine(canvas, rightEyeOuter, rightEar, whitePaint);
    drawLine(canvas, leftMouth, rightMouth, whitePaint);

    drawLine(canvas, leftShoulder, rightShoulder, whitePaint);
    drawLine(canvas, leftHip, rightHip, whitePaint);

    // Left body
    drawLine(canvas, leftShoulder, leftElbow, leftPaint);
    drawLine(canvas, leftElbow, leftWrist, leftPaint);
    drawLine(canvas, leftShoulder, leftHip, leftPaint);
    drawLine(canvas, leftHip, leftKnee, leftPaint);
    drawLine(canvas, leftKnee, leftAnkle, leftPaint);
    drawLine(canvas, leftWrist, leftThumb, leftPaint);
    drawLine(canvas, leftWrist, leftPinky, leftPaint);
    drawLine(canvas, leftWrist, leftIndex, leftPaint);
    drawLine(canvas, leftIndex, leftPinky, leftPaint);
    drawLine(canvas, leftAnkle, leftHeel, leftPaint);
    drawLine(canvas, leftHeel, leftFootIndex, leftPaint);

    // Right body
    drawLine(canvas, rightShoulder, rightElbow, rightPaint);
    drawLine(canvas, rightElbow, rightWrist, rightPaint);
    drawLine(canvas, rightShoulder, rightHip, rightPaint);
    drawLine(canvas, rightHip, rightKnee, rightPaint);
    drawLine(canvas, rightKnee, rightAnkle, rightPaint);
    drawLine(canvas, rightWrist, rightThumb, rightPaint);
    drawLine(canvas, rightWrist, rightPinky, rightPaint);
    drawLine(canvas, rightWrist, rightIndex, rightPaint);
    drawLine(canvas, rightIndex, rightPinky, rightPaint);
    drawLine(canvas, rightAnkle, rightHeel, rightPaint);
    drawLine(canvas, rightHeel, rightFootIndex, rightPaint);




    /* ------ Previous code Start

    double handX2 = leftWrist.getPosition().x;
    double handY2 = leftWrist.getPosition().y;
    double handX1 = leftShoulder.getPosition().x;
    double handY1 = leftShoulder.getPosition().y;

    double legX2 = leftHip.getPosition().x;
    double legY2 = leftHip.getPosition().y;
    double legX1 = leftAnkle.getPosition().x;
    double legY1 = leftAnkle.getPosition().y;

    double rhandX2 = rightWrist.getPosition().x;
    double rhandY2 = rightWrist.getPosition().y;
    double rhandX1 = rightShoulder.getPosition().x;
    double rhandY1 = rightShoulder.getPosition().y;

    double rlegX2 = rightHip.getPosition().x;
    double rlegY2 = rightHip.getPosition().y;
    double rlegX1 = rightAnkle.getPosition().x;
    double rlegY1 = rightAnkle.getPosition().y;

// Calculate the angle between hands and legs
    double angle = Math.atan2(handY2 - handY1, handX2 - handX1) - Math.atan2(legY2 - legY1, legX2 - legX1);
    double rangle = Math.atan2(rhandY2 - rhandY1, rhandX2 - rhandX1) - Math.atan2(rlegY2 - rlegY1, rlegX2 - rlegX1);

// Convert the angle from radians to degrees
    double angleDegrees = Math.toDegrees(angle);
    double rangleDegrees = Math.toDegrees(rangle);

// Make sure the angle is within the range [0, 360]
    if (angleDegrees < 0) {
      angleDegrees += 360;
    }
    if (rangleDegrees < 0) {
      rangleDegrees += 360;
    }

    PointF leftEyePos = lefyEye.getPosition();
    PointF rightEyePos = rightEye.getPosition();
    float deltaX = Math.abs(leftEyePos.x - rightEyePos.x);
    float deltaY = Math.abs(leftEyePos.y - rightEyePos.y);
    double distance = 0;
  //  3.74    5.1839957    3.8880026
   // 2.785    3.6556828    2.7417579
   // LivePreviewActivity.width = 1024;
   // LivePreviewActivity.height = 1024;
   // LivePreviewActivity.FLX = 2.785f;
   // LivePreviewActivity.horizontalAngle = 3.6556828;
   // LivePreviewActivity.verticalAngle =  2.7417579;
    if (deltaX >= deltaY) {
      distance = LivePreviewActivity.FLX * (63 / LivePreviewActivity.horizontalAngle) * (LivePreviewActivity.width / deltaX);
    } else {
      distance = LivePreviewActivity.FLX * (63 / LivePreviewActivity.verticalAngle) * (LivePreviewActivity.height / deltaY);
    }

    angleDegrees = 180-angleDegrees;
    rangleDegrees = rangleDegrees-180;
    Log.d("tryAngleCal","angle="+angleDegrees+"");

    distance = distance/304.8;
    int xPos = (canvas.getWidth() / 2);
    int yPos = (int) ((canvas.getHeight() / 2) - ((msgPaint.descent() + msgPaint.ascent()) / 2)) ;


    // Step 1: Obtain the distance from the camera to the person in feet
    double cameraToPersonDistanceFeet = distance; // You need to implement this method to get the distance.

// Step 2: Calculate the distance between the feet based on the positions of the body landmarks
// Assuming you have the coordinates of two landmarks representing the feet, e.g., PointF feetLandmark1 and PointF feetLandmark2
    PointF feetLandmark1 = leftAnkle.getPosition(); // You need to implement this method to get the landmark position.
    PointF feetLandmark2 = rightAnkle.getPosition(); // You need to implement this method to get the landmark position.

// Step 3: Use trigonometry to find the distance between the feet
    double distanceBetweenFeet = calculateDistanceBetweenPoints(feetLandmark1, feetLandmark2);

    double pixelsPerFoot = distance / (0.206);

// Now, you can calculate the distance between the feet in feet using the camera-to-person distance
    double actualDistanceBetweenFeet = distanceBetweenFeet * (cameraToPersonDistanceFeet/pixelsPerFoot);

    canvas.drawText(
            "X: " + nose.getPosition().x + " Y: " + nose.getPosition().y,
            xPos,
            canvas.getHeight()/2f,
            msgPaint);

    if(distance>= 4 && distance<=6){
      if(actualDistanceBetweenFeet<11){

        canvas.drawText(
                "Distance between your feet's should be greater than 1 feet",
                xPos,
                translateY2(),
                msgPaint);
//        ttsMessageListener.messageReceived();
      }else {
        if ((angleDegrees >= 40 && angleDegrees <= 50) && (rangleDegrees >= 40 && rangleDegrees <= 50)) {
          canvas.drawText(
                  "        Pose Complete                  ",
                  0,
                  translateY2(),
                  distancePaint);
        } else {
          canvas.drawText(
                  "Angle between your legs and arms should 45",
                  xPos,
                  translateY2(),
                  msgPaint);
        }
      }
    }else{
      canvas.drawText(
              "Please stand from 4 to 6 feet's away from camera",
             xPos,
              translateY2(),
              msgPaint);
    }

    Log.d("tryDistance","d="+LivePreviewActivity.FLX+"    "+LivePreviewActivity.horizontalAngle+"    "+LivePreviewActivity.verticalAngle+"   "+LivePreviewActivity.width);

    Log.d("tryDistance","d="+distance);



     Previous code end ------------------- */

// --------------------------
//    case fullBodyNotVisible = "Your full body is not visible in the camera screen. Please step back."    - check all landmark points are visible
//    case spreadLegs = "Legs are too close together. Please spread your legs apart by approximately 1 foot."    - check distance between right ankle point and left ankle point is 1 feet
//    case legsNotStraight = "Legs are not straight. Please straighten both legs, ensuring no bending at the knees."  - check angle between kee - hip and kee - ankle, it should be 90 degree straight
//    case tooCloseWarning = "You are standing too close to the device. Maintain a distance of 5-6 feet from the camera." - check z axis distance, distance should 5-6 not more not less
//    case tooFarWarning = "You are too far from the device. Stand closer to the camera within the 5-6 feet range." - check z axis distance, distance should 5-6 not more not less
//    case handsNotStraight = "The hands are not straight. Please straighten both hands." - check angle between shoulder - wrist
//    case lowerHands = "Hands are not at a 45-degree angle. Please lower hands slightly." - its already implemented, check if it is correct.
//    case riseHands = "Hands are not at a 45-degree angle. Please raise hands slightly."  - its already implemented, check if it is correct.


//    App complete flow
//            - Open app
//            - Open front camera
//    - TTS(Text-To-Speech) - "Please place your device vertically straight on a table, and step back, to make yourself fully visible in the camera frame."
//            - validate if there is a human in the frame - if fails TTS an error
//    - validate if all body parts are visible in frame - if fails TTS an error
//    - validate human is standing within 4-6 feet range - if fails TTS an error
//    - validate human hands are in 45 degree - if failse TTS an error
//    If every validation succeeded then start a countdown 5 to 1
//    Take a pic
//            - (TTS) ask the human to rotate 90 degree
//            - similar validations
//            - if validation success take picture 2
// -------------------------------


//    canvas.drawText(
//            String.format(Locale.US, "%.0f", leftBodyAngle),
//            translateX(leftWrist.getPosition().x),
//            translateY(leftWrist.getPosition().y),
//            anglePaint);
//
//    canvas.drawText(
//            String.format(Locale.US, "%.0f", rightBodyAngle),
//            translateX(rightWrist.getPosition().x),
//            translateY(rightWrist.getPosition().y),
//            anglePaint);

//    canvas.drawText(
//            String.format(Locale.US, "%.0f", poseAnalyzer.angleOfRightHand(pose)),
//            translateX(rightShoulder.getPosition().x),
//            translateY(rightShoulder.getPosition().y),
//            anglePaint);
//

//    canvas.drawText(
//            String.format(Locale.US, "%.0f", pose2Analyzer.getAngle(rightShoulder, rightElbow, rightWrist)),
//            translateX(rightHip.getPosition().x),
//            translateY(rightHip.getPosition().y),
//            msgPaint);


//    canvas.drawText(
//            String.format(Locale.US, "X: %.0f + Y: %.0f", rightShoulder.getPosition().x, rightShoulder.getPosition().y),
//            translateX(rightShoulder.getPosition().x),
//            translateY(rightShoulder.getPosition().y),
//            msgPaint);
//
//    canvas.drawText(
//            String.format(Locale.US, "X: %.0f + Y: %.0f", leftShoulder.getPosition().x, leftShoulder.getPosition().y),
//            translateX(leftShoulder.getPosition().x),
//            translateY(leftShoulder.getPosition().y),
//            msgPaint);

//    double landmarkX = leftEyeOuter.getPosition3D().getX();// X-coordinate;
//    double landmarkY = leftEyeOuter.getPosition3D().getY();// Y-coordinate;
//    double landmarkZ = leftEyeOuter.getPosition3D().getZ();// Z-coordinate;
//    // Assuming you have the 3D coordinates of a landmark in pixel units and camera intrinsic parameters
//
//// 3D coordinates of the landmark in pixel units
//    double landmarkXPixel = landmarkX;// X-coordinate in pixels;
//    double landmarkYPixel = landmarkY;// Y-coordinate in pixels;
//    double landmarkZPixel = landmarkZ;// Z-coordinate in pixels;
//
//// Camera intrinsic parameters
//    double focalLengthX = LivePreviewActivity.FLX;// Focal length along X-axis in pixels;
//    double focalLengthY = LivePreviewActivity.FLY;// Focal length along Y-axis in pixels;
//    double principalPointX = LivePreviewActivity.FLPX;// Principal point X-coordinate in pixels;
//    double principalPointY = LivePreviewActivity.FLPY;// Principal point Y-coordinate in pixels;
//
//// Convert pixel coordinates to meters
//    double landmarkXMeters = (landmarkXPixel - principalPointX) * (landmarkZPixel / focalLengthX);
//    double landmarkYMeters = (landmarkYPixel - principalPointY) * (landmarkZPixel / focalLengthY);
//    double landmarkZMeters = landmarkZPixel;
//
//// Now you have the 3D coordinates of the landmark in meters
//
//
//// Focal length of the camera (in pixels)
//    double focalLength = LivePreviewActivity.FLX;// Focal length value;
//
//// Calculate the projected position of the landmark onto the image plane
//    double imageX = (focalLength * landmarkX) / landmarkZ;
//    double imageY = (focalLength * landmarkY) / landmarkZ;
//
//// Known physical size of the landmark (e.g., height of a person's head) in meters
//    double realSize = 2;// Real size value;
//
//// Calculate the distance using similar triangles
//    double estimatedDistance = realSize / imageY;



    /* ------- Previous code
    canvas.drawText(
            String.format(Locale.US, "%.0f", angleDegrees),
            translateX(leftWrist.getPosition().x),
            translateY(leftWrist.getPosition().y),
            anglePaint);

    canvas.drawText(
            String.format(Locale.US, "%.0f", rangleDegrees),
            translateX(rightWrist.getPosition().x),
            translateY(rightWrist.getPosition().y),
            anglePaint);

    canvas.drawText(
            String.format(Locale.US, "%.0f", distance)+"feet",
            translateX(leftMouth.getPosition().x),
            translateY(rightMouth.getPosition().y),
            distancePaint);



    ---- previous code end */

//    canvas.drawText(
//            String.format(Locale.US, "%.0f", actualDistanceBetweenFeet),
//            translateX(leftAnkle.getPosition().x),
//            translateY(rightAnkle.getPosition().y),
//            anglePaint);
// Display or use the actual distance between the feet in your app

    // Draw inFrameLikelihood for all points
//    if (showInFrameLikelihood) {
//      for (PoseLandmark landmark : landmarks) {
//        canvas.drawText(
//            String.format(Locale.US, "%.2f", landmark.getInFrameLikelihood()),
//            translateX(landmark.getPosition().x),
//            translateY(landmark.getPosition().y),
//            whitePaint);
//      }
//    }
  }

  public static double calculateDistanceBetweenPoints(PointF point1, PointF point2) {
    double deltaX = point1.x - point2.x;
    double deltaY = point1.y - point2.y;

    // Use the Pythagorean theorem to calculate the distance
    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

    return distance;
  }

  void drawPoint(Canvas canvas, PoseLandmark landmark, Paint paint) {
    PointF3D point = landmark.getPosition3D();
    //updatePaintColorByZValue(paint, canvas, visualizeZ, rescaleZForVisualization, point.getZ(), zMin, zMax);
    canvas.drawCircle(translateX(point.getX()), translateY(point.getY()), DOT_RADIUS, paint);
  }

  void drawLine(Canvas canvas, PoseLandmark startLandmark, PoseLandmark endLandmark, Paint paint) {
    PointF3D start = startLandmark.getPosition3D();
    PointF3D end = endLandmark.getPosition3D();

    // Gets average z for the current body line
    float avgZInImagePixel = (start.getZ() + end.getZ()) / 2;
  //  updatePaintColorByZValue(paint, canvas, visualizeZ, rescaleZForVisualization, avgZInImagePixel, zMin, zMax);

    canvas.drawLine(
        translateX(start.getX()),
        translateY(start.getY()),
        translateX(end.getX()),
        translateY(end.getY()),
        paint);
  }
}

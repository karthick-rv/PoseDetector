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

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.java.VisionProcessorBase;
import com.google.mlkit.vision.demo.java.posedetector.classification.PoseClassifierProcessor;
import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.Pose2Analyzer;
import com.google.mlkit.vision.demo.java.posedetector.poseanalyzer.Pose1Analyzer;
import com.google.mlkit.vision.demo.java.posedetector.posevalidator.Pose1Validator;
import com.google.mlkit.vision.demo.java.posedetector.posevalidator.Pose2Validator;
import com.google.mlkit.vision.demo.java.posedetector.posevalidator.PoseTimerGraphic;
import com.google.mlkit.vision.demo.java.posedetector.posevalidator.PoseValidator;
import com.google.mlkit.vision.demo.java.posedetector.posevalidator.Timer;
import com.google.mlkit.vision.demo.java.posedetector.posevalidator.PoseValidationResultListener;
import com.google.mlkit.vision.demo.java.texttospeech.TTSMessageListener;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** A processor to run pose detector. */
public class PoseDetectorProcessor
    extends VisionProcessorBase<PoseDetectorProcessor.PoseWithClassification> implements PoseValidationResultListener {
  private static final String TAG = "PoseDetectorProcessor";

  private final PoseDetector detector;

  private final boolean showInFrameLikelihood;
  private final boolean visualizeZ;
  private final boolean rescaleZForVisualization;
  private final boolean runClassification;
  private final boolean isStreamMode;
  private final Context context;
  private final Executor classificationExecutor;
  private final TTSMessageListener ttsMessageListener;
  private boolean welcomeMessagePublished = false;
  private Timer timer;

  private final Handler handler = new Handler();
  private boolean isNotInFrame = false;

  private int poseTimerCount = 5;

  private boolean pose1complete = false;
  private boolean pose2complete = false;
  private float horizontalViewAngle;
  private float verticalViewAngle;

  private float focalLength;

  private Pose pose;

  private PoseClassifierProcessor poseClassifierProcessor;

  private GraphicOverlay graphicOverlay;

  /** Internal class to hold Pose and classification results. */
  protected static class PoseWithClassification {
    private final Pose pose;
    private final List<String> classificationResult;

    public PoseWithClassification(Pose pose, List<String> classificationResult) {
      this.pose = pose;
      this.classificationResult = classificationResult;
    }

    public Pose getPose() {
      return pose;
    }

    public List<String> getClassificationResult() {
      return classificationResult;
    }
  }

  public PoseDetectorProcessor(
          Context context,
          PoseDetectorOptionsBase options,
          float horizontalViewAngle,
          float verticalViewAngle,
          float focalLength,
          boolean showInFrameLikelihood,
          boolean visualizeZ,
          boolean rescaleZForVisualization,
          boolean runClassification,
          boolean isStreamMode,
          TTSMessageListener ttsMessageListener
      ) {
    super(context);
    this.horizontalViewAngle = horizontalViewAngle;
    this.verticalViewAngle = verticalViewAngle;
    this.focalLength = focalLength;
    this.showInFrameLikelihood = showInFrameLikelihood;
    this.visualizeZ = visualizeZ;
    this.rescaleZForVisualization = rescaleZForVisualization;
    detector = PoseDetection.getClient(options);
    this.runClassification = runClassification;
    this.isStreamMode = isStreamMode;
    this.context = context;
    this.ttsMessageListener = ttsMessageListener;
    classificationExecutor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<PoseWithClassification> detectInImage(InputImage image) {
    return detector
        .process(image)
        .continueWith(
            classificationExecutor,
            task -> {
              Pose pose = task.getResult();
              List<String> classificationResult = new ArrayList<>();
              if (runClassification) {
                if (poseClassifierProcessor == null) {
                  poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode);
                }
                classificationResult = poseClassifierProcessor.getPoseResult(pose);
              }
              return new PoseWithClassification(pose, classificationResult);
            });
  }

  @Override
  protected Task<PoseWithClassification> detectInImage(MlImage image) {
    return detector
        .process(image)
        .continueWith(
            classificationExecutor,
            task -> {
              Pose pose = task.getResult();
              List<String> classificationResult = new ArrayList<>();
              if (runClassification) {
                if (poseClassifierProcessor == null) {
                  poseClassifierProcessor = new PoseClassifierProcessor(context, isStreamMode);
                }
                classificationResult = poseClassifierProcessor.getPoseResult(pose);
              }
              return new PoseWithClassification(pose, classificationResult);
            });
  }

  @Override
  protected void onSuccess(
      @NonNull PoseWithClassification poseWithClassification,
      @NonNull GraphicOverlay graphicOverlay) {
    pose = poseWithClassification.pose;
    this.graphicOverlay = graphicOverlay;
    graphicOverlay.add(
        new PoseGraphic(
            graphicOverlay,
            pose,
            showInFrameLikelihood,
            visualizeZ,
            rescaleZForVisualization,
            poseWithClassification.classificationResult,
                ttsMessageListener));
    if(!welcomeMessagePublished){
      ttsMessageListener.messageReceived("Please place your device vertically straight on a table, and step back, to make yourself fully visible in the camera frame", false);
      welcomeMessagePublished = true;
      validatePose(poseTimerCount);
    }

    if(pose.getAllPoseLandmarks().isEmpty()){
      isNotInFrame = true;
    }

    if(isNotInFrame){
      validatePose(poseTimerCount);
    }


  }

  private void validatePose(int poseTimerCount){

    if (pose.getAllPoseLandmarks().isEmpty()){
      return;
    }else{
      isNotInFrame = false;
    }

    PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
    PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
    PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
    PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
    PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
    PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
    PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
    PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

    Pose1Analyzer.LeftBody leftBody = new Pose1Analyzer.LeftBody(leftWrist, leftShoulder, leftHip, leftAnkle);
    Pose1Analyzer.RightBody rightBody = new Pose1Analyzer.RightBody(rightWrist, rightShoulder, rightHip, rightAnkle);

    Pose1Analyzer pose1Analyzer = new Pose1Analyzer(pose,leftBody, rightBody);

    if(!pose1complete){
      PoseValidator pose1Validator = new Pose1Validator(pose1Analyzer, ttsMessageListener, horizontalViewAngle, verticalViewAngle, focalLength,this, poseTimerCount);
      pose1complete = pose1Validator.validatePose();
      return;
    }

    if(!pose2complete){
      Pose2Analyzer pose2Analyzer = new Pose2Analyzer(pose);
      PoseValidator pose2Validator = new Pose2Validator(pose2Analyzer, ttsMessageListener,horizontalViewAngle, verticalViewAngle, focalLength,this, poseTimerCount);
      pose2complete = pose2Validator.validatePose();
      return;
    }

    ttsMessageListener.messageReceived("Both pose completed successfully. Thank you!", false);
  }

  @Override
  public void onValidationError(int count) {
    handler.postDelayed(() -> {
      validatePose(count);
    }, 1000);
  }

  @Override
  public void onTimerStart(int count) {
    handler.postDelayed(()->{
      int count1 = count - 1;
//      graphicOverlay.add(new PoseTimerGraphic(graphicOverlay,String.valueOf(count1)));
      validatePose(count1);
    }, 1000);
  }

  @Override
  public void onPoseComplete(int count) {
    handler.postDelayed(()->{
      validatePose(count);
    }, 1000);
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Pose detection failed!", e);
  }

  @Override
  protected boolean isMlImageEnabled(Context context) {
    // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
    return true;
  }
}

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

package com.google.mlkit.vision.demo.java;

import static java.lang.Math.atan;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.util.SizeF;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.annotation.KeepName;


import com.google.mlkit.vision.demo.CameraSource;
import com.google.mlkit.vision.demo.CameraSourcePreview;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.java.posedetector.PoseDetectorProcessor;
import com.google.mlkit.vision.demo.preference.PreferenceUtils;
import com.google.mlkit.vision.demo.preference.SettingsActivity;

import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import com.google.android.gms.vision.CameraSource;
/** Live preview demo for ML Kit APIs. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
    implements OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
  private static final String OBJECT_DETECTION = "Object Detection";
  private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection";
  private static final String CUSTOM_AUTOML_OBJECT_DETECTION =
      "Custom AutoML Object Detection (Flower)";
  private static final String FACE_DETECTION = "Face Detection";
  private static final String BARCODE_SCANNING = "Barcode Scanning";
  private static final String IMAGE_LABELING = "Image Labeling";
  private static final String IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)";
  private static final String CUSTOM_AUTOML_LABELING = "Custom AutoML Image Labeling (Flower)";
  private static final String POSE_DETECTION = "Pose Detection";
  private static final String SELFIE_SEGMENTATION = "Selfie Segmentation";
  private static final String TEXT_RECOGNITION_LATIN = "Text Recognition Latin";
  private static final String TEXT_RECOGNITION_CHINESE = "Text Recognition Chinese";
  private static final String TEXT_RECOGNITION_DEVANAGARI = "Text Recognition Devanagari";
  private static final String TEXT_RECOGNITION_JAPANESE = "Text Recognition Japanese";
  private static final String TEXT_RECOGNITION_KOREAN = "Text Recognition Korean";
  private static final String FACE_MESH_DETECTION = "Face Mesh Detection (Beta)";

  private static final String TAG = "LivePreviewActivity";

  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private String selectedModel = POSE_DETECTION;
  public static float FLX = 0;
  public static float FLY = 0;
  public static float FLPX = 0;
  public static float FLPY = 0;
  public static double horizontalAngle = 0;
  public static double verticalAngle = 0;
  public static int width = 0;
  public static int height = 0;

  float F = 1f;           //focal length
  float sensorX, sensorY; //camera sensor dimensions
  float angleX, angleY;
  private Camera frontCam() {
    int cameraCount = 0;
    Camera cam = null;
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    cameraCount = Camera.getNumberOfCameras();
    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
      Camera.getCameraInfo(camIdx, cameraInfo);
      Log.v("CAMID", camIdx + "");
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        try {
          cam = Camera.open(camIdx);
        } catch (RuntimeException e) {
          Log.e("FAIL", "Camera failed to open: " + e.getLocalizedMessage());
        }
      }
    }

    return cam;
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    //if(grantResults.length>0 && grantResults[0] == RESULT_OK){
      Camera camera = frontCam();
      Camera.Parameters campar = camera.getParameters();
      LivePreviewActivity.FLX = campar.getFocalLength();
      F = campar.getFocalLength();
      angleX = campar.getHorizontalViewAngle();
      angleY = campar.getVerticalViewAngle();
      LivePreviewActivity.horizontalAngle = (float) (Math.tan(Math.toRadians(angleX / 2)) * 2 * F);
      LivePreviewActivity.verticalAngle = (float) (Math.tan(Math.toRadians(angleY / 2)) * 2 * F);
      camera.stopPreview();
      camera.release();

      CameraManager cameraManager = null;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

      }
      String cameraId = "";// Specify the camera ID


      CameraCharacteristics characteristics = null;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        try {
          String[] cameraIds = cameraManager.getCameraIdList();
          // Now cameraIds array contains the available camera IDs
          cameraId = cameraIds[0];
          characteristics = cameraManager.getCameraCharacteristics(cameraId);

          float[] focalLengths = new float[0];
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
          }

          if (focalLengths != null && focalLengths.length > 0) {
            float focalLength = focalLengths[0]; // Focal length in millimeters

            //FLX = focalLength;
            // FLY = focalLength;
            SizeF sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            //   horizontalAngle = (2f * atan((sensorSize.getWidth()  /  (focalLength * 2f))));// * 180.0 / Math.PI;
            //     verticalAngle =   (2f * atan((sensorSize.getHeight() /  (focalLength * 2f)))) ;// 180.0 / Math.PI;
            // You can convert millimeters to other units if needed
          }

          float[] lensIntrinsicCalibration = new float[0];
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            lensIntrinsicCalibration = characteristics.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION);
            if (lensIntrinsicCalibration != null && lensIntrinsicCalibration.length >= 2) {
              float principalPointX = lensIntrinsicCalibration[0];
              FLPX = lensIntrinsicCalibration[0];
              FLPY = lensIntrinsicCalibration[1];// The first element represents principalPointX
            }
          }
          Log.d("tryFL",FLX+"  "+FLPY+"   "+FLPX+"  "+FLPY);

        } catch (CameraAccessException e) {
          e.printStackTrace();
        }



      }

      Spinner spinner = findViewById(R.id.spinner);
      List<String> options = new ArrayList<>();
//    options.add(OBJECT_DETECTION);
//    options.add(OBJECT_DETECTION_CUSTOM);
//    options.add(CUSTOM_AUTOML_OBJECT_DETECTION);
//    options.add(FACE_DETECTION);
//    options.add(BARCODE_SCANNING);
//    options.add(IMAGE_LABELING);
//    options.add(IMAGE_LABELING_CUSTOM);
//    options.add(CUSTOM_AUTOML_LABELING);
      options.add(POSE_DETECTION);
//    options.add(SELFIE_SEGMENTATION);
//    options.add(TEXT_RECOGNITION_LATIN);
//    options.add(TEXT_RECOGNITION_CHINESE);
//    options.add(TEXT_RECOGNITION_DEVANAGARI);
//    options.add(TEXT_RECOGNITION_JAPANESE);
//    options.add(TEXT_RECOGNITION_KOREAN);
//    options.add(FACE_MESH_DETECTION);

      // Creating adapter for spinner
      ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
      // Drop down layout style - list view with radio button
      dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      // attaching data adapter to spinner
      spinner.setAdapter(dataAdapter);
      spinner.setOnItemSelectedListener(this);

      ToggleButton facingSwitch = findViewById(R.id.facing_switch);
      facingSwitch.setOnCheckedChangeListener(this);

      ImageView settingsButton = findViewById(R.id.settings_button);
      settingsButton.setOnClickListener(
              v -> {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.putExtra(
                        SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
                startActivity(intent);
              });

      createCameraSource(selectedModel);
      createCameraSource(selectedModel);
      startCameraSource();
   // }
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_vision_live_preview);

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
      Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
    } else {
      Camera camera = frontCam();
      Camera.Parameters campar = camera.getParameters();
      LivePreviewActivity.FLX = campar.getFocalLength();
      F = campar.getFocalLength();
      angleX = campar.getHorizontalViewAngle();
      angleY = campar.getVerticalViewAngle();
      LivePreviewActivity.horizontalAngle = (float) (Math.tan(Math.toRadians(angleX / 2)) * 2 * F);
      LivePreviewActivity.verticalAngle = (float) (Math.tan(Math.toRadians(angleY / 2)) * 2 * F);
      camera.stopPreview();
      camera.release();
    }

    preview = findViewById(R.id.preview_view);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }


    CameraManager cameraManager = null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    }
    String cameraId = "";// Specify the camera ID


      CameraCharacteristics characteristics = null;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        try {
          String[] cameraIds = cameraManager.getCameraIdList();
          // Now cameraIds array contains the available camera IDs
          cameraId = cameraIds[0];
          characteristics = cameraManager.getCameraCharacteristics(cameraId);

          float[] focalLengths = new float[0];
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
          }

          if (focalLengths != null && focalLengths.length > 0) {
            float focalLength = focalLengths[0]; // Focal length in millimeters

            //FLX = focalLength;
           // FLY = focalLength;
            SizeF sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
         //   horizontalAngle = (2f * atan((sensorSize.getWidth()  /  (focalLength * 2f))));// * 180.0 / Math.PI;
       //     verticalAngle =   (2f * atan((sensorSize.getHeight() /  (focalLength * 2f)))) ;// 180.0 / Math.PI;
            // You can convert millimeters to other units if needed
          }

          float[] lensIntrinsicCalibration = new float[0];
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            lensIntrinsicCalibration = characteristics.get(CameraCharacteristics.LENS_INTRINSIC_CALIBRATION);
            if (lensIntrinsicCalibration != null && lensIntrinsicCalibration.length >= 2) {
              float principalPointX = lensIntrinsicCalibration[0];
              FLPX = lensIntrinsicCalibration[0];
              FLPY = lensIntrinsicCalibration[1];// The first element represents principalPointX
            }
          }
          Log.d("tryFL",FLX+"  "+FLPY+"   "+FLPX+"  "+FLPY);

        } catch (CameraAccessException e) {
          e.printStackTrace();
        }



      }

    Spinner spinner = findViewById(R.id.spinner);
    List<String> options = new ArrayList<>();
//    options.add(OBJECT_DETECTION);
//    options.add(OBJECT_DETECTION_CUSTOM);
//    options.add(CUSTOM_AUTOML_OBJECT_DETECTION);
//    options.add(FACE_DETECTION);
//    options.add(BARCODE_SCANNING);
//    options.add(IMAGE_LABELING);
//    options.add(IMAGE_LABELING_CUSTOM);
//    options.add(CUSTOM_AUTOML_LABELING);
    options.add(POSE_DETECTION);
//    options.add(SELFIE_SEGMENTATION);
//    options.add(TEXT_RECOGNITION_LATIN);
//    options.add(TEXT_RECOGNITION_CHINESE);
//    options.add(TEXT_RECOGNITION_DEVANAGARI);
//    options.add(TEXT_RECOGNITION_JAPANESE);
//    options.add(TEXT_RECOGNITION_KOREAN);
//    options.add(FACE_MESH_DETECTION);

    // Creating adapter for spinner
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.spinner_style, options);
    // Drop down layout style - list view with radio button
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // attaching data adapter to spinner
    spinner.setAdapter(dataAdapter);
    spinner.setOnItemSelectedListener(this);

    ToggleButton facingSwitch = findViewById(R.id.facing_switch);
    facingSwitch.setOnCheckedChangeListener(this);

    ImageView settingsButton = findViewById(R.id.settings_button);
    settingsButton.setOnClickListener(
        v -> {
          Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
          intent.putExtra(
              SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
          startActivity(intent);
        });

    createCameraSource(selectedModel);

  }

  @Override
  public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    // An item was selected. You can retrieve the selected item using
    // parent.getItemAtPosition(pos)
//    selectedModel = parent.getItemAtPosition(pos).toString();
//    Log.d(TAG, "Selected model: " + selectedModel);
//    preview.stop();
//    createCameraSource(selectedModel);
//    startCameraSource();startCameraSource
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing.
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d(TAG, "Set facing");
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      } else {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      }
    }
    preview.stop();
    startCameraSource();
  }

  private void createCameraSource(String model) {

    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
      cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);

    }

    try {
      switch (model) {
        case POSE_DETECTION:
          PoseDetectorOptionsBase poseDetectorOptions =
                  PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
          Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);
          boolean shouldShowInFrameLikelihood =
                  PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
          boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
          boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
          boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);
          cameraSource.setMachineLearningFrameProcessor(
                  new PoseDetectorProcessor(
                          this,
                          poseDetectorOptions,
                          shouldShowInFrameLikelihood,
                          visualizeZ,
                          rescaleZ,
                          runClassification,
                          /* isStreamMode = */ true));
          break;
        default:
          Log.e(TAG, "Unknown model: " + model);
      }
    } catch (RuntimeException e) {
      Log.e(TAG, "Can not create image processor: " + model, e);
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getMessage(),
              Toast.LENGTH_LONG)
          .show();
    }
  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null");
        }
        preview.start(cameraSource, graphicOverlay);

      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    createCameraSource(selectedModel);
    startCameraSource();
  }

  /** Stops the camera. */
  @Override
  protected void onPause() {
    super.onPause();
    preview.stop();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }
}

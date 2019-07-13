/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.env.BorderedText;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Classifier;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
  private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Classifier detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  public Map<String, List<Element>> elementsList = new HashMap<>();

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;

    try {
      detector =
          TFLiteObjectDetectionAPIModel.create(
              getAssets(),
              TF_OD_API_MODEL_FILE,
              TF_OD_API_LABELS_FILE,
              TF_OD_API_INPUT_SIZE,
              TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing classifier!");
      Toast toast =
          Toast.makeText(
              getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            canvas -> {
//              tracker.draw(canvas);
              if (isDebug()) {
                tracker.drawDebug(canvas);
              }
            });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    initialiseDictionary();
  }

  private void initialiseDictionary(){
      List<Element> spoonElements = new ArrayList<>();
      spoonElements.add(new Element("Fe", 26, "55.845", "Iron", false));
      spoonElements.add(new Element("Ni", 28, "58.693", "Nickel", false));
      spoonElements.add(new Element("Cr", 24, "51.996", "Chromium", false));

      List<Element> bananaElements = new ArrayList<>();
      bananaElements.add(new Element("K", 19, "39.098", "Potassium", false));

      List<Element> donutElements = new ArrayList<>();
      donutElements.add(new Element("C", 6, "12.011", "Carbon", false));
      donutElements.add(new Element("H", 1, "1", "Hydrogen", false));
      donutElements.add(new Element("O", 8, "16", "Oxygen", false));

      List<Element> wineGlassElements = new ArrayList<>();
      wineGlassElements.add(new Element("Si", 14, "28.085", "Silicon", false));

      List<Element> laptopElements = new ArrayList<>();
      laptopElements.add(new Element("Al", 13, "26.982", "Aluminium", false));

      List<Element> broccoliElements = new ArrayList<>();
      broccoliElements.add(new Element("S", 16, "32.06", "Sulphur", false));
      broccoliElements.add(new Element("C", 6, "12.011", "Carbon", false));
      broccoliElements.add(new Element("H", 1, "1", "Hydrogen", false));
      broccoliElements.add(new Element("O", 8, "16", "Oxygen", false));
      broccoliElements.add(new Element("N", 7, "14", "Nitrogen", false));


      List<Element> carrotElements = new ArrayList<>();
      carrotElements.add(new Element("O", 8, "16", "Oxygen", false));
      carrotElements.add(new Element("H", 1, "1", "Hydrogen", false));


      List<Element> personElements = new ArrayList<>();
      personElements.add(new Element("H", 1, "1", "Hydrogen", false));
      personElements.add(new Element("O", 8, "16", "Oxygen", false));
      personElements.add(new Element("C", 6, "12.011", "Carbon", false));

      List<Element> cakeElements = new ArrayList<>();
      cakeElements.add(new Element("Na", 11, "22.990", "Sodium", false));
      cakeElements.add(new Element("P", 15, "30.974", "Phosphorus", false));
      cakeElements.add(new Element("C", 6, "12.011", "Carbon", false));
      cakeElements.add(new Element("H", 1, "1", "Hydrogen", false));
      cakeElements.add(new Element("O", 8, "16", "Oxygen", false));

      List<Element> forkElements = new ArrayList<>();
      forkElements.add(new Element("Ag", 47, "107.87", "Silver", false));

      List<Element> zebraElements = new ArrayList<>();
      zebraElements.add(new Element("Ag", 47, "107.87", "Silver", false));

      elementsList.put("spoon", spoonElements);
      elementsList.put("banana", bananaElements);
      elementsList.put("donut", donutElements);
      elementsList.put("wine glass", wineGlassElements);
      elementsList.put("laptop", laptopElements);
      elementsList.put("broccoli", broccoliElements);
      elementsList.put("carrot", carrotElements);
      elementsList.put("person", personElements);
      elementsList.put("cake", cakeElements);
      elementsList.put("fork", forkElements);
      elementsList.put("zebra", zebraElements);
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
            () -> {
              LOGGER.i("Running detection on image " + currTimestamp);
              final long startTime = SystemClock.uptimeMillis();
              final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
              lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

              cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
              final Canvas canvas1 = new Canvas(cropCopyBitmap);
              final Paint paint = new Paint();
              paint.setColor(Color.RED);
              paint.setStyle(Style.STROKE);
              paint.setStrokeWidth(2.0f);

              final List<Classifier.Recognition> mappedRecognitions = new LinkedList<>();

              for (final Classifier.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                  canvas1.drawRect(location, paint);

                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
              }



              tracker.trackResults(mappedRecognitions, currTimestamp);
              trackingOverlay.postInvalidate();

              computingDetection = false;

              runOnUiThread(
                      () -> {

//                        showFrameInfo(previewWidth + "x" + previewHeight);
//                        showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
//                        showInference(lastProcessingTimeMs + "ms");
                          float highestRecognition = 0.0f;
                          for(Classifier.Recognition recognition: mappedRecognitions) {
                              if(recognition.getConfidence() >= highestRecognition) {
                                  highestRecognition = recognition.getConfidence();
                              }
                              if(highestRecognition > 0.6) {
                                  objectType.setText(recognition.getTitle());
                                  typeObject = recognition.getTitle();
                                  collectElements.setVisibility(View.VISIBLE);
                                  try{
                                      collectable = elementsList.get(recognition.getTitle()).get(0);
                                  } catch(Exception e) {
                                     // Go away, nothing to see here
                                  }
                              } else {
                                  objectType.setText("Analysing Environment");
                                  collectElements.setVisibility(View.INVISIBLE);
                                  collectable = null;
                              }
                          }
                      });
            });
  }

  @Override
  protected int getLayoutId() {
    return R.layout.camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(() -> detector.setUseNNAPI(isChecked));
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }
}

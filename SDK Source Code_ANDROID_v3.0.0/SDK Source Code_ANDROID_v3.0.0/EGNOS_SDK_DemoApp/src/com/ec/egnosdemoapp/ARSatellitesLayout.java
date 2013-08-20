/**
 * @file ARSatellitesLayout.java
 *
 * Starts the Accelerometer and the Orientation 
 * sensors to get the inclination and the direction 
 * the device is pointed at. Also displays the horizon, geographic direction
 * of the device, the satellites with the future and historic path of travel.
 * Displays 3 different arrows, that guides the user to view the SBAS
 * satellites 120, 124 and 126.
 * 
 * Rev: 3.0.0
 * 
 * Author: DKE Aerospace Germany GmbH
 *
 * Copyright 2012 European Commission
 *
 * Licensed under the EUPL, Version 1.1 only (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 *
 **/
package com.ec.egnosdemoapp;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ec.R;
import com.ec.egnossdk.GlobalState;
import com.ec.egnossdk.LogFiles;
import com.ec.egnossdk.SatelliteData;

/**
 * Class that starts the Accelerometer and the Orientation 
 * sensors to get the inclination and the direction 
 * the device is pointed at. Also displays the horizon, geographic direction
 * of the device, the satellites with the future and historic path of travel.
 * Displays 3 different arrows, that guides the user to view the SBAS
 * satellites 120, 124 and 126.
 */
public class ARSatellitesLayout extends View implements SensorEventListener {

  private static final String TAG_ARSATELLITELAYOUT = "arsatlayout";
  private static float xAngleWidth = 29;
  private static float yAngleWidth = 19;

  public static float screenWidth = 480;
  public static float screenHeight = 320;

  static Vector<ARSatellitesView> arSatViews = new Vector<ARSatellitesView>();

  public SensorManager sensorManager;

  private Context context;
  public static float direction = (float) 22.4;
  public static double inclination;
  public double rollingX = (float) 0;
  public double rollingZ = (float) 0;
  public float kFilteringFactor = (float) 0.05;

  Bitmap arInfoImage;
  static PopupWindow satInfoPopup;

  Bitmap arrowImageRight;
  Bitmap arrowImageLeft;
  
  float vals[];
  private static int countTime = 0;

  private long oldTimeOne = 0;
  private long currentTime = 0;
  private long oldTimeTen = 0;

  float[] mGravity;
  float[] mGeomagnetic;
  private boolean dcMsgDisplayed = false;
  static LogFiles logFiles;
  
  public ARSatellitesLayout(Context context) {
    super(context);
    this.context = context;
    logFiles = new LogFiles();
    this.setFocusableInTouchMode(true);

    oldTimeTen = System.currentTimeMillis();
    oldTimeOne = System.currentTimeMillis();

    int rate = 50000;// 20000;//SensorManager.SENSOR_DELAY_UI;

    sensorManager = (SensorManager) this.context
        .getSystemService(Context.SENSOR_SERVICE);
    sensorManager.registerListener(this,
        sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), rate);
    sensorManager.registerListener(this,
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), rate);
    arInfoImage = ((BitmapDrawable) getResources().getDrawable(
        R.drawable.ic_dialog_info)).getBitmap();

    arrowImageRight = BitmapFactory.decodeResource(getResources(),
        R.drawable.ic_arrow_image_right);
    arrowImageLeft = BitmapFactory.decodeResource(getResources(),
        R.drawable.ic_arrow_image);
  }

  /**
   * onSensorChanged function 
   * 
   * Called when the accuracy of a sensor has changed. 
   * @param		arg0	Sensor
   * @param		arg1	Accuracy of the sensor
   */
  public void onAccuracyChanged(Sensor arg0, int arg1) {
  }

   /**
   * onSensorChanged function 
   * 
   * Called when sensor values have changed for bothe accelerometer and 
   * orientation sensors. Calculates the direction and inclination 
   * of the device
   * @param		sensorEvent		the SensorEvent
   */
  public void onSensorChanged(SensorEvent sensorEvent) {
    vals = sensorEvent.values;
    long timeDifferenceOne, timeDifferenceTen;
    currentTime = System.currentTimeMillis();
    timeDifferenceOne = currentTime - oldTimeOne;
    timeDifferenceTen = currentTime - oldTimeTen;

    if (timeDifferenceTen >= 600000) { // after 10 minutes recalculate satellite position
      countTime = 0;
      updateSatelliteDetailsafterTen();
      oldTimeTen = System.currentTimeMillis();
    }
    if (timeDifferenceOne >= 20000) {// after 20 secs
      countTime = countTime + 2;
      updateSatelliteDetailsafterOne();
      oldTimeOne = System.currentTimeMillis();
    }

    try {
    if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
      float tmp = vals[0];
      direction = tmp + 90+20;
      if(direction >= 360)
        direction = direction - 360;
    }
    if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
      rollingZ = (vals[2] * kFilteringFactor)
          + (rollingZ * (1.0 - kFilteringFactor));
      rollingX = (vals[0] * kFilteringFactor)
          + (rollingX * (1.0 - kFilteringFactor));

      if (rollingZ != 0.0) {
        inclination = Math.atan(rollingX / rollingZ);
      } else if (rollingX < 0) {
        inclination = Math.PI / 2.0;
      } else if (rollingX >= 0) {
        inclination = 3 * Math.PI / 2.0;
      }

      // convert to degrees
      inclination = inclination * (360 / (2 * Math.PI));

      // flip!
      if (inclination < 0)
        inclination = inclination + 90;
      else
        inclination = inclination - 90;
    }
    updateLayouts(direction, (float) inclination); 
    }catch(Exception e) {
      Log.e(TAG_ARSATELLITELAYOUT, "Error occurred while reading sensors data: "+e);
      logFiles.logError("AR View | Error occurred while reading sensors data: "+e);
    }
    
    if (SkyplotStatus.fromBTReceiver) {
      if ((GlobalState.getSocket() == null
          || GlobalState.getErrorWhileReadingBT() == -1)) {
        if (!dcMsgDisplayed) {
            if (GlobalState.getErrorWhileReadingBT() == -1) {
              Toast
                  .makeText(
                      context,
                      "Error occurred while using external Bluetooth Receiver." +
                      "Please connect again to receiver to continue using this feature",
                      Toast.LENGTH_LONG).show();
            }else if(GlobalState.getSocket() == null) {
            Toast
                .makeText(
                    context,
                    "Receiver is disconnected, please connect to a receiver or" +
                    " turn ON GPS of device to continue using Skyplot",
                    Toast.LENGTH_LONG).show();
            }
          dcMsgDisplayed = true;
        }

        for (int c = 0; c < 32; c++) {
          Arrays.fill(SkyplotView.gpsSatelliteTypes[c], 0.0);
          Arrays.fill(SkyplotView.egnosSatelliteTypes[c], 0.0);
          Arrays.fill(SkyplotView.rndSatelliteTypes[c], 0.0);
        }
        GlobalState.setGPSSatelliteType(SkyplotView.gpsSatelliteTypes);
        GlobalState.setEGNOSSatelliteType(SkyplotView.egnosSatelliteTypes);
        GlobalState.setRnDSatelliteType(SkyplotView.rndSatelliteTypes);
        Log.e(TAG_ARSATELLITELAYOUT, "ARSatellitesLayout | Receiver is disconnected");
        
        Enumeration<ARSatellitesView> e = arSatViews.elements();
        while (e.hasMoreElements()) {
          ARSatellitesView arSatView = e.nextElement();
          arSatView.satelliteType = 0;
          arSatView.rndSatelliteType = 0;
        }

      } else
        postInvalidate();
    } else
      postInvalidate();
  }

  /**
   * addARView function 
   * 
   * adds the object that contains information of every satellite
   * obtained from BORAD.
   * 
   * @param		view  the object of the ARSatellitesView class
   */
  public void addARView(ARSatellitesView view) {
    arSatViews.add(view);
  }
  
  /**
   * countARViews function 
   * 
   * Gets the count of all the satellites available from NORAD
   * 
   * @return  size of the array that contains all the satellite data
   */
  public int countARViews() {
    return arSatViews.size();
  }
  

  /**
   * calcXvalue function 
   * 
   * Calculates the x value of the screen coordinates for the 
   * satellite to be drawn based on the direction the device is pointed and 
   * the azimuth of the satellite
   * 
   * @param		leftArm       the left arm of the device
   * @param		rightArm      the right arm of the device
   * @param		az            the azimuth of the satellite
   * @return  ret           the x value of the screen coordinates of the satellite
   */
  private static float calcXvalue(float leftArm, float rightArm, float az) {
    float ret = 0;
    float offset;
    if (leftArm > rightArm) {
      if (az >= leftArm) {
        offset = az - leftArm;
      }
      if (az <= rightArm) {
        offset = 360 - leftArm + az;
      } else
        offset = az - leftArm;
    } else {
      offset = az - leftArm;
    }
    ret = (offset / xAngleWidth) * screenWidth;
    return ret;
  }

  /**
   * calcYvalue function 
   * 
   * Calculates the y value of the screen coordinates for the 
   * satellite to be drawn based on the direction the device is pointed and 
   * the azimuth of the satellite
   * 
   * @param		lowerArm      the left arm of the device
   * @param		upperArm      the left arm of the device
   * @param		inc           the inclination of the satellite
   * @return  ret           the y value of the screen coordinates of the satellite
   */
  private static float calcYvalue(float lowerArm, float upperArm, float inc) {
    float ret = 0;
    // distance in degress to the lower arm
    float offset = ((upperArm - yAngleWidth) - inc) * -1;
    ret = screenHeight - ((offset / yAngleWidth) * screenHeight);
    return ret;
  }

  /**
   * onDraw function 
   * 
   * Draws the horizon, geographic direction of device, satellites and 
   * the arrows that guide the user to SBAS 120,124 and 126.
   * @param		canvas        the canvas to be viewed
   */
  public void onDraw(Canvas canvas) {
    try {
    Enumeration<ARSatellitesView> e = arSatViews.elements();

    Paint p = new Paint();
    p.setColor(Color.WHITE);
    p.setTextSize(20);

    // Draw horizon path
    drawHorizon(canvas);

    p.setTextSize(25);

    String geoDirections = String.valueOf((int) direction) + "°"
        + getDirections();
    // adding geographic direction
    canvas.drawText(geoDirections, screenWidth - (geoDirections.length() * 16),
        20, p);

    // adding Augmented Reality Info Button
    canvas.drawBitmap(arInfoImage, screenWidth - 100, screenHeight - 100, p);

    while (e.hasMoreElements()) {
      ARSatellitesView arSatView = e.nextElement();
      arSatView.draw(canvas);

      // draw an arrow pointing to EGNOS satellite
      if (arSatView.satellitePRN == 120 || arSatView.satellitePRN == 124
          || arSatView.satellitePRN == 126)
          {
        drawEGNOSArrow(canvas, arSatView);
      }

      if (null != satInfoPopup)
        if (satInfoPopup.isShowing())
          if (!arSatView.isSatelliteDrawn && arSatView.isSatellitePopupOpen) {
            arSatView.isSatellitePopupOpen = false;
            closeSatellitePopupWindow();
          }
    }
    invalidate();
    }catch(Exception e) {
      Log.e(TAG_ARSATELLITELAYOUT, "Error occurred while drawing AR View: "+e);
      logFiles.logError("AR View | Error occurred while drawing AR View: "+e);
    }
    
  }

  /**
   * getDirections function 
   * 
   * Corresponds the direction the device is pointed at in terms 
   * of geographic directions N, S, E, W.
   * 
   * @return  geo_directions   the geographic direction of the device
   */
  private String getDirections() {
    String geo_directions = "";
    if (direction < 22) {
      geo_directions = "N";
    } else if (direction >= 22 && direction < 67) {
      geo_directions = "NE";
    } else if (direction >= 67 && direction < 112) {
      geo_directions = "E";
    } else if (direction >= 112 && direction < 157) {
      geo_directions = "SE";
    } else if (direction >= 157 && direction < 202) {
      geo_directions = "S";
    } else if (direction >= 202 && direction < 247) {
      geo_directions = "SW";
    } else if (direction >= 247 && direction < 292) {
      geo_directions = "W";
    } else if (direction >= 292 && direction < 337) {
      geo_directions = "NW";
    } else if (direction >= 337) {
      geo_directions = "N";
    }
    return geo_directions;
  }

  /**
   * drawEGNOSArrow function 
   * 
   * Sets the position (x,y) in terms of screen coordinates, to display the 
   * arrows pointing at the direction the SBAS satellites 120, 14 and 126 
   * would occur.
   * 
   * @param		canvas          the canvas to be viewed
   * @param		arSatView       the object of the ARSatellitesView that contains 
   *                          information about the satellite 120, 124 or 126
   */
  private void drawEGNOSArrow(Canvas canvas, ARSatellitesView arSatView) {
    float posY = 0;
    float posX = 0;

     if(arSatView.isSatelliteDrawn == false) {
      arSatView.isEGNOSArrowDrawn = true;
      
      float x2 = arSatView.satPosX;
      float y2 =arSatView.satPosY ;
      
      if( arSatView.satPosY < 0)
         y2 = 20;
      else if( arSatView.satPosY > 480)
        y2 = screenHeight -130;//350;
      posY = (int) y2;
      
      if( arSatView.satPosX < 0)
        x2 = 20;
     else if( arSatView.satPosX > 800)
       x2 = screenWidth - 130;//800-130;
     posX = (int) x2;

     drawArrow(arSatView, canvas,posX,posY);
    }
  }
  
  /**
   * drawArrow function 
   * 
   * Draws arrows, that guide the direction the SBAS satellites 120, 124 and 126
   * would occur
   * 
   * @param		arSatView        the object of the ARSatellitesView that contains 
   *                           information about the satellite 120, 124 or 126
   * @param		canvas           the canvas to be viewed
   * @param		posX             the x value of the position of the arrow on the screen
   * @param		posY             the y value of the position of the arrow on the screen
   */
  private void drawArrow (ARSatellitesView arSatView,Canvas canvas,float posX,float posY) {
    Bitmap bMapRotate;

    Matrix arrowMatrix = new Matrix();
    int arrowWidth , arrowHeight;

    Paint textPaint = new Paint();
    textPaint.setColor(Color.RED);
    textPaint.setTextSize(20);
    
    double  distance;
    distance = Math.abs(((arSatView.satelliteAzimuth - direction)/360)*100);
    if((int)distance == 0)
      distance = Math.abs(((arSatView.satelliteElevation - inclination)/360)*100);
    
    double div =  (arSatView.satPosY - posY)/( arSatView.satPosX - posX);
    double angle = Math.atan(div)*180/Math.PI;
  
    if(arSatView.satellitePRN == 124) {
      Log.d(TAG_ARSATELLITELAYOUT, "angle: "+ angle);
      Log.d(TAG_ARSATELLITELAYOUT, "posX: "+ posX +" posY: "+posY);
    }
    arrowMatrix.postRotate((float) angle);
    try {
      if (posX != 20) {
        arrowWidth = arrowImageRight.getWidth();
        arrowHeight = arrowImageRight.getHeight();
        if (arrowWidth > 0 && arrowHeight > 0) {
          bMapRotate = Bitmap.createBitmap(arrowImageRight, 0, 0,
              arrowImageRight.getWidth(), arrowImageRight.getHeight(),
              arrowMatrix, true);
          canvas.drawBitmap(bMapRotate, posX, posY, null);
         // canvas.rotate((float) angle, posX + 140 - 300, posY);
          canvas.drawText(" Satellite " + String.valueOf(arSatView.satellitePRN)
              + " - " + String.valueOf(Math.abs((int) distance)) + "%  ",
              (posX)-40, posY, textPaint);
        }
      } else if (posX == 20) {
        arrowWidth = arrowImageLeft.getWidth();
        arrowHeight = arrowImageLeft.getHeight();
        if (arrowWidth > 0 && arrowHeight > 0) {
          bMapRotate = Bitmap.createBitmap(arrowImageLeft, 0, 0,
              arrowImageLeft.getWidth(), arrowImageLeft.getHeight(),
              arrowMatrix, true);
          canvas.drawBitmap(bMapRotate, posX, posY, null);
        //  canvas.rotate((float) angle, Math.abs(posX), posY);
          canvas.drawText("Satellite " + String.valueOf(arSatView.satellitePRN)
              + " - " + String.valueOf(Math.abs((int) distance)) + "% ",
              Math.abs(posX), posY, textPaint);
        }
      }
    }catch(IllegalArgumentException e) {
      Log.e(TAG_ARSATELLITELAYOUT, "Error occurred while drawing EGNOS arrows: "+e);
      logFiles.logError("AR View | Error occurred while drawing EGNOS arrows: "+e);
    }    
  }


  /**
   * drawHorizon function 
   * 
   * Draws the horizon on the augmented reality view.
   * @param		canvas        the canvas to be viewed  
   */
  private void drawHorizon(Canvas canvas) {

    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(10);
    paint.setColor(Color.WHITE);

    Path horizonPath = new Path();

    float x1, y1;// Start point of horizon
    float x2, y2;// mid point of horizon
    float x3, y3;// end point of horizon

    float upperArm = (float) inclination + (yAngleWidth / 2);
    float lowerArm = (float) inclination - (yAngleWidth / 2);

    x2 = (screenWidth / 2);
    y2 = calcYvalue(lowerArm, upperArm, (float) 0);

    x1 = 0;
    y1 = y2 + 40;

    x3 = screenWidth;
    y3 = y2 + 40;

    horizonPath.moveTo(x1, y1);
    horizonPath.quadTo(x2, y2, x3, y3);

    canvas.drawPath(horizonPath, paint);
    canvas.drawLine(x2, y2 - 5, x2, y2 + 35, paint);
  }

  /**
   * computeXY function 
   * 
   * Computes the XY position every satellite based on the azimuth-elevation
   * of the device and the satellite to be drawn.
   * 
   * @param		satelliteValues        an array of satellites with azimuth and elevation
   * @param		XY                     and array containing the XY position of the satellite
   *                                 on the screen
   */
  public static void computeXY(double[] satelliteValues, float[] XY) {

    float leftArm = direction - (xAngleWidth / 2);
    float rightArm = direction + (xAngleWidth / 2);
    if (leftArm < 0)
      leftArm = leftArm + 360;
    if (rightArm > 360)
      rightArm = rightArm - 360;

    float upperArm = (float) (inclination + (yAngleWidth / 2));
    float lowerArm = (float) (inclination - (yAngleWidth / 2));

    for (int i = 0; i < satelliteValues.length; i = i + 2) {
      XY[i] = calcXvalue(leftArm, rightArm, (float) satelliteValues[i]);
      XY[i + 1] = calcYvalue(lowerArm, upperArm, (float) satelliteValues[i + 1]);
    }
  }

  /**
   * updateLayouts function 
   * 
   * Updates the already existing information of the satellites from 
   * ARSatellitesView class with the satellites position x and y.
   * 
   * @param		Azi             the azimuth or direction the device is pointed at
   * @param		Incli           the inclination of the device
   */
  private static void updateLayouts(float Azi, float Incli) {

    // Process the accelerometer
    float leftArm = Azi - (xAngleWidth / 2);
    float rightArm = Azi + (xAngleWidth / 2);
    if (leftArm < 0)
      leftArm = leftArm + 360;
    if (rightArm > 360)
      rightArm = rightArm - 360;

    float upperArm = Incli + (yAngleWidth / 2);
    float lowerArm = Incli - (yAngleWidth / 2);

    Enumeration<ARSatellitesView> e = arSatViews.elements();

    if (arSatViews.size() == 0)
      return;

    while (e.hasMoreElements()) {
      try {
        ARSatellitesView arSatView = e.nextElement();
        arSatView.satPosX = (int) calcXvalue(leftArm, rightArm,
            arSatView.satelliteAzimuth);

        arSatView.satPosY = (int) calcYvalue(lowerArm, upperArm,
            arSatView.satelliteElevation);
 
        arSatView.layout(arSatView.satPosX, arSatView.satPosY,
            arSatView.getBottom(), arSatView.getRight());

      } catch (Exception x) {
        Log.e("ArLayout", x.getMessage());
      }
    }
  }

  /**
   * onTouchEvent function 
   * 
   * Called when the screen of the device was touched.
   * Displays the Augmented Reality about information screen when the screen 
   * was touched at the right most bottom corner.
   * Displays the popup when a satellite has been touched touched.
   * 
   * @param   motionEvent        the object of the MotionEvent class, indicates
   *                             the event that raised the touch
   * @return  TRUE if screen was touched else FALSE
   */
  @Override
  public boolean onTouchEvent(MotionEvent motionEvent) {
    int posX = (int) motionEvent.getX();
    int posY = (int) motionEvent.getY();
    Log.d(TAG_ARSATELLITELAYOUT, "Touched at posX: " + posX + " and posy: "
        + posY);

    switch (motionEvent.getAction()) {
    case MotionEvent.ACTION_DOWN:
      if (posX > screenWidth - 100 && posX < screenWidth
          && posY > screenHeight - 100 && posY < screenHeight) {
        Intent arinfoIntent = new Intent(getContext(), ARViewInformation.class);
        arinfoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(arinfoIntent);
      } else {
        Enumeration<ARSatellitesView> e = arSatViews.elements();
        while (e.hasMoreElements()) {
          ARSatellitesView arSatView = e.nextElement();
          int imageWidth = arSatView.gpsSatelliteBitmap.getWidth();
          int imageHeight = arSatView.gpsSatelliteBitmap.getHeight();
          if (posX > arSatView.satPosX - imageWidth
              && posX < arSatView.satPosX + imageWidth
              && posY > arSatView.satPosY - imageHeight
              && posY < arSatView.satPosY + imageHeight) {
            closeSatellitePopupWindow();
            displaySatellitesInformation(arSatView);
          }
        }
      }
      break;
    }
    return super.onTouchEvent(motionEvent);
  }

  /**
   * updateSatelliteDetailsafterTen function 
   * 
   * Updates information of all the satellites obtained from NORAD,
   * by recalculating the satellite positions after every 10 minutes
   */
  private void updateSatelliteDetailsafterTen() {
    double[] gpsCoordinates = new double[3];
    double gpsTOW = 0;
    double gpsWeekNum = 0;
    double[][] satelliteDetails;
    int i = 0;
    double[] sat_data = new double[2];
    int gpsSatType, egnosSatType;

    if (SkyplotStatus.fromBTReceiver) {
      gpsCoordinates[0] = GlobalState.getPosition()[0];
      gpsCoordinates[1] = GlobalState.getPosition()[1];
      gpsCoordinates[2] = GlobalState.getPosition()[2];
      gpsTOW = GlobalState.getGPSTOW();
      gpsWeekNum = GlobalState.getGPSWN();
    }else {
      gpsCoordinates[0] = SkyplotStatus.gpsCoordinates[0];
      gpsCoordinates[1] = SkyplotStatus.gpsCoordinates[1];
      gpsCoordinates[2] = SkyplotStatus.gpsCoordinates[2];
      gpsTOW = SkyplotStatus.gpsTOW;
      gpsWeekNum = 0;
    }
    
    satelliteDetails = SatelliteData.getSatelliteDetails(
        GlobalState.getNORADData(), gpsCoordinates, gpsTOW, gpsWeekNum,
        SkyplotStatus.fromBTReceiver, true);
    try {
    Enumeration<ARSatellitesView> e = arSatViews.elements();
    while (e.hasMoreElements()) {

      ARSatellitesView arSatView = e.nextElement();
      if (i < SatelliteData.satelliteCount) {
        if (satelliteDetails[i][0] >= 120 & satelliteDetails[i][0] <= 138
            || satelliteDetails[i][0] == 183) {
          arSatView.satelliteType = (int) satelliteDetails[i][1];
        } else {
          gpsSatType = SkyplotView.searchSatelliteUsed(
              GlobalState.getGPSSatelliteType(), satelliteDetails[i][0]);
          egnosSatType = SkyplotView.searchSatelliteUsed(
              GlobalState.getEGNOSSatelliteType(), satelliteDetails[i][0]);
          arSatView.rndSatelliteType = SkyplotView.searchSatelliteUsed(
              GlobalState.getRnDSatelliteType(), satelliteDetails[i][0]);
          arSatView.satelliteType = egnosSatType;

          if (egnosSatType == 0)
            arSatView.satelliteType = gpsSatType;

          if (SkyplotStatus.egnosPos_selected == 0)
            if (egnosSatType == 1)
              arSatView.satelliteType = egnosSatType;
            else
              arSatView.satelliteType = gpsSatType;
        }

        SkyplotView.compute_All(satelliteDetails[i], arSatView.satelliteAziEle);

        arSatView.satelliteAzimuth = (float) satelliteDetails[i][146];
        arSatView.satelliteElevation = (float) satelliteDetails[i][147];

        Log.d(TAG_ARSATELLITELAYOUT,
            "updateSatelliteDetailsafterTen | Satellite Azimuth: "
                + arSatView.satelliteAzimuth + "Satellite Elevation: "
                + arSatView.satelliteElevation);

        ARView.getSat_Data(GlobalState.getGPSSatelliteType(),
            (int) satelliteDetails[i][0], sat_data);
        ARView.getSat_Data(GlobalState.getEGNOSSatelliteType(),
            (int) satelliteDetails[i][0], sat_data);
        arSatView.satelliteDistance = sat_data[0];
        arSatView.satelliteSNR = sat_data[1];

        System.arraycopy(satelliteDetails[i], 2, arSatView.satellitePastValues,
            0, 144);
        System.arraycopy(satelliteDetails[i], 148,
            arSatView.satelliteFutureValues, 0, 144);
        i++;
      }
    }
    }catch(Exception e) {
      Log.e(TAG_ARSATELLITELAYOUT, "Error occurred while updating AR view: "+e);
      logFiles.logError("AR View | Error occurred while updating AR view: "+e);
    }
  }

  /**
   * closeSatellitePopupWindow function 
   * 
   * Closes the popup window that displays the satellite information
   */
  private void closeSatellitePopupWindow() {
    if (null != satInfoPopup) {
      if (satInfoPopup.isShowing()) {
        satInfoPopup.dismiss();
        satInfoPopup = null;
      }
    }
  }

  /**
   * displaySatellitesInformation function 
   * 
   * Displays the popup window that displays the satellite information
   * @param		arSatView        the object of the ARSatellitesView class
   *                           that contains information about the satellite
   */
  private void displaySatellitesInformation(ARSatellitesView arSatView) {
    int w, h;
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    ARSatellitePopup arSatPopupLayout = (ARSatellitePopup) inflater.inflate(
        R.layout.arsatelliteinfo, null);
    TextView prnTextView = (TextView) arSatPopupLayout
        .findViewById(R.id.prntextView);
    TextView nameTextView = (TextView) arSatPopupLayout
        .findViewById(R.id.nametextView);
    TextView snrTextView = (TextView) arSatPopupLayout
        .findViewById(R.id.snrtextView);
    TextView elevTextView = (TextView) arSatPopupLayout
        .findViewById(R.id.elevationtextView);
    TextView aziTextView = (TextView) arSatPopupLayout
        .findViewById(R.id.azimuthtextView);
    satInfoPopup = new PopupWindow(arSatPopupLayout);
    arSatPopupLayout.setARView(arSatView);

    Log.d(TAG_ARSATELLITELAYOUT, "Satellite Info Popup shown");
    satInfoPopup.showAtLocation(this, Gravity.NO_GRAVITY, arSatView.satPosX,
        arSatView.satPosY);
    
    if (arSatView.satellitePRN == 22)
      Log.d(TAG_ARSATELLITELAYOUT, "displaySatellitesInformation | satPRN: "
          + arSatView.satellitePRN + " satPosX: " + arSatView.satPosX
          + "satPosY: " + arSatView.satPosY);
        
    if(screenHeight> 500)
      h = (int) screenHeight /2-100;
    else 
      h = 200;
    
    if(screenWidth > 800)
      w = (int) screenWidth / 2-200;
    else 
      w = 300;
      
    satInfoPopup.update(arSatView.satPosX, arSatView.satPosY, w , h);
    arSatView.isSatellitePopupOpen = true;

    prnTextView.setText("Satellite Number: " + arSatView.satellitePRN);
    nameTextView.setText("Name: " + arSatView.satelliteName);
    aziTextView.setText("Azimuth: "
        + Math.round(arSatView.satelliteAzimuth * 100) / 100.0d);

    elevTextView.setText("Elevation: "
        + Math.round(arSatView.satelliteElevation * 100) / 100.0d);
    
    if(arSatView.satelliteSNR != 0)
      snrTextView.setText("SNR: " + Math.round(arSatView.satelliteSNR * 100)
        / 100.0d);
    else 
      snrTextView.setText("SNR: n/a");

    ImageView cancelImageView = (ImageView) arSatPopupLayout
        .findViewById(R.id.close_button);
    cancelImageView.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        closeSatellitePopupWindow();
      }
    });
  }

  /**
   * updateSatelliteDetailsafterOne function 
   * 
   * Updates information of all the satellites obtained from NORAD,
   * by getting the satellite positions after every 1 minute from 
   * the pre-calculated list of the satellite positions.
   */
  public static void updateSatelliteDetailsafterOne() {
    int i = 0;
    double[] sat_data = new double[2];
    double[][] satelliteDetails = GlobalState.getSatelliteDetails();
    int gpsSatType, egnosSatType;
    try{
    Enumeration<ARSatellitesView> e = arSatViews.elements();
    while (e.hasMoreElements()) {
      ARSatellitesView arSatView = e.nextElement();
      if (i < SatelliteData.satelliteCount) {
        if (satelliteDetails[i][0] >= 120 & satelliteDetails[i][0] <= 138
            || satelliteDetails[i][0] == 183) {
          arSatView.satelliteType = (int) satelliteDetails[i][1];
        } else {
          gpsSatType = SkyplotView.searchSatelliteUsed(
              GlobalState.getGPSSatelliteType(), satelliteDetails[i][0]);
          egnosSatType = SkyplotView.searchSatelliteUsed(
              GlobalState.getEGNOSSatelliteType(), satelliteDetails[i][0]);
          arSatView.rndSatelliteType = SkyplotView.searchSatelliteUsed(
              GlobalState.getRnDSatelliteType(), satelliteDetails[i][0]);
          arSatView.satelliteType = egnosSatType;

          if (egnosSatType == 0)
            arSatView.satelliteType = gpsSatType;

          if (SkyplotStatus.egnosPos_selected == 0)
            if (egnosSatType == 1)
              arSatView.satelliteType = egnosSatType;
            else
              arSatView.satelliteType = gpsSatType;
        }
        SkyplotView.compute_All(satelliteDetails[i], arSatView.satelliteAziEle);

        arSatView.satelliteAzimuth = (float) arSatView.satelliteAziEle[4380 + countTime];
        arSatView.satelliteElevation = (float) arSatView.satelliteAziEle[4381 + countTime];

        Log.d(TAG_ARSATELLITELAYOUT, "count time: " + countTime);
        if (arSatView.satellitePRN == 22)
          Log.d(TAG_ARSATELLITELAYOUT,
              "updateSatelliteDetailsafterOne | Satellite Azimuth: "
                  + arSatView.satelliteAzimuth + "Satellite Elevation: "
                  + arSatView.satelliteElevation);

        if (SkyplotStatus.fromBTReceiver) {
          ARView.getSat_Data(GlobalState.getGPSSatelliteType(),
              (int) satelliteDetails[i][0], sat_data);
          ARView.getSat_Data(GlobalState.getEGNOSSatelliteType(),
              (int) satelliteDetails[i][0], sat_data);
          arSatView.satelliteDistance = sat_data[0];
          arSatView.satelliteSNR = sat_data[1];
        }

        System.arraycopy(satelliteDetails[i], 2, arSatView.satellitePastValues,
            0, 145);
        System.arraycopy(satelliteDetails[i], 148,
            arSatView.satelliteFutureValues, 0, 144);
        i++;
      }
    }
    }catch(Exception e) {
      Log.e(TAG_ARSATELLITELAYOUT, "Error occurred while updating AR view: "+e);
      logFiles.logError("AR View | Error occurred while updating AR view: "+e);
    }
  }

  /**
   * onKeyDown function 
   * 
   * Called when a key is held down.
   * Closes the popup that displays satellite information.
   * Unregisters listeners to the sensors.
   * Closes the augmented reality view
   * 
   * @param  keyCode         The keycode of the key pressed.
   * @param  event           The description of the key envent.
   * @return TRUE if key is pressed otherwise FALSE.
   **/
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    switch (keyCode) {
    case KeyEvent.KEYCODE_BACK:
      closeSatellitePopupWindow();
      ((Activity) context).finish();
      sensorManager.unregisterListener(this);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}

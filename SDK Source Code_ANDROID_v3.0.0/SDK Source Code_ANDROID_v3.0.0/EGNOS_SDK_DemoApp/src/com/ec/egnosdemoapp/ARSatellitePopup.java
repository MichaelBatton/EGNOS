/**
 * @file ARSatellitePopup.java
 *
 * Displays a popup in the augmented reality view, to display the 
 * information of the satellite when a satellite is clicked.
 * This popup displays the satellite name, satellite number,
 * azimuth of the satellite on the sky, elevation of the satellite 
 * and Signal to Noise ratio (SNR) of the satellite.This popup also 
 * contains a delete button to close the popup.
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Class that displays a popup in the augmented reality view, to display the 
 * information of the satellite when a satellite is clicked.
 * This popup displays the satellite name, satellite number,
 * azimuth of the satellite on the sky, elevation of the satellite 
 * and Signal to Noise ratio (SNR) of the satellite.This popup also 
 * contains a delete button to close the popup.
 */
public class ARSatellitePopup  extends LinearLayout{
  ARSatellitesView arSatView;

  /**
   * ARSatellitePopup constructor 
   *   
   * @param		context   context of this Activity
   * @return	attrs     the attribute set
   */
  public ARSatellitePopup(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  /**
   * setARView function 
   * 
   * Sets the object of the ARSatelliteview class
   * 
   * @param		arSatView    the object of the ARSatellitesView class.
   */
  public void setARView(ARSatellitesView arSatView){
    this.arSatView = arSatView;
  }

  /**
   * dispatchDraw function 
   * 
   * draws the rectangle for the popup on the canvas
   * @param   canvas    canvas to  be displayed
   */
  @Override
  protected void dispatchDraw(Canvas canvas) {
    Paint panelPaint = new Paint();
    int measuredWidth = getMeasuredWidth();
    int measuredHeight = getMeasuredHeight();

    panelPaint.setARGB(0, 0, 0, 0);

    RectF panelRect = new RectF();
    panelRect.set(0, getMeasuredHeight(),
        getMeasuredWidth(), getMeasuredHeight());
    canvas.drawRoundRect(panelRect, 10, 10, panelPaint);

    RectF baloonRect = new RectF();
    baloonRect.set(0, 0, measuredWidth-30,
        measuredHeight);

    panelPaint.setARGB(230, 255, 255, 255);
    canvas.drawRoundRect(baloonRect, 10, 10, panelPaint);
    super.dispatchDraw(canvas);
  }
}
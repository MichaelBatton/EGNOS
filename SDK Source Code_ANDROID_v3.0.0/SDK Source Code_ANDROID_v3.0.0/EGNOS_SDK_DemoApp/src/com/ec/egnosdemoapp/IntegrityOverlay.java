/**
 * @file IntegrityOverlay.java
 *
 * Overlay to draw position integrity circle/s on the map 
 * around the EGNOS location point.
 *
 * Rev: 3.0.0
 * 
 * Author: DKE Aerospace Germany GmbH
 * 
 * Copyright 2012 DKE Aerospace Germany GmbH
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
 **/

package com.ec.egnosdemoapp;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import com.ec.R;
import com.ec.egnossdk.GlobalState;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * Overlay todraw position integrity circle/s on the map 
 * around the EGNOS location point.
 */
public class IntegrityOverlay extends Overlay {
  GeoPoint geoPoint;
  ArrayList<String> displayValue = new ArrayList<String>(3);
  ArrayList<Float> circleRadius = new ArrayList<Float>(3);
  float drawRadius;

  /**
   * IntegrityOverlay constructor
   * 
   * The function displays up to 3 circles of a radius equal to 
   * the HPL parameter value multiplied with 3 different values of 
   * the K factor.
   * @param geoPoint          The geopoint of start point.
   * @param displayValue      The arraylist of strings of values indicating the 
   *                          radius of the position integrity circle/s.
   * @param HPL               The HPL without the multiplication by the K factor.
   */
  public IntegrityOverlay(GeoPoint geoPoint, ArrayList<String> displayValue,
      double HPL) {
    this.geoPoint = geoPoint;
    this.displayValue = displayValue;
    if(displayValue.get(0) == "true") 
   // K factor for aviation use according to the MOPS.
     circleRadius.add((float)6.18*(float)HPL);     
    else 
     circleRadius.add((float) 0.0);
    if(displayValue.get(1) == "true")
   // K factor for maritime use according to IMO req: 10-5 per 3 hour).
     circleRadius.add((float)5.6*(float)HPL);  
    else 
      circleRadius.add((float) 0.0);
    if(displayValue.get(2) == "true")
   // K factor computed from the inverse normal cumulative distribution 
   //  function with a probability of 85%.
     circleRadius.add((float)1.04*(float)HPL); 
    else 
      circleRadius.add((float) 0.0);
  }

  /**
   * draw function 
   * 
   * Draws position integrity circle/s on map around a location
   * point with a specific color 
   * @param canvas           The canvas to draw route.
   * @param mapView          The map view that requested the draw.
   * @param shadow           If TRUE, draws the shadow layer. If FALSE, 
   *                         draws the overlay contents.
   * @return TRUE if canvas has to be drawn again, otherwise FALSE.
   */
  @Override
  public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    super.draw(canvas, mapView, shadow);

    Projection projection = mapView.getProjection();
    Point point = new Point();
    projection.toPixels(geoPoint, point);
    if(GlobalState.getisEgnosPosition() == 1){
    for (int i = 0; i < 3; i++) {
      if(circleRadius.get(i) != 0){
    	 float radius =  circleRadius.get(i);
    	 radius = (float) (circleRadius.get(i) * 1 / Math.cos(geoPoint.getLatitudeE6() / 1E6 * Math.PI / 180));
        drawRadius = metersToRadius( radius, mapView,geoPoint.getLatitudeE6());
        
//        drawRadius = mapView.getProjection().metersToEquatorPixels(
//            circleRadius.get(i));
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if(i == 0)//Aviation
          circlePaint.setColor(0x186666ff);
        else if(i == 1)// Maritime
          circlePaint.setColor(Color.MAGENTA);
        else if(i == 2)//85%
          circlePaint.setColor(R.color.Purple);
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Style.STROKE);
        canvas.drawCircle(point.x, point.y, drawRadius, circlePaint);
        
        circlePaint.setColor(0x186666ff);
        circlePaint.setStyle(Style.FILL);
        canvas.drawCircle(point.x, point.y, drawRadius, circlePaint);
      }
    }
   }
  }
  
  public static int metersToRadius(float meters, MapView map, double latitude) {
    return (int) (map.getProjection().metersToEquatorPixels(meters) * (1 / Math
        .cos(Math.toRadians(latitude/1E6))));
  }
}

/**
 * @file DirectionOverlay.java
 *
 * An overlay to draw route from start point to end point on the Google Map.
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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import com.ec.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * Class that uses an overlay to draw route from start point to end point 
 * on the Google Map.
 */
public class DirectionOverlay extends Overlay {
  private GeoPoint gp1;
  private GeoPoint gp2;
  private int mRadius = 6;
  private int mode = 0;

  /**
   * DirectionOverlay constructor
   * 
   * Constructs the Geopoint for the start and end point.
   * The modes 1 to 3, indicating mode 1 as start point, mode 2 as intermediate
   * geopoints between start and end point and mode 3 as end point. 
   * @param gp1             The geopoint of start point.
   * @param gp2             The geopoint of end point.
   * @param mode            The modes 1 to 3.
   */
  public DirectionOverlay(GeoPoint gp1, GeoPoint gp2, int mode) {
    this.gp1 = gp1;
    this.gp2 = gp2;
    this.mode = mode;
  }

  /**
   * DirectionOverlay constructor
   * 
   * Constructs the Geopoint for the start and end point.
   * The modes 1 to 3, indicating mode 1 as start point, mode 2 as intermediate
   * geopoints between start and end point and mode 3 as end point.  
   * @param gp1                The geopoint of start point.
   * @param gp2                The geopoint of end point.
   * @param mode               The modes 1 to 3.
   * @param defaultColor       The color of the route path.
   */
  public DirectionOverlay(GeoPoint gp1, GeoPoint gp2, int mode, int defaultColor) {
    this.gp1 = gp1;
    this.gp2 = gp2;
    this.mode = mode;
  }

  /**
   * getMode function
   * 
   * Gets the different modes of the route to be drawn. 
   * Mode 1, indicating Start point of the route.
   * Mode 2, indicating the path of the route.
   * Mode 3, indicating End point of the route. 
   * @return   mode    The different modes of the route.
   */
  public int getMode() {
    return mode;
  }

  /**
   * draw function 
   * 
   * Draws route start point to end point, with start point in
   * blue dot and end point in green dot.
   * @param canvas           The canvas to draw route.
   * @param mapView          The map view that requested the draw.
   * @param shadow           If TRUE, draws the shadow layer. If FALSE, 
   *                         draws the overlay contents.
   * @param when             The timestamp of the draw.
   * @return TRUE if canvas has to be drawn again, otherwise FALSE.
   */
  @Override
  public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
    Projection projection = mapView.getProjection();
    if (shadow == false) {
      Paint paint = new Paint();
      paint.setAntiAlias(true);
      Point point = new Point();
      projection.toPixels(gp1, point);
      if (mode == 1) {
        paint.setColor(Color.RED);
        RectF oval = new RectF(point.x - mRadius, point.y - mRadius, point.x
            + mRadius, point.y + mRadius);
       // Start Point
        canvas.drawOval(oval, paint);
      } else if (mode == 2) {
        paint.setColor(Color.GREEN);
        Point point2 = new Point();
        projection.toPixels(gp2, point2);
        paint.setStrokeWidth(5);
        paint.setAlpha(120);
       // Geo points between start and end point.
        canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
      } else if (mode == 3) {
        paint.setColor(Color.GREEN);
        Point point2 = new Point();
        projection.toPixels(gp2, point2);
        paint.setStrokeWidth(5);
        paint.setAlpha(120);
        canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
        RectF oval = new RectF(point2.x - mRadius, point2.y - mRadius, point2.x
            + mRadius, point2.y + mRadius);
        paint.setAlpha(255);
        // End Point
        canvas.drawOval(oval, paint);
      }
    }
    return super.draw(canvas, mapView, shadow, when);
  }
}
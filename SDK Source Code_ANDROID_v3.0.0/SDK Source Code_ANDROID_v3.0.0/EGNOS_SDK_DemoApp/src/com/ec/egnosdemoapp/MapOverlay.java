/**
 * @file MapOverlay.java
 *
 * Overlay to draw current location markers on google map.
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

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Overlay to draw current location markers on google map.
 */
public class MapOverlay extends ItemizedOverlay<OverlayItem> {
  private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

  /**
   * MapOverlay constructor
   * 
   * @param defaultMarker    current location drawable to use to draw on map.
   */
  public MapOverlay(Drawable defaultMarker) {
    super(boundCenterBottom(defaultMarker));

  }

  /**
   * populateOverlay function
   * 
   * Function to do processing on a newly added ItemizedOverlay.
   */
  public void populateOverlay() {
    populate();
  }

  /**
   * createItem function 
   * 
   * Create an item to pin current location drawable on map.
   * @param i    Gets overlay item at position i.
   */
  @Override
  protected OverlayItem createItem(int i) {
    return mOverlays.get(i);
  }

  /**
   * size function 
   * Size of overlay list of drawables.
   */
  @Override
  public int size() {
    return mOverlays.size();
  }

  /**
   * addOverlay function 
   * 
   * Add overlay item to map.
   * @param overlay    overlayItem to add.
   */
  public void addOverlay(OverlayItem overlay) {
    mOverlays.add(overlay);
    setLastFocusedIndex(-1);
  }

  /**
   * removeOverlay function 
   * 
   * Remove overlay item from map.
   * @param overlay    overlayItem to remove.
   */
  public void removeOverlay(OverlayItem overlay) {
    mOverlays.remove(overlay);
    mOverlays.clear();
    setLastFocusedIndex(-1);
    populate();
  }

}

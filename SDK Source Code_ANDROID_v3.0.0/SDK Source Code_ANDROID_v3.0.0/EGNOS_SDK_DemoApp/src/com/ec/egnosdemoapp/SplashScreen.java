/**
 * @file SplashScreen.java
 *
 * Displays Splash Screen for 3 seconds or until user
 * touches on mobile device screen
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

import com.ec.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to display the Splash Screen of the application 
 * showing the EGNOS enabled logo and EGNOS portal.
 */
public class SplashScreen extends Activity {

  protected boolean _active = true;
  protected int _splashTime = 3000; // time to display the splash screen in ms
  private static final String TAG = "EGNOS-SDK";
  private final String VERSION_NUMBER = "Version 3.0.0"; //Version number of the application.

  /**
   * Method which is first called when an activity is created.
   **/
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.splashscreen);
    TextView appVersionNumber = (TextView)this.findViewById(R.id.version_number);
    appVersionNumber.setText(VERSION_NUMBER);
    Toast.makeText(this, "Hallo Herr Bernot", Toast.LENGTH_LONG).show();	//Class Toast for welcome Long is time ca. 5s 

    /**
     * Thread to display the EGNOSDemoAppMain activity after 3000ms 
     * or on Touch event.
    **/
    Thread splashTread = new Thread() {
      @Override
      public void run() {
        try {
          int waited = 0;
          while (_active && (waited < _splashTime)) {
            sleep(100);
            if (_active) {
              waited += 100;
            }
          }
        } catch (InterruptedException e) {
          Log.e(TAG,
              "SplashScreen | Wait for 100ms interrupted: " + e.getMessage());
        } finally {
          finish();
          Intent i = new Intent(SplashScreen.this, EgnosDemoAppMain.class);
          startActivity(i);
        }
      }
    };
    splashTread.start();
  }

  /**
   * onTouchEvent function
   * 
   * _active is false if mobile device screen is touched.
   * @param event    Motion event on mobile device.
   * @return TRUE if touch event occured, otherwise FALSE.
   */
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      _active = false;
    }
    return true;
  }

}
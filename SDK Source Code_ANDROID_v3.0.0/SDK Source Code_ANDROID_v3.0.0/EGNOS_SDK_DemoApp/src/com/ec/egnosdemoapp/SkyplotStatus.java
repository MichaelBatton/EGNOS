/**
 * @file SkyplotStatus.java
 *
 * Displays Skyplot View of the application.
 * Get satellite data from NORAD, gets the 
 * the satellite azimuth and elevation over 
 * a period of 24 hours. Calls the Skyplot View class
 * to display the Skyplot and the satellites.
 * Displays a time slider to view all the satellites 
 * over a period of 12 hours.Contains text views to enable 
 * or disable the different satellites displayed on the 
 * Skyplot like satellites used to get a GPS position,
 * satellites used to get an EGNOS position,
 * satellites used to get an R&D position, sbas satellites
 * and satellites not used to get the current position.
 * Displays the count for all the satellites.
 * Also contains a button to open the Augmented Reality view.
 * 
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
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ec.R;
import com.ec.egnossdk.GlobalState;
import com.ec.egnossdk.LogFiles;
import com.ec.egnossdk.SatelliteData;

/**
 * Class that displays Skyplot View of the application.
 * Get satellite data from NORAD, gets the 
 * the satellite azimuth and elevation over 
 * a period of 24 hours. Calls the Skyplot View class
 * to display the Skyplot and the satellites.
 * Displays a time slider to view all the satellites 
 * over a period of 12 hours.Contains text views to enable 
 * or disable the different satellites displayed on the 
 * Skyplot like satellites used to get a GPS position,
 * satellites used to get an EGNOS position,
 * satellites used to get an R&D position, sbas satellites
 * and satellites not used to get the current position.
 * Displays the count for all the satellites.
 * Also contains a button to open the Augmented Reality view.
 **/
public class SkyplotStatus extends Activity {

  private static final String TAG_SYKPLOT_STATUS = "skyplot_status";

  TextView msgTextView;
  public static TextView gpsPos_count;
  public static TextView egnosPos_count;
  public static TextView rndPos_count;
  public static TextView sbasSat_count;
  public static TextView notused_count;
  SeekBar timeSlider;

  private static SkyplotView skyplotView = null;

  private static ProgressDialog progressBarDialog;
  static double[][] satelliteDetails = null;

  static SharedPreferences skyplotStatusSharedPreferences;
  public static final String KEY_SATELLITE_SHARED_PREF = "satelliteSharedPrefKey";
  private SharedPreferences.Editor prefEditor;

  private static final int TIME_SLIDER_MAX = 1440;// 1440;//144;
  private static int TIME_SLIDER_PROGRESS = 720;// 72;

  public static final String KEY_SATELLITEPATH_CHECKED = "satellitePathKey";
  public static final String KEY_GPSPOS_SELECTED = "gpsPosKey";
  public static final String KEY_EGNOSPOS_SELECTED = "egnosPosKey";
  public static final String KEY_RnDPOS_SELECTED = "rndPosKey";
  public static final String KEY_SBASSATELLITE_SELECTED = "sbasKey";
  public static final String KEY_NOTUSED_SELECTED = "notusedKey";

  public static final String KEY_SATELLITE_DETAILS = "satelliteDetails";
  public static int gpsPos_selected;
  public static int egnosPos_selected;
  private int rndPos_selected;
  private int sbasSat_selected;
  private int notUsed_selected;

  public static double[] gpsCoordinates = new double[3];
  public static double gpsTOW = 0;
  static double gpsWeekNum = 0;
  
  static boolean fromBTReceiver = false;
  
  static LocationManager locationManager;
  LocationListener gpslocListener;
  private GpsStatus.Listener gpsListener = null;
  
  public boolean gpsLocationAvailable = false;

  private static int noradError = 0;
  public static String[][] satelliteDatafromNORAD = new String[50][24];

  static boolean timeSliderUsed = false;
  static boolean gpsPosClicked= false;
  static boolean egnosPosClicked = false;
  static boolean rndPosCLicked = false;
  static boolean sbasClicked = false;
  static boolean notUsedClicked = false;

  public static Handler updateSkyplotHandler;
  static double oldTimeOne;
  static double oldTimeTen;
  static double timeDifferenceOne;
  static double timeDifferenceTen;

  public static int countTime = 0;
  
  private boolean isPaused = false;
  private boolean isARClicked = false;
  
  private static boolean onStart = true;
  private static Context context;
  static LogFiles logFiles;
  int countWait = 0;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.skyplotstatus);
    // StrictMode.ThreadPolicy policy = new
    // StrictMode.ThreadPolicy.Builder().permitAll().build();
    // StrictMode.setThreadPolicy(policy);
    context = this;
    GlobalState.setisSkyplot(true);  
    logFiles = new LogFiles();
    init();
  }


  /**
   * init function
   * 
   * This function inititalizes the Skyplot View. 
   * Displays the Time Slider the TextViews for the 
   * different categories of the satellites.
   * Checks if external Bluetooth Receiver is connected, if not 
   * adds a listener to get position from the internal receiver of 
   * the device.Calls an Async Task to get satellite data from NORAD.
   **/
  public void init() {
   
    skyplotStatusSharedPreferences = getSharedPreferences(
        KEY_SATELLITE_SHARED_PREF, MODE_WORLD_READABLE);

    prefEditor = skyplotStatusSharedPreferences.edit();

    skyplotView = (SkyplotView) this.findViewById(R.id.skyplotView);

    timeSlider = (SeekBar) this.findViewById(R.id.timeSlider);
    timeSlider.setMax(TIME_SLIDER_MAX);
    timeSlider.setProgress(TIME_SLIDER_PROGRESS);
    timeSlider.setOnSeekBarChangeListener(timeSliderListener);

    msgTextView = (TextView) this.findViewById(R.id.msgtextView);

    gpsPos_count = (TextView) this.findViewById(R.id.gpsPostextView);
    gpsPos_count.setText("GPS Pos:0");

    egnosPos_count = (TextView) this.findViewById(R.id.egnosPostextView);
    egnosPos_count.setText("EGNOS Pos:0");

    rndPos_count = (TextView) this.findViewById(R.id.rndPostextView);
    rndPos_count.setText("*R&D Pos:0");

    sbasSat_count = (TextView) this.findViewById(R.id.sbastextView);
    sbasSat_count.setText("SBAS:0");

    notused_count = (TextView) this.findViewById(R.id.notUsedtextView);
    notused_count.setTextColor(Color.GRAY);
    notused_count.setText("Not Used:0");

    ImageView arView = (ImageView) this.findViewById(R.id.arView);
    arView.setOnClickListener(arViewListener);
    

    
    try {
    if (GlobalState.getSocket() == null) {
      Log.d(TAG_SYKPLOT_STATUS, "Getting satellite details from GPS of device");
      fromBTReceiver = false;

      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      gpslocListener = new GPSLocationListener();
      
      gpsListener = new GpsListener();
      
      if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
        displayAlertMessageNoGps();
      }else {
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = locationManager.getBestProvider(crit, false);

        locationManager.addGpsStatusListener(gpsListener);
        locationManager.requestLocationUpdates(bestProvider, 0,
          0, gpslocListener);
        
        if (GlobalState.getNORADData() == null) {
          GetNORADData getNORADData = new GetNORADData();
          getNORADData.execute("");
          showDialog(0);
        }else {
          updateSatelliteData();      
          updateSkyplotHandler = new Handler();
          updateSkyplotHandler.postDelayed(UpdateSkyplot, 100);
        }    
       }
    } else {    
      Log.d(TAG_SYKPLOT_STATUS, "SkyplotStatus | Getting satellite details"
          + " from BT receiver");
      fromBTReceiver = true;
      if (GlobalState.getNORADData() == null) {
        GetNORADData getNORADData = new GetNORADData();
        getNORADData.execute("");
        showDialog(0);
      } else {
        showDialog(0);
        updateSatelliteData();
        updateSkyplotHandler = new Handler();
        updateSkyplotHandler.postDelayed(UpdateSkyplot, 100);
      }
    }
    }catch(Exception e) {
      logFiles.logError("SkyplotStatus | Error occurred while creating Skyplot View: "+e);
    }
    setSatelliteInformation();
  }
  
  /**
   * onPrepareOptionsMenu function 
   * 
   * Creates a menu with Hide Satellite Path/ Show Satellite Path
   * @param menu         The menu list to create.
   * @return TRUE if click was handled,else FALSE.
   */
  @Override
  public final boolean onPrepareOptionsMenu(final Menu menu) {
    menu.clear();    
   if(skyplotStatusSharedPreferences.getBoolean(KEY_SATELLITEPATH_CHECKED, true) == true) {
     menu.add(0, 0, 0, "Hide satellite path");
   }
   else {
     menu.add(0, 1, 0, "Show satellite path");
   }
    return super.onPrepareOptionsMenu(menu);
  }
  
  /**
   * onOptionsItemSelected function 
   * 
   * Calls functions when an item in the options menu is selected.
   * @param item               The menu item.
   * @return TRUE if click was handled,else FALSE.
   **/
  @Override
  public final boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
    case 0:
      prefEditor.putBoolean(KEY_SATELLITEPATH_CHECKED, false);
      prefEditor.commit();
      skyplotView.postInvalidate();
      return true;
    case 1:    
      prefEditor.putBoolean(KEY_SATELLITEPATH_CHECKED, true);
      prefEditor.commit();
      skyplotView.postInvalidate();
      return true;    
    default:
      return super.onOptionsItemSelected(item);
    }
  }
  
  /** 
   * onPause function
   * 
   * Called as part of the activity lifecycle 
   * when an activity is going into the background, 
   * but has not (yet) been killed 
   * Removes any listeners added to get position 
   * from internal receiver of the device.
   * Removes the callback for UpdatSykplotpltHandler. 
   */
  @Override
  public void onPause() {
    super.onPause();
    isPaused = true;
    Log.d(TAG_SYKPLOT_STATUS, "Syplot paused");
    GlobalState.setisSkyplot(false);

    if (null != updateSkyplotHandler) {
      updateSkyplotHandler.removeCallbacks(UpdateSkyplot);
      updateSkyplotHandler = null;
    }

    if (!fromBTReceiver) {
      if (null != locationManager) {
        locationManager.removeUpdates(gpslocListener);
        locationManager.removeGpsStatusListener(gpsListener);
      }
    }
  }
  
  
  /** 
   * onResume function
   *
   * Called after onPause has been called
   */
  @Override
  public void onResume() {
    super.onResume();
    
    Log.d(TAG_SYKPLOT_STATUS, "Syplot resumed");    
    if(isPaused) {
      isPaused = false;
    if(!fromBTReceiver) {
      if (locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER)) {  
        locationManager.addGpsStatusListener(gpsListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
          0, gpslocListener);

        if (GlobalState.getNORADData() == null) {
          GetNORADData getNORADData = new GetNORADData();
          getNORADData.execute("");
          showDialog(0);
        }else 
          updateSatelliteData();
      }else {
        Toast.makeText(getBaseContext(),
            "Please turn ON GPS or Bluetooth Receiver to use Skyplot feature.",
            Toast.LENGTH_LONG).show();
        SkyplotStatus.this.finish();
      }
    }else {
      if (GlobalState.getNORADData() == null) {
        GetNORADData getNORADData = new GetNORADData();
        getNORADData.execute("");
        showDialog(0);
      }else {
      updateSkyplotHandler = new Handler();
      updateSkyplotHandler.postDelayed(UpdateSkyplot, 100);
      }
    }
   }
  }

  /** 
   * displayAlertMessageNoGps function
   * 
   * Displays a popup message when the user did not enable the GPS receiver 
   * of the device.
   */
  private void displayAlertMessageNoGps() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder
        .setMessage(
            "GPS seems to be disabled, would you like to enable it to further use Skyplot?")
        .setCancelable(false)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
           }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
            Toast
                .makeText(
                    getBaseContext(),
                    "Please turn ON GPS or Bluetooth Receiver to use Skyplot feature.",
                    Toast.LENGTH_LONG).show();
            SkyplotStatus.this.finish();
          }
        });
    final AlertDialog alert = builder.create();
    alert.show();
  }

  /** 
   * setSatelliteInformation function
   * 
   * Displays the text views for the different satellites 
   * displayed on the skyplot view. In case no external Bluetooth
   * receiver is connected, theses text views will be disabled and a 
   * text will be displayed indicating to connect to a receiver 
   * to get more information about the satellites.
   */
  private void setSatelliteInformation() {
    if (fromBTReceiver) {
      msgTextView.setVisibility(View.GONE);

      gpsPos_count.setOnClickListener(gpsPosListener);
      if ((gpsPos_selected = skyplotStatusSharedPreferences.getInt(
          KEY_GPSPOS_SELECTED, 1)) == 0)
        gpsPos_count.setTextColor(getResources().getColor(Color.DKGRAY));

      egnosPos_count.setOnClickListener(egnosPosListener);
      if ((egnosPos_selected = skyplotStatusSharedPreferences.getInt(
          KEY_EGNOSPOS_SELECTED, 1)) == 0)
        egnosPos_count.setTextColor(getResources().getColor(Color.DKGRAY));

      rndPos_count.setOnClickListener(rndPosListener);

      if ((rndPos_selected = skyplotStatusSharedPreferences.getInt(
          KEY_RnDPOS_SELECTED, 1)) == 0)
        rndPos_count.setTextColor(getResources().getColor(Color.DKGRAY));

      sbasSat_count.setOnClickListener(sbasSatListener);

      if ((sbasSat_selected = skyplotStatusSharedPreferences.getInt(
          KEY_SBASSATELLITE_SELECTED, 1)) == 0)
        sbasSat_count.setTextColor(getResources().getColor(Color.DKGRAY));

      notused_count.setOnClickListener(notUsedListener);
      if ((notUsed_selected = skyplotStatusSharedPreferences.getInt(
          KEY_NOTUSED_SELECTED, 1)) == 0)
        notused_count.setTextColor(getResources().getColor(Color.DKGRAY));
    } else {
      msgTextView.setBackgroundColor(Color.TRANSPARENT);
      msgTextView.setTextColor(Color.RED);

      gpsPos_count.setEnabled(false);
      gpsPos_count.setTextColor(Color.DKGRAY);

      egnosPos_count.setEnabled(false);
      egnosPos_count.setTextColor(Color.DKGRAY);

      rndPos_count.setEnabled(false);
      rndPos_count.setTextColor(Color.DKGRAY);

      sbasSat_count.setEnabled(false);
      sbasSat_count.setTextColor(Color.DKGRAY);
      
      notused_count.setEnabled(false);
      notused_count.setTextColor(Color.DKGRAY);
    }
  }

  /** 
   * GPSLocationListener class
   * 
   * Class called by the LocationManager when the location has changed. 
   * These methods are called if the LocationListener has been registered
   * with the location manager service.
   */
  private class GPSLocationListener implements LocationListener {

    /** 
     * onLocationChanged function
     * 
     * Called when location has been changed.
     * @param location    the new location, as a location object.
     */
    @Override
    public void onLocationChanged(Location location) {
      Log.d(TAG_SYKPLOT_STATUS, "onLocatinChanged | Location updated");
      gpsLocationAvailable = true;
      gpsCoordinates[0] = location.getLatitude();
      gpsCoordinates[1] = location.getLongitude();
      gpsCoordinates[2] = location.getAltitude();
      gpsTOW = location.getTime();
      gpsWeekNum = 0;

      if (!isARClicked) {
        if (onStart) {
          locationManager.removeUpdates(gpslocListener);
          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
              0, 0, gpslocListener);
          oldTimeOne = gpsTOW;
          oldTimeTen = gpsTOW;
          progressBarDialog.setMessage("Loading Data...");
          satelliteDetails = SatelliteData.getSatelliteDetails(
              satelliteDatafromNORAD, gpsCoordinates, gpsTOW, gpsWeekNum,
              fromBTReceiver, true);

          skyplotView.setSatelliteData(satelliteDetails, TIME_SLIDER_PROGRESS,
              0);

          skyplotView.postInvalidate();

          if (null != progressBarDialog)
            if (progressBarDialog.isShowing())
              progressBarDialog.dismiss();

        } else {
          if(!timeSliderUsed || !gpsPosClicked || !egnosPosClicked || !rndPosCLicked
              || !sbasClicked || !notUsedClicked)
            updateSatelliteData();
        }
      }
    }


    /** 
     * onProviderDisabled function
     * 
     * Called when provider is disabled by user
     * @param provider    the provider that is disabled.
     */
    @Override
    public void onProviderDisabled(String provider) {
      Log.e(TAG_SYKPLOT_STATUS, "onProviderDisabled | providerdisabled: "+provider);
    }

    /** 
     * onProviderEnabled function
     * 
     * Called when provider is enabled by user
     * @param provider    the provider that is enabled.
     */
    @Override
    public void onProviderEnabled(String provider) {
      Log.d(TAG_SYKPLOT_STATUS, "onProviderEnabled | providerEnabled: "+provider);
    }


    /** 
     * onStatusChanged function
     * 
     * Called when the provider status changes.
     * @param  provider    the provider
     * @param  status      OUT_OF_SERVICE if the provider is out of service, 
     *                     and this is not expected to change in the near future;
     *                     TEMPORARILY_UNAVAILABLE if the provider is temporarily 
     *                     unavailable but is expected to be available shortly;
     *                     and AVAILABLE if the provider is currently available. 
     *                     extras  an optional Bundle which will contain provider
     *                     specific status variables.
     * @param  extras      A number of common key/value pairs for the extras Bundle 
     *                     are listed below. Providers that use any of the keys on 
     *                     this list must provide the corresponding value as described below. 
     *                     satellites - the number of satellites used to derive the fix.
     *                     
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      Log.d(TAG_SYKPLOT_STATUS, "onstatusChanged | provider: "+provider);
      
    }
  }

  /** 
   * GpsListener class
   * 
   * Called when status of GPS engine has changed
   */
  private class GpsListener implements GpsStatus.Listener {

    /** 
     * onGpsStatusChanged function
     * 
     * Called to report when status of GPS engine has changed.
     * @param   event     the event that occured.
     */
    @Override
    public void onGpsStatusChanged(int event) {
   
      Log.d(TAG_SYKPLOT_STATUS, "Getting GPS Data");
      GpsStatus status = locationManager.getGpsStatus(null);
      Iterable<GpsSatellite> sats = status.getSatellites();
      double[][] satelliteTypes = new double[status.getMaxSatellites()][4];
      // Check number of satellites in list to determine fix state
      Iterator<GpsSatellite> it = sats.iterator();
      int sat = 0;
      while (it.hasNext()) {
        GpsSatellite gpsSat = (GpsSatellite) it.next();
        if (gpsSat.usedInFix() == true) {
          satelliteTypes[sat][0] = gpsSat.getPrn();//PRN
          satelliteTypes[sat][1] = 1;//indicating satellite used to get GPS location.
          satelliteTypes[sat][2] = 0;// Distance
          satelliteTypes[sat][3] = gpsSat.getSnr();//SNR
        }
        sat++;
      }
      GlobalState.setGPSSatelliteType(satelliteTypes);      
     
      if(gpsLocationAvailable == false) {
        countWait++;
        Log.d(TAG_SYKPLOT_STATUS, "countWait: "+countWait);
        if(countWait >= 30) {
          Toast.makeText(context, "Current location not available \n(GPS/EGNOS position is required)", Toast.LENGTH_SHORT).show();
          Log.d(TAG_SYKPLOT_STATUS, "Current location not available \n(GPS/EGNOS position is required)");
          GlobalState.setNORADData(null);
          SatelliteData.hasDataFromNORAD = false;
          SkyplotStatus.this.finish();
        }
      }
      
    }
  }
  
  /**
   * GetNORADData class 
   * 
   * An async task which would perform some actions in the background 
   * without interrupting the UI.Gets the satellite data from NORAD.
   **/
  private class GetNORADData extends AsyncTask<String, Void, String[][]> {

    /**
     * doInBackground function 
     * 
     * Initiates background process to get satellite data from NORAD.
     * @param  params                      The parameters of this task.
     * @return satelliteDatafromNORAD      A table with all satellite data from NORAD.
     **/
    @Override
    protected String[][] doInBackground(String... params) {
      noradError = SatelliteData.getNORADData(satelliteDatafromNORAD);
      satelliteDatafromNORAD = GlobalState.getNORADData();
      return satelliteDatafromNORAD;
    }

    /**
     * onPostExecute function 
     * 
     * Function is called after the background process is completed.
     * Gets the current location from the external Bluetooth receiver;
     * otherwise adds a location listener to get current position from
     * the internal receiver of the device.
     * @param noradData      A table with all satellite data from NORAD.
     **/
    @Override
    protected void onPostExecute(String[][] noradData) {
      super.onPostExecute(noradData);
      try {

        if (noradError == 0) {
          if (fromBTReceiver) { // From Bluetooth receiver
            while (GlobalState.getPosition()[0] == 0) {
              Log.d(TAG_SYKPLOT_STATUS,
                  "GetNORADData onPostExecute | Waiting for position from BT Receiver");
            }
            progressBarDialog.setMessage("Loading Data...");
            gpsCoordinates[0] = GlobalState.getPosition()[0];
            gpsCoordinates[1] = GlobalState.getPosition()[1];
            gpsCoordinates[2] = GlobalState.getPosition()[2];
            gpsTOW = GlobalState.getGPSTOW();

            gpsWeekNum = GlobalState.getGPSWN();

            oldTimeOne = gpsTOW;
            oldTimeTen = gpsTOW;
            progressBarDialog.setMessage("Loading Data...");
            satelliteDetails = SatelliteData.getSatelliteDetails(
                satelliteDatafromNORAD, gpsCoordinates, gpsTOW, gpsWeekNum,
                fromBTReceiver, true);

            skyplotView.setSatelliteData(satelliteDetails,
                TIME_SLIDER_PROGRESS, 0);

            skyplotView.postInvalidate();

            if (null != progressBarDialog)
              if (progressBarDialog.isShowing())
                progressBarDialog.dismiss();

            updateSkyplotHandler = new Handler();
            updateSkyplotHandler.postDelayed(UpdateSkyplot, 100);
          } else
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, gpslocListener);
        } else {
          showErrorMessages(noradError);
          GlobalState.setNORADData(null);
          SatelliteData.hasDataFromNORAD = false;
          SkyplotStatus.this.finish();
        }
      } catch (Exception e) {
        logFiles
            .logError("SkyplotStatus | Error occurred after getting NORAD data : "
                + e);
      }
    }
  }

  /**
   * UpdateSkyplot runnable 
   * 
   * Calls the function updatesatellite data to update the Sykplot View
   * for every 100ms.
   **/
  public static Runnable UpdateSkyplot = new Runnable() {

    @Override
    public void run() {
      
      
      if(fromBTReceiver) {
       gpsTOW = GlobalState.getGPSTOW();
      }
      // update Skyplot View only if Time slider is not in use
      if (!timeSliderUsed || !gpsPosClicked || !egnosPosClicked || !rndPosCLicked
          || !sbasClicked || !notUsedClicked)
        updateSatelliteData();
      if(updateSkyplotHandler != null)  
       updateSkyplotHandler.postDelayed(this, 100);
    }
  };

  /**
   * updateSatelliteData function 
   * 
   * Calls the onDraw function of the Skyplot View class to update the Skyplot.
   * The Skyplot View is updated every 100ms. After 1 minute the count Time 
   * is increased by 1, to indicate that 1 minute is completed and to read the 
   * azimuth and elevation for the next minute.
   * After 10 minutes the satellite azimuth and elevation is recomputed.
   **/
  public static void updateSatelliteData() {
    Log.d(TAG_SYKPLOT_STATUS, "SkyplotStatus | Updating Skyplot View");
    
   try { 
    if (fromBTReceiver) {
      if (GlobalState.getSocket() == null || GlobalState.getErrorWhileReadingBT() == -1) {
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
        if (null != updateSkyplotHandler ) {
          updateSkyplotHandler.removeCallbacks(UpdateSkyplot);
          updateSkyplotHandler = null;
        }
        for(int c = 0; c < 32; c++) {
          Arrays.fill(SkyplotView.gpsSatelliteTypes[c],0.0);
          Arrays.fill(SkyplotView.egnosSatelliteTypes[c], 0.0);
          Arrays.fill(SkyplotView.rndSatelliteTypes[c], 0.0);
        }
        GlobalState.setGPSSatelliteType(SkyplotView.gpsSatelliteTypes);
        GlobalState.setEGNOSSatelliteType(SkyplotView.egnosSatelliteTypes);
        GlobalState.setRnDSatelliteType(SkyplotView.rndSatelliteTypes);
       Log.e(TAG_SYKPLOT_STATUS, "SkyplotStatus | Receiver is disconnected");
      }   
    }
    
    onStart = false;
    timeDifferenceOne = gpsTOW - oldTimeOne;
    timeDifferenceTen = gpsTOW - oldTimeTen;
    if (fromBTReceiver) {
      timeDifferenceOne = timeDifferenceOne * 1000;//from seconds to ms
      timeDifferenceTen = timeDifferenceTen * 1000;//from seconds to ms
    }
    
    if ((int) timeDifferenceTen > 600000) {// calculate satellite position after 10 minutes or 600000ms
      countTime = 0;
      if (fromBTReceiver) {// if location is from Bluetooth Receiver
        gpsCoordinates[0] = GlobalState.getPosition()[0];
        gpsCoordinates[1] = GlobalState.getPosition()[1];
        gpsCoordinates[2] = GlobalState.getPosition()[2];
        gpsTOW = GlobalState.getGPSTOW();
        gpsWeekNum = GlobalState.getGPSWN();
      }
      oldTimeTen = gpsTOW;
      Log.d(TAG_SYKPLOT_STATUS,
          "SkyplotStatus | Updating satellite positions from Bluetooth receiver");
      satelliteDetails = SatelliteData.getSatelliteDetails(
          satelliteDatafromNORAD, gpsCoordinates, gpsTOW, gpsWeekNum,
          fromBTReceiver, false);
    }

    if(fromBTReceiver) {
      if((int)timeDifferenceOne > 60000) {// 1 minute or 60000 ms
        countTime = countTime + 2;
        oldTimeOne = gpsTOW;
      }
    }else {
      if((int)timeDifferenceOne > 60000) {// 1 minute or 60000 ms
        countTime = countTime + 2;
        oldTimeOne = gpsTOW;
      }
    }

    skyplotView.setSatelliteData(satelliteDetails, TIME_SLIDER_PROGRESS,
        countTime);
    skyplotView.postInvalidate();
    
    if(progressBarDialog != null)
     if(progressBarDialog.isShowing())
      progressBarDialog.dismiss();
   } catch(Exception e) {
     logFiles.logError("SkyplotStatus | Error occurred while updating Skyplot View: "+e);
   }
  }

  /**
   * showErrorMessages function 
   * 
   * Displays error messages when data from NORAD is requested.
   * @param noradError    error number from NORAD
   **/
  public void showErrorMessages(int noradError) {
    switch (noradError) {
    case -2:
      Toast
          .makeText(
              getBaseContext(),
              "Unable to get data, check your network connection or please try again after a while.",
              Toast.LENGTH_LONG).show();
      break;
    case -3:
      Toast.makeText(getBaseContext(),
          "Error while getting data, please try again after a while.",
          Toast.LENGTH_LONG).show();
      break;
    }
  }

  /**
   * onCreateDialog Dialog.
   * 
   * Creates a progress bar dialog based on the id. Called when showDialog is
   * called.
   * 
   * @param id          id of the progress bar to be created.
   * @return Dialog the progress bar dialog created.
   */
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case 0:// Spinner
      progressBarDialog = new ProgressDialog(this);
      progressBarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      progressBarDialog.setCancelable(false);
      if (!SatelliteData.hasDataFromNORAD)
        progressBarDialog.setMessage("Downloading data...\n(GPS/EGNOS position required)");
      else if (SatelliteData.hasAllSatelliteDetails)
        progressBarDialog.setMessage("Loading data...");
      return progressBarDialog;
    default:
      return null;
    }
  }

  /**
   * arViewListener onClickListener.
   * 
   * Called when AR View button is clicked
   */
  View.OnClickListener arViewListener = new OnClickListener() {

    /**
     * onClick function.
     * 
     * Called when AR View button is clicked.
     * Checks if Acclerometer and Orientation sensors are available 
     * on the device.
     * Starts the activity ARVIew.
     * @param  v     the view that was clicked.
     */
    @Override
    public void onClick(View v) {
     try {
      isARClicked = true;    
      int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.GINGERBREAD) {

          PackageManager manager = getPackageManager();
          boolean hasAccelerometer = manager
              .hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
          boolean hasCompass = manager
              .hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
          if (hasAccelerometer || hasCompass) {
            Intent arViewIntent = new Intent(SkyplotStatus.this, ARView.class);
            startActivity(arViewIntent);
          } else
            Toast.makeText(getBaseContext(),
                "Live video streaming is not supported on your device.",
                Toast.LENGTH_LONG).show();
        } else
          Toast.makeText(getBaseContext(),
              "Live video streaming is not supported on your device.",
              Toast.LENGTH_LONG).show();

    }catch(Exception e) {
      logFiles.logError("ARView | Error occurred while opening AR View: "+e);
    }
     
    }};
  

    /**
     * timeSliderListener function.
     * 
     * Called when time slider is used.
     */
  OnSeekBarChangeListener timeSliderListener = new OnSeekBarChangeListener() {

    /**
     * onStopTrackingTouch function.
     * 
     * Notification that the user has finished a touch gesture. 
     * Clients may want to use this to re-enable advancing the seekbar. 
     * @param  seekBar       The SeekBar in which the touch gesture began.
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * onStartTrackingTouch function.
     * 
     * Notification that the user has started a touch gesture.
     * Clients may want to use this to disable advancing the seekbar. 
     * @param  seekBar       The SeekBar in which the touch gesture began.
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * onProgressChanged function.
     * 
     *  Notification that the progress level has changed. Clients can use
     *  the fromUser parameter to distinguish user-initiated changes from those that occurred programmatically.
     *  @param  seekBar       The SeekBar in which the touch gesture began.
     *  @param  progress      The current progress level.
     *  @param  fromUser      True if the progress change was initiated by the user.
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
        boolean fromUser) {
      Log.d(TAG_SYKPLOT_STATUS, "SkyplotStatus | Time slider value: "
          + progress);
      timeSliderUsed = true;
      TIME_SLIDER_PROGRESS = progress;
      skyplotView.setSatelliteData(satelliteDetails, progress, countTime);
      skyplotView.postInvalidate();
      timeSliderUsed = false;
    }
  };


  View.OnClickListener gpsPosListener = new OnClickListener() {

    @Override
    public void onClick(View v) {
      Log.d(TAG_SYKPLOT_STATUS, "SkyplotStatus | Clicked on GPS Pos ");
      gpsPosClicked = true;
      if (gpsPos_selected == 1) {// selected
        gpsPos_count.setTextColor(Color.DKGRAY);
        gpsPos_selected = 0;
      } else {
        gpsPos_count.setTextColor(getResources().getColor(R.color.Slate_Blue));
        gpsPos_selected = 1;
      }
      prefEditor.putInt(KEY_GPSPOS_SELECTED, gpsPos_selected);
      prefEditor.commit();
      skyplotView.postInvalidate();
      gpsPosClicked = false;
    }
  };

  View.OnClickListener egnosPosListener = new OnClickListener() {

    @Override
    public void onClick(View v) {
      Log.d(TAG_SYKPLOT_STATUS, "SkyplotStatus | Clicked on EGNOS Pos ");
      egnosPosClicked = true;
      if (egnosPos_selected == 1) {// selected
        egnosPos_count.setTextColor(Color.DKGRAY);
        egnosPos_selected = 0;
      } else {
        egnosPos_count
            .setTextColor(getResources().getColor(R.color.Lime_Green));
        egnosPos_selected = 1;
      }
      prefEditor.putInt(KEY_EGNOSPOS_SELECTED, egnosPos_selected);
      prefEditor.commit();
      skyplotView.postInvalidate();
      egnosPosClicked = false;
    }
  };

  View.OnClickListener rndPosListener = new OnClickListener() {

    @Override
    public void onClick(View v) {
      Log.d(TAG_SYKPLOT_STATUS, "SkyplotStatus | Clicked on R&D Pos ");
      rndPosCLicked = true;
      if (rndPos_selected == 1) {// selected
        rndPos_count.setTextColor(Color.DKGRAY);
        rndPos_selected = 0;
      } else {
        rndPos_count.setTextColor(getResources().getColor(R.color.Magenta));
        rndPos_selected = 1;
      }
      prefEditor.putInt(KEY_RnDPOS_SELECTED, rndPos_selected);
      prefEditor.commit();
      skyplotView.postInvalidate();
      rndPosCLicked = false;
    }
  };

  View.OnClickListener sbasSatListener = new OnClickListener() {

    @Override
    public void onClick(View v) {
      Log.d(TAG_SYKPLOT_STATUS, "SkyplotStatus | Clicked on SBAS ");
      sbasClicked = true;
      if (sbasSat_selected == 1) {// selected
        sbasSat_count.setTextColor(Color.DKGRAY);
        sbasSat_selected = 0;
      } else {
        sbasSat_count.setTextColor(getResources().getColor(R.color.Red));
        sbasSat_selected = 1;
      }
      prefEditor.putInt(KEY_SBASSATELLITE_SELECTED, sbasSat_selected);
      prefEditor.commit();
      skyplotView.postInvalidate();
      sbasClicked = false;
    }
  };

  View.OnClickListener notUsedListener = new OnClickListener() {

    @Override
    public void onClick(View v) {

      Log.d(TAG_SYKPLOT_STATUS, "SkyplotStatus | Clicked on Not Used ");
      notUsedClicked = true;
      if (notUsed_selected == 1) {// selected
        notused_count.setTextColor(Color.DKGRAY);
        notUsed_selected = 0;
      } else {
        notused_count.setTextColor(Color.GRAY);
        notUsed_selected = 1;
      }
      prefEditor.putInt(KEY_NOTUSED_SELECTED, notUsed_selected);
      prefEditor.commit();
      skyplotView.postInvalidate();
      notUsedClicked = false;
    }
  };
  
}

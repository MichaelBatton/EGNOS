/**
 * @file EgnosDemoAppMain.java
 *
 * Map Page displayed after the Splash screen.
 * Displays the Google Map and a menu with the functionalities
 * of the EGNOS Demo App.
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
 * 
 * @mainpage The EGNOS SDK
 * <b>EGNOS</b> is the European satellite-based augmentation system (SBAS),
 * an infrastructure that consists of three geostationary satellites and a network 
 * of ground stations. The system improves, in Europe, the accuracy of the open 
 * public service offered by the Global Positioning System (GPS) by providing 
 * corrections of GPS satellites clocks and orbits, and of the error caused by 
 * the ionosphere. 
 * 
 * <b>SISNeT</b> is a server that provides over the Internet the same EGNOS 
 * corrections as if they would have been received from the satellites. 
 * 
 * The <b>EGNOS SDK</b> has been designed to allow application developers to 
 * take advantage of the EGNOS benefits, and to use them for the software they 
 * develop for mobile devices. The open-source library in the EGNOS SDK offers the 
 * possibility to include EGNOS corrections for a more accurate position, as well
 * as "Integrity". Integrity gives an estimation of the confidence in the calculation
 * of the position provided by the system along with alerts in real time (less than 
 * six seconds) of any shortcomings in the reliability of GPS positioning signals. 
 *  
 * The <b>Demonstration Application</b> shows the main features of the EGNOS SDK at
 * work, providing application developers with examples on how the EGNOS library can
 * be used and showing the benefits of the EGNOS corrections on positioning.
 * 
 * The <b>EGNOS SDK Interface</b> provides the necessary functionalities for interfacing 
 * the GUI with the software receiver. 
 * 
 * For additional information on the EGNOS SDK please visit
 *  <a>http://www.egnos-portal.eu/</a>
 *
 **/
package com.ec.egnosdemoapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ec.R;
import com.ec.egnossdk.BluetoothConnect;
import com.ec.egnossdk.GlobalState;
import com.ec.egnossdk.LogFiles;
import com.ec.egnossdk.SISNeT;
import com.ec.egnossdk.UtilsDemoApp;
import com.ec.egnossdk.uBlox;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * Class that displays the Google Map, the menu with Current Location,
 * Get Directions, Start Tracking/Stop Tracking, Clear Map,
 * Settings, About and Exit.Gets the current GPS and EGNOS 
 * position from the background process. Adds the GPS and EGNOS 
 * position to the Google Map.Updates the current position based on 
 * Minimum Time and Minimum Distance from Settings.Gets the start 
 * point and end point from UI and obtains a route path. Resets the 
 * Google Map on Clear Map.Creates a log file. Closes Bluetooth 
 * connection and the log file.
 **/
public class EgnosDemoAppMain extends MapActivity {
  private MapView mapView;
  MapController mapController;
  List<Overlay> listOverlay;
  List<Overlay> integrityListOverlay;
  Drawable gpsdrawable;
  Drawable egnosdrawable;
  Drawable gpsegnosdrawable;
  Drawable rnddrawable;
  MapOverlay mapItemizedOverlay;
  MapOverlay egnosmapItemizedOverlay;
  MapOverlay gpsegnosmapItemizedOverlay;
  MapOverlay rndmapItemizedOverlay;
  GeoPoint gpsPoint;
  GeoPoint egnosPoint;
  GeoPoint rndPoint;
  GeoPoint nullPoint = new GeoPoint((int) (0), (int) (0));
  double gpsLatitude;
  double gpsLongitude;
  double egnosLatitude;
  double egnosLongitude;
  double rndLatitude;
  double rndLongitude;
  String source;
  String destination;
  Location gpsLocation;
  Geocoder coder;
  LocationManager locManager;
  String strLocationProvider;
  /** Start/stop tracking, if FALSE no tracking. */
  public static boolean isTrack = false;
  /** Get route from start to end point, if FALSE no direction is requested. */
  public boolean isRoute = false;
  /** Current location if FALSE no current location is requested. */
  public static boolean isCurrent = false;
  public static int isLogFile = 0;
  boolean isClear = false;
  boolean isExit = false;
  OverlayItem overlayItemGPS;
  OverlayItem overlayItemEGNOS;
  OverlayItem overlayItemRnD;
  float locationAccuracy;
  IntegrityOverlay integrityOverlay;
  EditText sourceEdit, destinationEdit;
  long minTime;
  float minDistance, integrityValue;
  String bluetoothDeviceAddress;
  BluetoothDevice device;
 
  int zoomLevel;
  public static int EGNOS = 0;
  public static int SISNET = 0;
  public static final String KEY_MIN_TIME = "minTimeKey";
  public static final String KEY_MIN_DISTANCE = "minDistanceKey";
  public static final String KEY_INTEGRITY_VALUE = "integrityValueKey";
  public static final String START_POINT = "startPoint";
  public static final String END_POINT = "endPoint";
  public static final String START_POINT_EDIT = "startPointEdit";
  public static final String END_POINT_EDIT = "endPointEdit";
  public static final String IS_TRACK = "isTrack";
  public static final String IS_ROUTE = "isRoute";
  public static final String IS_ROUTE_NEW_STATE = "isRouteNewState";
  public static final String IS_CURRENT = "isCurrent";
  public static final String IS_CLEAR = "isClear";
  public static final String DIRECTION_OVERLAY = "directionOberlay";
  public static final String ZOOM_LEVEL = "zoomLevel";
  public static final String GPS_TRACK_POINTS_LATITUDE = "gpstrackPointsLat";
  public static final String GPS_TRACK_POINTS_LONGITUDE = "gpstrackPointsLong";
  public static final String EGNOS_TRACK_POINTS_LATITUDE = "egnostrackPointsLat";
  public static final String EGNOS_TRACK_POINTS_LONGITUDE = "egnostrackPointsLong";
  public static final String ROUTE_POINTS_LATITUDE = "routePointsLat";
  public static final String ROUTE_POINTS_LONGITUDE = "routePointsLong";
  public static final String HPL_VALUE = "hplValue";
  ArrayList<String> gpstrackPointsLat = new ArrayList<String>();
  ArrayList<String> gpstrackPointsLong = new ArrayList<String>();
  ArrayList<String> egnostrackPointsLat = new ArrayList<String>();
  ArrayList<String> egnostrackPointsLong = new ArrayList<String>();
  ArrayList<String> routePointsLat = new ArrayList<String>();
  ArrayList<String> routePointsLong = new ArrayList<String>();
  ArrayList<String> displayValue = new ArrayList<String>();
  double HPL;
  double HPLValue;
  double[] coordinates = new double[10];
  public static final int MESSAGE_TOAST = 1;
  public static final String TOAST = "toast";
  Handler locationHandler;
  public static final int WRITE_MESSAGE = 2;
  public static final String MESSAGE2830 = "m28m30";
  String m28 = new String("Message 28: ");
  String m30 = new String("Message 30: ");
  File root = Environment.getExternalStorageDirectory();
  File errorLogfile;
  File internalLogFile;
  File positionLogFile;
  FileWriter errorLogFileWriter;
  FileWriter internalLogFilewriter;
  FileWriter positionLogFilewriter;
  public static final String KEY_BLUETOOTH_PREF = "bluetoothPrefKey";
  public static final String KEY_SOCKET = "socketKey";
  String sisnetUsername;
  String sisnetPassword;
  /** Variable to easily identify relevant messages. */
  private static final String TAG = "EGNOS-SDK";
  private static int RECEIVER_TYPE = 0;
  NotificationManager notificationManager;
  Intent notifyIntent;
  PendingIntent appIntent;
  Notification egnosNotification;
  Notification satellitenetNotification;
  Notification edasNotification;
  ProgressBarThread progressBarThread;
  ProgressDialog progressBarDialog;
  boolean egnosPositionCheck = false;
  AlertDialog directionsDisplay;
  long oldTime;
  double oldLatitude = 0;
  double oldLongitude = 0;
  static long updateTime = 0;
  LogFiles log;
  public static String textToDisplay = null;
  TextView positionToDisplayText;
  RelativeLayout displayRelativeLayout;
  ImageView clearMapImage;
  static boolean onStart = false;
  
  /**
   * onCreate function
   * 
   * Called when the activity is first created. Loads a map view with 
   * Google Map. Displays an alert if the mobile device is not connected 
   * to a network.
   * @param savedInstanceState    The bundle of any saved instances.
   **/
 
  @Override
  public final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

//    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
//        .permitAll().build();
//    StrictMode.setThreadPolicy(policy);
    
    init();
    log = new LogFiles();
    createLogFile(); 

    if (checkNetwork() == 0) {
        new AlertDialog.Builder(this)
            .setTitle("Connect to Network")
            .setMessage(
                "Please connect to a network to "
                    + "continue using this application")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              public void onClick(final DialogInterface dialog, final int which) {
                startNetwork();
              }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
              public void onClick(final DialogInterface dialog, final int which) {
                // closes AlertDialog
              }
            }).show();
      }
    
    if (savedInstanceState != null) {
      applicationStateChange(savedInstanceState);
    }
  }

  /**
   * init function
   * 
   * This function initializes Map View, and other classes to
   * display location point, draw route and position integrity display circle/s
   * Creates a log file named gpxfile+"current date".gpx.
   * GlobalState.setisLogFile(isLogFile):
   * isLogFile has to be set to 1 if the developer needs a log file, 
   * otherwise 0.
   **/
  private void init() {
    gpsPoint = new GeoPoint((int) (0), (int) (0));
    egnosPoint = new GeoPoint((int) (0), (int) (0));
    mapView = (MapView) this.findViewById(R.id.mapView);
    listOverlay = mapView.getOverlays();
    integrityListOverlay = mapView.getOverlays();
    gpsdrawable = this.getResources().getDrawable(R.drawable.ic_gps_location);
    egnosdrawable = this.getResources().getDrawable(
        R.drawable.ic_egnos_location);
    gpsegnosdrawable = this.getResources().getDrawable(R.drawable.
        ic_gps_egnos_location);
    rnddrawable = this.getResources().getDrawable(R.drawable.ic_rnd_location);
    
    mapItemizedOverlay = new MapOverlay(gpsdrawable);
    egnosmapItemizedOverlay = new MapOverlay(egnosdrawable);
    gpsegnosmapItemizedOverlay = new MapOverlay(gpsegnosdrawable);
    rndmapItemizedOverlay = new MapOverlay(rnddrawable);
    
    displayRelativeLayout = (RelativeLayout) this.findViewById(R.id.displayRelativeLayout);
    displayRelativeLayout.setVisibility(View.GONE);
    positionToDisplayText = (TextView) this.findViewById(R.id.textToDisplay);
    
    mapView.setBuiltInZoomControls(true);
    mapView.setSatellite(true);
    mapController = mapView.getController();

    getLocationUpdatePreferences();
    getLocationDataAvailabilityPreferences();
    getRnDPositionPreferences();
    
    clearMapImage = (ImageView)this.findViewById(R.id.clearMapimageView);
    clearMapImage.setOnClickListener(new View.OnClickListener() {
      
      @Override
      public void onClick(View v) {
        clearMap();        
      }
    });
   
  }

  /**
   * getRnDPositionPreferences function
   * 
   * gets the user defined options for the R&D position types
   **/
	private void getRnDPositionPreferences() {
    SharedPreferences rndSharedPref = getSharedPreferences(RnDPositionType.KEY_RnD_SHARED_PREF,
        1);   
    int[] rndPositionTypes = new int[8];
    // Increased Satellite constellation
    boolean r0 = rndSharedPref.getBoolean(RnDPositionType.KEY_RnDTYPE_ONE, false)== true ;
    rndPositionTypes[0] = r0 == true ?1:0;
    // Best satellite constellation
    boolean r1 = rndSharedPref.getBoolean(RnDPositionType.KEY_RnDTYPE_TWO, false) == true;
    rndPositionTypes[1] = r1 == true? 1:0;
    // 2D positioning
    boolean r2 = rndSharedPref.getBoolean(RnDPositionType.KEY_RnDTYPE_THREE, false) == true;
    rndPositionTypes[2] = r2 == true? 1:0;
    // Positioning with RAIM
    boolean r3 = rndSharedPref.getBoolean(RnDPositionType.KEY_RnDTYPE_FOUR, false) == true;
    rndPositionTypes[3] = r3 == true?1:0;
    // Fast correction with no RRC
    boolean r4 = rndSharedPref.getBoolean(RnDPositionType.KEY_RnDTYPE_FIVE, false) == true;
    rndPositionTypes[4] = r4  == true? 1:0;
    // Best weight matrix
    boolean r5 =  rndSharedPref.getBoolean(RnDPositionType.KEY_RnDTYPE_SIX, false) == true;
    rndPositionTypes[5] = r5 == true? 1:0;
    // INS enhanced position
    //boolean r6 =rndSharedPref.getBoolean(RnDPositionType.KEY_RnDTYPE_SEVEN, false) == true;
    rndPositionTypes[6] = 0;//r6 == true? 1:0;
    // SBAS Ranging function (MT9 and MT17)
    rndPositionTypes[7] = rndSharedPref.getInt(RnDPositionType.KEY_RnDTYPE_EIGHT, 0);    

    GlobalState.setRndPositionType(rndPositionTypes);
  }

  /**
	 * createLogFile function
	 * 
	 * Creates 3 log files on the SD Card of the Android device.
	 * a)internallogfile+"current date".log: contains all log statements of Ephemeris data,
	 * Raw data, Sfrb data,GPS and EGNOS positions.
	 * b)error +"current date".log: contains all error statements that occure while executing 
	 * the application.
	 * c)position+ "current date".log: contains positions in the following order: GPS Latitude,GPS 
	 * Longitude, GPS Altitude, EGNOS Latitude, EGNOS Longitude, EGNOS Altitude, Receiver Latitude,
	 * Receiver Longitude, Receiver Altitude, HPL.
	 */
  private void createLogFile(){
	    Date dt = new Date();
	    SimpleDateFormat sdf;
	    sdf = new SimpleDateFormat("dd_MM_yyyy@HH_MM_SS");
	    String curTime = sdf.format(dt);
	    File directory = new File(root,"/EGNOSDemoApp/");
	    
	    if(!directory.exists())
	      directory.mkdir();
 
	    errorLogfile = new File(directory, "error" + curTime + ".log");
	    internalLogFile = new File(directory, "internalLogFile" + curTime + ".log");
	    positionLogFile = new File(directory, "position" + curTime + ".log");

	    try {	      
	      
        errorLogFileWriter = new FileWriter(errorLogfile);
        GlobalState.setErrorBufferedWriter(new BufferedWriter(errorLogFileWriter));

        //uncomment this section to log into internal log file
//	      internalLogFilewriter = new FileWriter(internalLogFile);
//        GlobalState.setInternalBufferedWriter(new BufferedWriter(internalLogFilewriter));

        positionLogFilewriter = new FileWriter(positionLogFile);
        GlobalState.setPositionBufferedWriter(new BufferedWriter(positionLogFilewriter));
  
        isLogFile = 1;
	    } catch (IOException e) {
	      e.printStackTrace();
	      Toast.makeText(this, "Unable to create log file", Toast.LENGTH_SHORT)
	          .show();
	      isLogFile = 0;
	    }
	    GlobalState.setisLogFile(isLogFile);
  }
 
  /**
   * checkNetwork function 
   * 
   * This function checks if the mobile device is connected to a network 
   * via 3G or Wifi.
   * @return 1 if mobile device is connected to a network, otherwise 0.
   **/
  public final int checkNetwork() {
    int network;
    ConnectivityManager connect = (ConnectivityManager) getSystemService
                                  (Context.CONNECTIVITY_SERVICE);
    NetworkInfo wifi = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    NetworkInfo mobile = connect
        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    if (!wifi.isConnectedOrConnecting() && !mobile.isConnectedOrConnecting()) {
      network = 0;
    } else {
      network = 1;
    }
    GlobalState.setNetwork(network);
    return network;
  }

  /**
   * startNetwork function 
   * 
   * This function starts the Wireless & Network System Settings.
   **/
  public final void startNetwork() {
    startActivity(new Intent(android.provider.Settings.
        ACTION_WIRELESS_SETTINGS));
  }

  /**
   * applicationStateChange function 
   * 
   * Redraws on the map when application state is
   * changed between portrait mode and landscape mode.
   * 
   * @param savedInstanceState   The bundle of messages with flags and 
   *                             coordinates.
   **/
  private void applicationStateChange(final Bundle savedInstanceState) {
    isClear = savedInstanceState.getBoolean(IS_CLEAR);
    if (!isClear) {
      isCurrent = savedInstanceState.getBoolean(IS_CURRENT);
      isTrack = savedInstanceState.getBoolean(IS_TRACK);

      if (isRoute) {
        routePointsLat = savedInstanceState
            .getStringArrayList(ROUTE_POINTS_LATITUDE);
        routePointsLong = savedInstanceState
            .getStringArrayList(ROUTE_POINTS_LONGITUDE);
        drawRoutePath();
      } else {
        gpstrackPointsLat = savedInstanceState
            .getStringArrayList(GPS_TRACK_POINTS_LATITUDE);
        gpstrackPointsLong = savedInstanceState
            .getStringArrayList(GPS_TRACK_POINTS_LONGITUDE);
        egnostrackPointsLat = savedInstanceState
            .getStringArrayList(GPS_TRACK_POINTS_LATITUDE);
        egnostrackPointsLong = savedInstanceState
            .getStringArrayList(GPS_TRACK_POINTS_LONGITUDE);
        HPLValue = savedInstanceState.getDouble(HPL_VALUE);

        for (int i = 0; i <= gpstrackPointsLat.size() - 1; i++) {
          gpsPoint = new GeoPoint((int) (Double.parseDouble(gpstrackPointsLat
              .get(i)) * 1E6), (int) (Double.parseDouble(gpstrackPointsLong
              .get(i)) * 1E6));
          overlayItemGPS = new OverlayItem(gpsPoint, "GPS", "GPS");
          mapItemizedOverlay.addOverlay(overlayItemGPS);

          egnosPoint = new GeoPoint(
              (int) (Double.parseDouble(egnostrackPointsLat.get(i)) * 1E6),
              (int) (Double.parseDouble(egnostrackPointsLong.get(i)) * 1E6));
          overlayItemEGNOS = new OverlayItem(egnosPoint, "EGNOS", "EGNOS");
          egnosmapItemizedOverlay.addOverlay(overlayItemEGNOS);
          integrityOverlay = new IntegrityOverlay(egnosPoint, displayValue,
              HPLValue);
          listOverlay.add(integrityOverlay);
          listOverlay.add(mapItemizedOverlay);
          listOverlay.add(egnosmapItemizedOverlay);
        }
      }
    }
  }

  /**
   * onSaveInstanceState function 
   * 
   * Stores flags, latitudes and longitudes when
   * application state is changed between portrait mode and landscape mode.
   * @param savedInstanceState    The bundle with flags,latitudes and longitudes.
   **/
  @Override
  public final void onSaveInstanceState(final Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    savedInstanceState.putString(START_POINT, source);
    savedInstanceState.putString(END_POINT, destination);
    savedInstanceState.putBoolean(IS_TRACK, isTrack);
    savedInstanceState.putBoolean(IS_ROUTE, isRoute);
    savedInstanceState.putBoolean(IS_CURRENT, isCurrent);
    savedInstanceState.putBoolean(IS_CLEAR, isClear);
    savedInstanceState.putInt(ZOOM_LEVEL, zoomLevel);
    savedInstanceState.putStringArrayList(GPS_TRACK_POINTS_LATITUDE,
        gpstrackPointsLat);
    savedInstanceState.putStringArrayList(GPS_TRACK_POINTS_LONGITUDE,
        gpstrackPointsLong);
    savedInstanceState.putStringArrayList(EGNOS_TRACK_POINTS_LATITUDE,
        egnostrackPointsLat);
    savedInstanceState.putStringArrayList(EGNOS_TRACK_POINTS_LONGITUDE,
        egnostrackPointsLong);
    savedInstanceState
        .putStringArrayList(ROUTE_POINTS_LATITUDE, routePointsLat);
    savedInstanceState.putStringArrayList(ROUTE_POINTS_LONGITUDE,
        routePointsLong);
    savedInstanceState.putDouble(HPL_VALUE, HPL);
  }

  /**
   * onPrepareOptionsMenu function 
   * 
   * Creates a menu from /res/menu/optionsmenu.xml.
   * @param menu         The menu list to create.
   * @return TRUE if click was handled,else FALSE.
   */
  @Override
  public final boolean onPrepareOptionsMenu(final Menu menu) {
    menu.clear();
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.optionsmenu, menu);
    menu.setGroupVisible(R.id.subGroup, false);   
    if (isTrack) {
      menu.setGroupVisible(R.id.mainGroup, false);
      menu.setGroupVisible(R.id.subGroup, true);
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
    case R.id.skyplot:
      Log.d(TAG, "EDAM | Selected Skyplot.");
      displaySkyplotView();
      return true;
    case R.id.currentLocation:
      Log.d(TAG, "EDAM | Selected Current Location.");
      isCurrent = false;
      RECEIVER_TYPE = GlobalState.getReceiverType();
      getCurrentLocation();
      return true;
//    case R.id.getDirections:
//      //Google stopped providing Maps kml files.
//      Log.d(TAG, "EDAM | Selected Get Directions.");
//      routePointsLat.clear();
//      routePointsLong.clear();
//      getDirections();
//      return true;
    case R.id.startTracking:
      Log.d(TAG, "EDAM | Selected Start Tracking.");
      gpstrackPointsLat.clear();
      gpstrackPointsLong.clear();
      egnostrackPointsLat.clear();
      egnostrackPointsLong.clear();
      isTrack = false;
      RECEIVER_TYPE = GlobalState.getReceiverType();
      startTracking(); 
      return true;
    case R.id.stopTracking:
      Log.d(TAG, "EDAM | Selected Stop Tracking.");
      stopTracking();
      return true;  
    case R.id.rnd:
      Log.d(TAG, "EDAM | Selected R&D.");
      RnDPositionType rnd = new RnDPositionType(this);
      rnd.rndPositionType();
      return true;
    case R.id.egnoscorrections:
        Log.d(TAG, "EDAM | EGNOS corrections output feature.");
        displayEGNOSCorrection();
        return true;
    case R.id.settings:
      Log.d(TAG, "EDAM | Selected Settings.");
      displaySettings();
      return true;
    case R.id.exit:
      Log.d(TAG, "EDAM | Selected Exit.");
      exitApplication();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }
  
  /**
   * displaySkyplotView function 
   * 
   **/
  private void displaySkyplotView() {    
	  Intent skyplotIntent = new Intent(this,SkyplotStatus.class);
	    if(GlobalState.getNORADData() == null) {
	      if(checkNetwork()!= 0) {         
	         startActivity(skyplotIntent);        
	      }else 
	        Toast.makeText(this, "Please connect to a network", Toast.LENGTH_SHORT).show();
	    }else 
	      startActivity(skyplotIntent);   
	    
  }

  /**
   * displayEGNOSCorrection 2.3	EGNOS corrections output feature 
   * 
   **/
  private void displayEGNOSCorrection() {    
  
    Intent egnosCorrectionOutputIntent = new Intent(this,EGNOSCorrectionInputOutput.class);
    startActivity(egnosCorrectionOutputIntent);        
 
  }

  /**
   * getCurrentLocation function 
   * 
   * Called when current location is clicked. This function creates a 
   * handler to be executed in regular intervals based on minTime.
   **/
  private void getCurrentLocation() {
    getLocationUpdatePreferences();
    getLocationDataAvailabilityPreferences();
    
    textToDisplay = null;
    displayRelativeLayout.setVisibility(View.GONE);
    
    if (GlobalState.getSocket() != null) {
      isCurrent = true;
      startLocationUpdate();
    }else {
      Toast.makeText(getBaseContext(), "Please connect to a Receiver", Toast.LENGTH_LONG).show();
      isCurrent = false;
    }
    GlobalState.setisCurrent(isCurrent);
  }
  
  private int startLocationUpdate() {
    switch (RECEIVER_TYPE) {
    case 1:
      Log.d(TAG, "EDAM | currentLocation, Handling uBlox receiver.");
//      for (int i = 0; i < GlobalState.getPosition().length; i++) {
//        GlobalState.getPosition()[i] = 0.0;
//      }
//      GlobalState.setPosition(GlobalState.getPosition());
      onStart = true;
      progressBarThread = new ProgressBarThread(progressBarhandler);
      if(coordinates[0] == 0.0)
        progressBarThread.setState(ProgressBarThread.RUNNING);
      else 
        progressBarThread.setState(ProgressBarThread.DONE);
      locationHandler = new Handler();
      locationHandler.removeCallbacks(currentLocation);
      locationHandler.postDelayed(currentLocation, 0);
      return 1;
    default:
      Toast.makeText(this, "Please connect to a receiver",
          Toast.LENGTH_SHORT).show();
      Log.d(TAG, "EDAM | currentLocation, No receiver found.");
      isCurrent = false;
      GlobalState.setisCurrent(isCurrent);
      isTrack = false;
      GlobalState.setisTracking(isTrack);
      return -1;
    }
  }
  

  /**
   * currentLocation runnable 
   * 
   * Gets coordinates from Bluetooth receiver and SW-Receiver 
   * and adds to map, and also calls the locationHandler to
   * run on regular intervals based on Minimum Time and Minimum Distance
   * from Settings.
   **/
  private Runnable currentLocation = new Runnable() {
    @Override
    public void run() {
      getLocationUpdatePreferences();

     // if (GlobalState.getSocket() != null) {

        if (isCurrent || isTrack) {
          try {
            if (GlobalState.getErrorWhileReadingBT() == -1) {
              Toast
                  .makeText(
                      getBaseContext(),
                      "Error occurred while using external Bluetooth Receiver.Please connect again to receiver to continue using this feature",
                      Toast.LENGTH_LONG).show();
              Log.e(TAG,"Error occurred while using external Bluetooth " +
              		"Receiver.Please connect again to receiver to continue using this feature");
              locationHandler.removeCallbacks(currentLocation);
              textToDisplay = null;
              displayRelativeLayout.setVisibility(View.GONE);
              new GetCoordinates().cancel(true);
              BluetoothConnect bConnect = new BluetoothConnect(getBaseContext());
              bConnect.closeConnection();
            }
            new GetCoordinates().execute("");
            if (minTime != 0 && minDistance == 0)
              updateTime = minTime;
            else if (minTime == 0 && minDistance != 0)
              updateTime = 3000;
            else if (minTime == 0 && minDistance == 0)
              updateTime = 60000;
            else if (minTime != 0 && minDistance != 0) {
              updateTime = minTime;
            }
            if (onStart) {
              locationHandler.postDelayed(this, 0);
              onStart = false;
            } else
              locationHandler.postDelayed(this, updateTime);
          } catch (Exception e) {
            log.logError("currentLocation runnable - Error: " + e);
          }
        }
//      } else {
//        Toast.makeText(getBaseContext(), "Please connect to a receiver",
//            Toast.LENGTH_SHORT).show();
//        locationHandler.removeCallbacks(currentLocation);
//        isTrack = false;
//        isCurrent = false;
//        GlobalState.setisCurrent(isCurrent);
//        GlobalState.setisTracking(isTrack);
//      }
    }
  };
  
  /**
   * GetCoordinates class 
   * 
   * An async task which would perform some actions in the background 
   * without interrupting the UI.Parse the data and send data to SW
   * Receiver to get GPS and EGNOS coordinates.
   **/
  private class GetCoordinates extends AsyncTask<String, Void, double[]> {
    /**
     * doInBackground function 
     * 
     * Initiates background process to get a 7 X 1 table of coordinates.
     * Rows 0 to 6 as GPS Latitude, GPS Longitude, GPS Altitude, EGNOS Latitude,
     * EGNOS Longitude, EGNOS Altitude and HPL respectively.
     * @param  params           The parameters of this task.
     * @return coordinates      The 7 X 1 table of GPS and EGNOS coordinates.
     **/
    @Override
    protected double[] doInBackground(final String... params) {
      if (isCurrent || isTrack) {
        Log.d(TAG, "EDAM | CurrentLocation, Handling uBlox receiver.");
        checkNetwork();
        textToDisplay = null;
        coordinates = GlobalState.getPosition();
        Log.d(TAG, "EDAM | GPS Latitude: " + coordinates[0]
            + "\nEDAM | GPS Longitude: " + coordinates[1]
            + "\nEDAM | GPS Altitude: " + coordinates[2]
            + "\nEDAM | EGNOS Latitude: " + coordinates[3]
            + "\nEDAM | EGNOS Longitude: " + coordinates[4]
            + "\nEDAM | EGNOS Altitude: " + coordinates[5] 
            + "\nEDAM | HPL: " + coordinates[6]
            + "\nEDAM | R&D Latitude: " + coordinates[7]
            + "\nEDAM | R&D Longitude: " + coordinates[8] 
            + "\nEDAM | R&D Altitude: " + coordinates[9]);
      }
      return coordinates;
    }
    
    /**
     * onPostExecute function 
     * 
     * Function is called after the background process is completed.
     * Adds the current position to the map.
     * Updates the current position based on Minimum Time and 
     * Minimum Distance from Settings.
     * @param coordinates      The 7 X 1 table of GPS and EGNOS coordinates.
     **/
    @Override
    protected void onPostExecute(final double[] coordinates) {
      double dist = 0;
      if (coordinates != null) {
        if (coordinates[0] != 0.0 || coordinates[1] != 0.0) {
          if (isCurrent || isTrack) {
            if(updateTime == 3000 && oldLatitude != 0 && minTime == 0){
               dist = UtilsDemoApp.getDistance(oldLatitude,oldLongitude,coordinates);
               Log.e(TAG, "EDAM | Distance : "+dist);
             if(dist >= minDistance)
               addToMap(coordinates);
            }else 
               addToMap(coordinates);
            for (int i = 0; i < GlobalState.getPosition().length; i++) {
              GlobalState.getPosition()[i] = 0.0;
            }
            GlobalState.setPosition(GlobalState.getPosition());
          }
        } else if (uBlox.startThread == 0) 
              showDialog(0);         
        }      
    }

    /**
     * onCancelled function 
     * 
     * Function is called when the Async Task is cancelled.
     * Sets all flags to false, and removes any Itemized Overlays, if it exists.
     **/
    @Override
    protected void onCancelled() {
      if (isCurrent) {
        mapItemizedOverlay.removeOverlay(overlayItemGPS);
        egnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
        listOverlay.clear();
      }
      isCurrent = false;
      isTrack = false;
      GlobalState.setisCurrent(isCurrent);
      GlobalState.setisTracking(isTrack);
      if (isRoute) {
        drawRoutePath();
      }
      isRoute = false;
    }
  }

  /**
   * onCreateDialog Dialog.
   *  
   * Creates a progress bar dialog based on the id.
   * Called when showDialog is called. 
   * @param id       The id of the progress bar to be created.
   * @return Dialog  The progress bar dialog created.
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
    case 0:// Spinner
      progressBarDialog = new ProgressDialog(this);
      progressBarDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      if (isCurrent || isTrack)
        progressBarDialog.setMessage("Acquiring GPS data...");
      else
        progressBarDialog.setMessage("Getting Route...");
      progressBarDialog.setOnCancelListener(new OnCancelListener() {
        public void onCancel(DialogInterface arg0) {
          if (progressBarDialog.isShowing())
            progressBarDialog.dismiss();
        }
      });
      progressBarThread.start();
      oldTime = System.currentTimeMillis();
      return progressBarDialog;
    default:
      return null;
    }
  }

  /**
   * progressBarhandler Handler.
   *
   * Handler to take action on different messages.
   * Display or remove progress bar dialog when the current GPS and  EGNOS
   * positions are obtained from the Background process.
   */
  final Handler progressBarhandler = new Handler() {
    long currentTime;
    /**
     * handleMessage Handler.
     *
     * Subclass to receive messages from a non UI thread.
     * @param message    The message received.
     */
    public void handleMessage(Message message) {
      coordinates = GlobalState.getPosition();
      if (isCurrent || isTrack) {
        if (coordinates[0] != 0) {
          if (coordinates[3] != 0) {
            removeDialog(0);
            progressBarThread.setState(ProgressBarThread.DONE);
          } else {
            if(EGNOS == 1){
              if(GlobalState.getSocket() != null){
                progressBarDialog.setMessage("Acquiring EGNOS data...");
                progressBarDialog.setOnCancelListener(new OnCancelListener() {
                  public void onCancel(DialogInterface arg0) {
                    if (progressBarDialog.isShowing()){
                     removeDialog(0);
                     progressBarThread.setState(ProgressBarThread.DONE);
                    }
                }
              });
             }else {
               removeDialog(0);
               progressBarThread.setState(ProgressBarThread.DONE);
               if(GlobalState.getSocket() != null)
                Toast.makeText(getBaseContext(), "Receiver Disconnected",
                   Toast.LENGTH_SHORT).show();
               isTrack = false;
               isCurrent = false;
               GlobalState.setisCurrent(isCurrent);
               GlobalState.setisTracking(isTrack);
              }
             }else if(SISNET == 1 && GlobalState.getNetwork() == 1 && uBlox.sisnetSocket != null){
                 if(GlobalState.getSocket() != null){
                     progressBarDialog.setMessage("Acquiring EGNOS data...");
                     progressBarDialog.setOnCancelListener(new OnCancelListener() {
                       public void onCancel(DialogInterface arg0) {
                         if (progressBarDialog.isShowing()){
                            removeDialog(0);
                         progressBarThread.setState(ProgressBarThread.DONE);
                         }
                     }
                   });
                  }else {
                    removeDialog(0);
                    progressBarThread.setState(ProgressBarThread.DONE);
                    if(GlobalState.getSocket() != null)
                      Toast.makeText(getBaseContext(), "Receiver Disconnected",
                        Toast.LENGTH_SHORT).show();
                    isTrack = false;
                    isCurrent = false;
                    GlobalState.setisCurrent(isCurrent);
                    GlobalState.setisTracking(isTrack);
                   }            	 
             }else{
            	 removeDialog(0);
                 progressBarThread.setState(ProgressBarThread.DONE);            	 
             }
          }
        } else {
          if (GlobalState.getSocket() != null) {
            currentTime = System.currentTimeMillis();
            if (currentTime == oldTime + 60000) { // wait for 30 seconds for GPS
                                                  // data
              removeDialog(0);
              progressBarThread.setState(ProgressBarThread.DONE);
              Toast.makeText(getBaseContext(),
                  "Location currently unavailable.", Toast.LENGTH_SHORT).show();
              isTrack = false;
              isCurrent = false;
              GlobalState.setisCurrent(isCurrent);
              GlobalState.setisTracking(isTrack);
            }
          } else {
            removeDialog(0);
            progressBarThread.setState(ProgressBarThread.DONE);
            if(GlobalState.getSocket() != null)
              Toast.makeText(getBaseContext(), "Receiver Disconnected",
                Toast.LENGTH_SHORT).show();
            isTrack = false;
            isCurrent = false;
            GlobalState.setisCurrent(isCurrent);
            GlobalState.setisTracking(isTrack);
          }
        }
      } else if (isRoute) {
        if (coordinates[0] != 0) {
          directionsDisplay.dismiss();
          showDirections(source, destination);
          removeDialog(0);
          progressBarThread.setState(ProgressBarThread.DONE);
        } else {
          currentTime = System.currentTimeMillis();
          if (currentTime == oldTime + 50000) { // wait for 60 seconds for GPS
                                                // data
            directionsDisplay.dismiss();
            removeDialog(0);
            progressBarThread.setState(ProgressBarThread.DONE);
            Toast.makeText(getBaseContext(), "Location currently unavailable.",
                Toast.LENGTH_SHORT).show();
            isRoute = false;
          }
        }
      }
    }
  };
  
  /**
   * ProgressBarThread Thread. 
   * 
   * Thread to to get a 7 X 1 table of coordinates.
   * Rows 0 to 6 as GPS Latitude, GPS Longitude, GPS Altitude, EGNOS Latitude,
   * EGNOS Longitude, EGNOS Altitude and HPL respectively.
  */
  private class ProgressBarThread extends Thread {
    final static int DONE = 0;
    final static int RUNNING = 1;
    int mState;
    Handler mHandler;

    ProgressBarThread(Handler h) {
      mHandler = h;
    }

    @Override
    public void run() {
      mState = RUNNING;
      while (mState == RUNNING) {
        coordinates = GlobalState.getPosition();
        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putDoubleArray("Coordinates", coordinates);
        msg.setData(b);
        mHandler.sendMessage(msg);
      }
    }

    // Set current state of thread
    public void setState(int state) {
      mState = state;
    }
  }

  /**
   * addToMap function 
   * 
   * Adds current GPS and EGNOS location markers on map.
   * Draws a route path from start point to end point
   * @param coordinates   The 7 X 1 table of GPS and EGNOS coordinates.
   **/
  private void addToMap(final double[] coordinates) {
    getLocationDataAvailabilityPreferences();
    integrityListOverlay.clear();
    if (isCurrent == true) {
      mapItemizedOverlay.removeOverlay(overlayItemGPS);
      rndmapItemizedOverlay.removeOverlay(overlayItemRnD);
      egnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
      gpsegnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
      listOverlay.clear();
    }
    if (isRoute == true) {
      drawRoutePath();
    }
    if (isCurrent || isTrack) {
      gpsLatitude = coordinates[0];
      gpsLongitude = coordinates[1];

      gpsPoint = new GeoPoint((int) (gpsLatitude * 1E6),
          (int) (gpsLongitude * 1E6));
      overlayItemGPS = new OverlayItem(gpsPoint, "GPS", "GPS");
      if(gpsLatitude != oldLatitude && gpsLongitude != oldLongitude)
        mapItemizedOverlay.addOverlay(overlayItemGPS);
      
      oldLatitude = gpsLatitude;
      oldLongitude = gpsLongitude;     
      
      textToDisplay = null;
      displayRelativeLayout.setVisibility(View.GONE);

      if (coordinates[3] != 0.0 || coordinates[4] != 0.0) {
        if (EGNOS == 1 || SISNET == 1 || GlobalState.getEDAS() == 1) {
          egnosLatitude = coordinates[3];
          egnosLongitude = coordinates[4];

          egnosPoint = new GeoPoint((int) (egnosLatitude * 1E6),
              (int) (egnosLongitude * 1E6));
          overlayItemEGNOS = new OverlayItem(egnosPoint, "EGNOS", "EGNOS");

          setPositionInfo();

          if (GlobalState.getisEgnosPosition() == 1)
            // displays EGNOS green position marker.
            // indicating an EGNOS position.
            egnosmapItemizedOverlay.addOverlay(overlayItemEGNOS);
          else
            // displays EGNOS orange position marker.
            // indicating a preliminary EGNOS position.
            gpsegnosmapItemizedOverlay.addOverlay(overlayItemEGNOS);
          Log.i(TAG,
              "EDAM | egnos_position:" + GlobalState.getisEgnosPosition());
          getIntegrityCirclePreferences();
          HPL = coordinates[6];

          integrityOverlay = new IntegrityOverlay(egnosPoint, displayValue, HPL);
          integrityListOverlay.add(integrityOverlay);
        }
      } else if (EGNOS == 1 && uBlox.sisnet == 0 && uBlox.edasSocket == null && uBlox.sisnetSocket == null) { // Signal in Space is
                                                    // available and EGNOS
                                                    // Signal in Space is ON in
                                                    // Settings.
        Toast.makeText(getBaseContext(),
            "Please wait obtaining EGNOS" + " position", Toast.LENGTH_SHORT)
            .show();
      } else if (SISNET == 1 && GlobalState.getNetwork() == 1
          && uBlox.sisnetSocket != null&& uBlox.edasSocket == null) {// SISNeT is ON in Settings.
        Toast.makeText(getBaseContext(),
            "Please wait obtaining position " + "from ESA's SISNeT",
            Toast.LENGTH_SHORT).show();
      } else if (GlobalState.getEDAS() == 1 && GlobalState.getNetwork() == 1
          && uBlox.edasSocket != null && uBlox.sisnetSocket == null) {// SISNeT is ON in Settings.
        Toast.makeText(getBaseContext(),
            "Please wait obtaining position " + "from EC's EDAS",
            Toast.LENGTH_SHORT).show();
      }
      rndLatitude = coordinates[7];
      rndLongitude = coordinates[8];
      
      if (rndLatitude != 0.0 || rndLongitude != 0.0) {
      rndPoint = new GeoPoint((int) (rndLatitude * 1E6),
          (int) (rndLongitude * 1E6));
      overlayItemRnD = new OverlayItem(rndPoint, "R&D", "R&D");
      rndmapItemizedOverlay.addOverlay(overlayItemRnD);
      }

      mapItemizedOverlay.populateOverlay();
      listOverlay.add(mapItemizedOverlay);

      if(gpsegnosmapItemizedOverlay.size() != 0){
        listOverlay.add(gpsegnosmapItemizedOverlay);
        gpsegnosmapItemizedOverlay.populateOverlay();
      }
      
      if (egnosmapItemizedOverlay.size() != 0) {
        listOverlay.add(egnosmapItemizedOverlay);
        egnosmapItemizedOverlay.populateOverlay();
      }
      
      if (rndmapItemizedOverlay.size() != 0) {
        listOverlay.add(rndmapItemizedOverlay);
        rndmapItemizedOverlay.populateOverlay();
      }

      gpstrackPointsLat.add(String.valueOf(gpsLatitude));
      gpstrackPointsLong.add(String.valueOf(gpsLongitude));
      egnostrackPointsLat.add(String.valueOf(egnosLatitude));
      egnostrackPointsLong.add(String.valueOf(egnosLongitude));
      
      
      if(coordinates[3] != 0.0)
        mapController.animateTo(egnosPoint);
      else
        mapController.animateTo(gpsPoint);
      
      
      zoomLevel = mapView.getZoomLevel();
    }
  }

  /**
   * setPositionInfo function 
   * 
   * Sets the text to display on the map. This text displays if the EGNOS position
   * displayed on the map is from Signal in Space or SISNeT.
   **/
	private void setPositionInfo() {
		if (EGNOS == 1 && uBlox.sisnet == 0)
			textToDisplay = "Position from EGNOS Signal in Space";
		else if (SISNET == 1 && GlobalState.getNetwork() == 1
				&& uBlox.sisnetSocket != null)
			textToDisplay = "Position from ESA's SISNeT";
		else if(GlobalState.getEDAS() == 1 && GlobalState.getNetwork() == 1)
		  textToDisplay = "Position from EC's EDAS";
		else
			textToDisplay = null;

		if (textToDisplay != null) {
			displayRelativeLayout.setVisibility(View.VISIBLE);
			positionToDisplayText.setText(textToDisplay);
		}
	}

/**
   * getLocationUpdatePreferences function 
   * 
   * Gets minimum time, minimum distance from Shared Preferences.
   **/
  private void getLocationUpdatePreferences() {
    SharedPreferences settingPrefs = PreferenceManager
        .getDefaultSharedPreferences(getBaseContext());

    try {
      minTime = Long.valueOf(settingPrefs
          .getString(Settings.KEY_MIN_TIME, "3"));
      minTime = minTime * 1000;
    } catch (NumberFormatException e) {
      minTime = 3*1000;
      log.logError("Minimum Time - Number format exception ");
    }

    try {
      minDistance = Float.valueOf(settingPrefs.getString(
          Settings.KEY_MIN_DISTANCE, "50"));
    } catch (NumberFormatException e) {
      minDistance = 50;
      log.logError("Minimum Distance - Number format exception ");
    }
  }
  

  /**
   * getIntegrityCirclePreferences function 
   * 
   * Gets Integrity Circle values from Shared Preferences.
   **/
  private void getIntegrityCirclePreferences() {
    displayValue.clear();
    SharedPreferences displayPref = getSharedPreferences(
        Settings.KEY_SETTINGS_SHARED_PREF, 0);
    displayValue.add(String.valueOf(displayPref.getBoolean(
        Settings.KEY_FIRST_CIRCLE, false)));
    displayValue.add(String.valueOf(displayPref.getBoolean(
        Settings.KEY_SECOND_CIRCLE, false)));
    displayValue.add(String.valueOf(displayPref.getBoolean(
        Settings.KEY_THIRD_CIRCLE, false)));
  }

  /**
   * getLocationDataAvailabilityPreferences function
   * 
   * Gets status of EGNOS Signal in Space On/Off and SISNeT On/Off from Shared Preferences.
   **/
  private void getLocationDataAvailabilityPreferences() {
    SharedPreferences locationdataAvailabilityPref = getSharedPreferences(
        Settings.KEY_SETTINGS_SHARED_PREF, 0);
    boolean egnosSettings = locationdataAvailabilityPref.getBoolean(
        Settings.KEY_EGNOSSIS_CHECK, false);
    boolean sisnetSettings = locationdataAvailabilityPref.getBoolean(
        Settings.KEY_SISNET_CHECK, false);
    boolean edasSettings = locationdataAvailabilityPref.getBoolean(
        Settings.KEY_EDAS_CHECK, false);
    if (egnosSettings)
      EGNOS = 1;
    else
      EGNOS = 0;

    if (sisnetSettings)
      SISNET = 1;
    else
      SISNET = 0;
 
    GlobalState.setEgnos(EGNOS);
    GlobalState.setSISNeT(SISNET);
    GlobalState.setEDAS(edasSettings == true ? 1: 0);
    
    Log.d(TAG, "EDAM | EGNOS ON/OFF: " + EGNOS);
    Log.d(TAG, "EDAM | SISNeT ON/OFF: " + SISNET);

    if (!isExit) {
      initNotifications();
      openNotifications();
    }
  }

  /**
   * getDirections function 
   * 
   * Called when Get Directions is clicked. Displays an alert dialog with 
   * Start Point ad End point from /res/layout/getdirections.xml. 
   * On click of OK, directionDetails is called to get Start point and 
   * End point values. On click of Cancel, the alert dialog is closed.
   **/
  private void getDirections() {
    LayoutInflater factory = LayoutInflater.from(this);
    View getDirectionsView = factory.inflate(R.layout.getdirections, null);
    sourceEdit = (EditText) getDirectionsView.findViewById(R.id.source_edit);
    destinationEdit = (EditText) getDirectionsView
        .findViewById(R.id.destination_edit);
    Builder directionsDisplayBox = new AlertDialog.Builder(this);

    mapItemizedOverlay.removeOverlay(overlayItemGPS);
    rndmapItemizedOverlay.removeOverlay(overlayItemRnD);
    egnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
    gpsegnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
    listOverlay.clear();
    
    textToDisplay = null;
    displayRelativeLayout.setVisibility(View.GONE);

    directionsDisplayBox.setTitle(R.string.alertdialog_getDirections);
    directionsDisplayBox.setView(getDirectionsView);
    directionsDisplay = directionsDisplayBox.show();
    Button goButton = (Button) getDirectionsView.findViewById(R.id.goButton);
    goButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(final View v) {
        source = sourceEdit.getText().toString();
        destination = destinationEdit.getText().toString();
        isTrack = false;
        isCurrent = false;
        onStart = false;
        GlobalState.setisCurrent(isCurrent);
        GlobalState.setisTracking(isTrack);

        if (source.length() == 0) {
          Toast.makeText(getBaseContext(), "Please give a start point",
              Toast.LENGTH_SHORT).show();
          isRoute = false;
        } else if (destination.length() == 0) {
          Toast.makeText(getBaseContext(), "Please give an end point",
              Toast.LENGTH_SHORT).show();
          isRoute = false;
        } else if (checkNetwork()== 0) {
          Toast.makeText(getBaseContext(), "Please connect to a network",
              Toast.LENGTH_SHORT).show();
          isRoute = false;
          directionsDisplay.dismiss();
        } else if (source.equals(destination)) {
          Toast.makeText(getBaseContext(),
              "Please give" + " different start and end point",
              Toast.LENGTH_SHORT).show();
        } else {
          if (source.contains("Current Location")
              || destination.contains("Current Location")) {
            
            if (GlobalState.getSocket() == null) {
              Toast.makeText(getBaseContext(), "Please connect to a receiver",
                  Toast.LENGTH_LONG).show();
              directionsDisplay.dismiss();
              isRoute = false;
            } else {
              isRoute = true;
              coordinates = GlobalState.getPosition();
              Log.d(TAG, "EDAM | Get Directions | GPS Latitude: "
                  + coordinates[0]
                  + "\n EDAM | Get Directions | GPS Longitude: "
                  + coordinates[1]);
              if (coordinates[0] == 0.0)
                showDialog(0);
              else {
                showDirections(source, destination);
                directionsDisplay.dismiss();
              }
            }
          } else {
            isRoute = true;
            showDirections(source, destination);
            directionsDisplay.dismiss();
          }
        }
      }
    });
  }

  /**
   * showDirections function 
   * 
   * Gets coordinates of Start point and End point.
   * Calls DrawGPSPath function to draw route on map.
   * @param getSource          Start point.
   * @param getDestination     End point.
   **/
  protected final void showDirections(final String getSource,
      final String getDestination) {
    List<Address> sourceLatLong, destinationLatLong;
    GeoPoint srcGeoPoint = null, destGeoPoint;
    double sourceLat = 0, sourceLong = 0;
    double destinationLat = 0, destinationLong = 0;

 
    if (getSource.contains("Current Location")) {
      coordinates = GlobalState.getPosition();
      try {
        if(coordinates[3] == 0) {
          sourceLat = coordinates[0];
          sourceLong = coordinates[1];
        }else {
          sourceLat = coordinates[3];
          sourceLong = coordinates[4];
        }
      } catch (Exception e) {
        sourceLat = 0.0;
        sourceLong = 0.0;
        isRoute = false;
      }
    } else {
      try {
        // get longitude and latitude of start point
        sourceLatLong = getLatitudeLongitude(getSource);
        for (int i = 0; i < sourceLatLong.size(); ++i) {
          Address sourceAddress = sourceLatLong.get(i);
          sourceLat = sourceAddress.getLatitude();
          sourceLong = sourceAddress.getLongitude();
        }
      } catch (Exception e) {
        sourceLat = 0.0;
        sourceLong = 0.0;
        isRoute = false;
      }
    }

    if (getDestination.contains("Current Location") && isRoute) {
      coordinates = GlobalState.getPosition();
      try {
        if(coordinates[3] == 0) {
          destinationLat = coordinates[0];
          destinationLong = coordinates[1];
        }else {
          destinationLat = coordinates[3];
          destinationLong = coordinates[4];
        }
      } catch (Exception e) {
        destinationLat = 0.0;
        destinationLong = 0.0;
        isRoute = false;
      }
    } else {
      try {
        // get longitude and latitude of end point
        destinationLatLong = getLatitudeLongitude(getDestination);
        for (int j = 0; j < destinationLatLong.size(); ++j) {
          Address destinationAdress = destinationLatLong.get(j);
          destinationLat = destinationAdress.getLatitude();
          destinationLong = destinationAdress.getLongitude();
        }
      } catch (Exception e) {
        destinationLat = 0.0;
        destinationLong = 0.0;
        isRoute = false;
      }
    }
    if (sourceLat != 0.0 || sourceLong != 0.0 || destinationLat != 0.0
        || destinationLong != 0.0) {
      // create a GeoPoint
      srcGeoPoint = new GeoPoint((int) (sourceLat * 1E6),
          (int) (sourceLong * 1E6));
      destGeoPoint = new GeoPoint((int) (destinationLat * 1E6),
          (int) (destinationLong * 1E6));

      // draw the route from source to destination
      drawGpsPath(srcGeoPoint, destGeoPoint, Color.GREEN);

    } else {
      Toast.makeText(this, "No Route found", Toast.LENGTH_SHORT).show();
      isRoute = false;
    }
  }

  /**
   * getLatitudeLongitude function 
   * 
   * Gets coordinates of Start point or End point.
   * @param address         The address of start point or end point.
   * @return coordinates    The latitude and longitude of address of start 
   *                        point or end point.
   **/
  public final List<Address> getLatitudeLongitude(final String address) {
    List<Address> coordinates = null;
    coder = new Geocoder(this);
    try {
      coordinates = coder.getFromLocationName(address, 5);
    } catch (IOException e) {
      log.logError("Geocode - Unable to get coordinates from location name: "+e);
      Log.e(TAG, "EDAM | Unable to get coordinates from location name: "+e);
    }
    return coordinates;
  }

  /**
   * drawRoutePath function 
   * 
   * Draw route on map when application state is changed between portrait 
   * mode and landscape mode.
   **/
  private void drawRoutePath() {
    int color = Color.GREEN;
    GeoPoint startGP = null, destGPS;
    if (!routePointsLat.isEmpty() || !routePointsLong.isEmpty()) {
      startGP = new GeoPoint(
          (int) (Double.parseDouble(routePointsLat.get(0)) * 1E6),
          (int) (Double.parseDouble(routePointsLong.get(0)) * 1E6));
      listOverlay.add(new DirectionOverlay(startGP, startGP, 1));
      GeoPoint gp1;
      GeoPoint gp2 = startGP;
      for (int i = 1; i < routePointsLat.size(); i++) {
        gp1 = gp2;
        gp2 = new GeoPoint(
            (int) (Double.parseDouble(routePointsLat.get(i)) * 1E6),
            (int) (Double.parseDouble(routePointsLong.get(i)) * 1E6));
        listOverlay.add(new DirectionOverlay(gp1, gp2, 2, color));
      }
      destGPS = new GeoPoint(
          (int) (Double.parseDouble(routePointsLat.get
                (routePointsLat.size() - 1)) * 1E6),
          (int) (Double.parseDouble(routePointsLong.get
                (routePointsLong.size() - 1)) * 1E6));
      listOverlay.add(new DirectionOverlay(destGPS, destGPS, 3));
    }
  }

  /**
   * DrawGPSPath function 
   * 
   * Draw route on map from start point to end point.
   * @param srcGPS    The GEO-point of start point.
   * @param destGPS   The GEO-point of end point.
   * @param color     The color to draw route.
   **/
  private void drawGpsPath(final GeoPoint srcGPS, final GeoPoint destGPS,
      final int color) {
    Document doc = null;
    GeoPoint startGPS;
    try {
      // get the kml (XML) doc from google map web service
      doc = getGeoPoint(srcGPS, destGPS);
      if (doc.getElementsByTagName("GeometryCollection").getLength() > 0) {
        String path="";
        NodeList list;
        list = doc.getElementsByTagName("GeometryCollection").item(0).getChildNodes();
        int len = list.getLength();
        for(int p = 0; p < len ; p++) {
          path = path + list.item(p).getFirstChild().getTextContent();
        }
        String[] pairs = path.split(" ");
        String[] lngLat = pairs[0].split(",");
        routePointsLat.add(lngLat[1]);
        routePointsLong.add(lngLat[0]);
        startGPS = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6),
            (int) (Double.parseDouble(lngLat[0]) * 1E6));
        listOverlay.add(new DirectionOverlay(startGPS, startGPS, 1));
        GeoPoint gp1;
        GeoPoint gp2 = startGPS;
        for (int i = 0; i < pairs.length; i++) {
          lngLat = pairs[i].split(",");
          routePointsLat.add(lngLat[1]);
          routePointsLong.add(lngLat[0]);

          gp1 = gp2;
          gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6),
              (int) (Double.parseDouble(lngLat[0]) * 1E6));
          listOverlay.add(new DirectionOverlay(gp1, gp2, 2, color));
        }
        listOverlay.add(new DirectionOverlay(destGPS, destGPS, 3));

        int moveToLat = (startGPS.getLatitudeE6() + 
                        (destGPS.getLatitudeE6() - 
                         startGPS.getLatitudeE6()) / 2);
        int moveToLong = (startGPS.getLongitudeE6() + 
                         (destGPS.getLongitudeE6() - startGPS
                         .getLongitudeE6()) / 2);
        GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);

        mapController.animateTo(moveTo);
        zoomLevel = mapController.setZoom(9);
        mapView.invalidate();
      }
    } catch (Exception e) {
      Toast.makeText(getBaseContext(), "No route found", Toast.LENGTH_SHORT)
          .show();
      log.logError("Get Directions - No route found ");
      Log.e(TAG, "EDAM | No route found");
      isTrack = false;
      isRoute = false;
    }
  }

  /**
   * getGeoPoint function 
   * 
   * Get a document as xml or kml with list of coordinates
   * from start point to end point. Get document from Google map web service.
   * @param src         The GEO-point of start point.
   * @param dest        The GEO-point of end point.
   * @return doc        The document from Google map web service.
   */
  private Document getGeoPoint(final GeoPoint src, final GeoPoint dest) {
    // connect to Google map web service
    StringBuilder urlString = new StringBuilder();
    urlString.append("http://maps.google.com/maps?f=d&hl=en");
    urlString.append("&saddr="); // from
    urlString.append(Double.toString(src.getLatitudeE6() / 1.0E6));
    urlString.append(",");
    urlString.append(Double.toString(src.getLongitudeE6() / 1.0E6));
    urlString.append("&daddr="); // to
    urlString.append(Double.toString(dest.getLatitudeE6() / 1.0E6));
    urlString.append(",");
    urlString.append(Double.toString(dest.getLongitudeE6() / 1.0E6));
    urlString.append("&ie=UTF8&0&om=0&output=kml");
    // get the kml (XML) doc. And parse it to get the
    // coordinates(direction route).
    Document doc = null;
    HttpURLConnection urlConnection = null;
    URL url = null;
    try {
      url = new URL(urlString.toString());
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.setDoOutput(true);
      urlConnection.setDoInput(true);
      urlConnection.connect();
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.parse(urlConnection.getInputStream());
    } catch (MalformedURLException e) {
    	log.logError("Get Directions - Unable to create URL from the given string: "+urlString.toString());
        Log.e(TAG, "EDAM | Unable to create URL from the given string: "+urlString.toString());
    } catch (IOException e) {
    	log.logError("Get Directions - IO error occurred: "+e);
        Log.e(TAG, "EDAM |IO error occurred: "+e);
    } catch (ParserConfigurationException e) {
    	log.logError("Get Directions - Parser cannot be created: "+e);
        Log.e(TAG, "EDAM | Parser cannot be created: "+e);
    } catch (SAXException e) {
    	log.logError("Get Directions - SAXEception occurred: "+e);
        Log.e(TAG, "EDAM | SAXEception occurred: "+e);
    }
    return doc;
  }

  /**
   * startTracking function 
   * 
   * Called when Start Tracking is clicked and creates a
   * handler to run on regular intervals based on minTime.
   **/
  private void startTracking() {
    getLocationUpdatePreferences();
    getLocationDataAvailabilityPreferences();

    isCurrent = false;
    textToDisplay = null;
    displayRelativeLayout.setVisibility(View.GONE);
    
     clearMapImage.setVisibility(View.GONE);    
    
     GlobalState.setisCurrent(isCurrent);
     mapItemizedOverlay.removeOverlay(overlayItemGPS);
     rndmapItemizedOverlay.removeOverlay(overlayItemRnD);
     egnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
     gpsegnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
     listOverlay.clear();
     
      if (GlobalState.getSocket() != null) {
        isTrack = true;
        startLocationUpdate();
      }else {
        isTrack = false;
        Toast.makeText(getBaseContext(), "Please connect to a Receiver", Toast.LENGTH_LONG).show();
      }
      GlobalState.setisTracking(isTrack);
  }

  /**
   * stopTracking function 
   * 
   * Called when Stop Tracking is clicked and creates a
   * handler to run on regular intervals based on minTime.
   **/
  private void stopTracking() {
    if (isTrack) {
      clearMapImage.setVisibility(View.VISIBLE);
      locationHandler.removeCallbacks(currentLocation);
      new GetCoordinates().cancel(true);
      oldLatitude = 0;
      oldLongitude = 0;
    }
  }

  /**
   * clearMap function 
   * 
   * Called when Clear Map is clicked. Deletes any handler, clears flags, 
   * clears overlays.
   **/
  private void clearMap() {
    isClear = true;
    mapItemizedOverlay.removeOverlay(overlayItemGPS);
    rndmapItemizedOverlay.removeOverlay(overlayItemRnD);
    egnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
    gpsegnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
    listOverlay.clear();
    routePointsLat.clear();
    routePointsLong.clear();
    gpstrackPointsLat.clear();
    gpstrackPointsLong.clear();
    egnostrackPointsLat.clear();
    egnostrackPointsLong.clear();
    oldLatitude = 0;
    oldLongitude = 0;
    textToDisplay = null;
    displayRelativeLayout.setVisibility(View.GONE);
    try {
      if (isCurrent || isTrack) {
        locationHandler.removeCallbacks(currentLocation);
        new GetCoordinates().cancel(true);
      }
    } catch (NullPointerException e) {
      Log.e(TAG, "EDAM | clearMap() Error: " + e.getMessage());
      log.logError("Clear Map - Unable to stop timer");
    }
    mapView.invalidate();
  }

  /**
   * displaySettings function 
   * 
   * Called when Settings is clicked. Call
   * Settings.class activity.
   **/
  private void displaySettings() {
	try{
     Intent settingsIntent = new Intent(this, Settings.class);
     startActivity(settingsIntent);
	}catch(Exception e){
		log.logError("Settings - unable to load EGNOS SDK core");	
		Log.e(TAG, "EDAM | unable to load EGNOS SDK core " + e);
	}
  }

  /**
   * displaySettings function 
   * 
   * Called when Settings is clicked. Call
   * Settings.class activity.
   **/
  private void displaySettings_io() {
	try{
     Intent settingsIntent = new Intent(this, EGNOSIoSettingActivity.class);
     startActivity(settingsIntent);
	}catch(Exception e){
		log.logError("Settings - unable to load EGNOS SDK core");	
		Log.e(TAG, "EDAM | unable to load EGNOS SDK core " + e);
	}
  }

  /**
   * initNotifications function 
   * 
   * This functions initiates the required variables to show or close an
   * ongoing notification.
   **/
  public void initNotifications() {
    notificationManager = (NotificationManager) getSystemService
                          (NOTIFICATION_SERVICE);
    notifyIntent = new Intent(this, EgnosDemoAppMain.class);
    appIntent = PendingIntent.getActivity(this, 0, notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    egnosNotification = new Notification(R.drawable.ic_egnos_enabled_logo_small,
    		"EGNOS Signal in Space is ON", System.currentTimeMillis());
    egnosNotification.flags = Notification.FLAG_ONGOING_EVENT;
    egnosNotification.setLatestEventInfo(this, "EGNOS Demo App", "EGNOS Signal in Space is ON",
        appIntent);

    satellitenetNotification = new Notification(R.drawable.
        ic_egnos_enabled_logo_small,"ESA's SISNeT is ON", System.currentTimeMillis());
    satellitenetNotification.flags = Notification.FLAG_ONGOING_EVENT;
    satellitenetNotification.setLatestEventInfo(this, "EGNOS Demo App",
        "ESA's SISNeT is ON", appIntent);
    

//    edasNotification = new Notification(R.drawable.
//        ic_egnos_enabled_logo_small,"EC's EDAS is ON", System.currentTimeMillis());
//    edasNotification.flags = Notification.FLAG_ONGOING_EVENT;
//    edasNotification.setLatestEventInfo(this, "EGNOS Demo App",
//        "EC's EDAS is ON", appIntent);
  }

  /**
   * openNotifications function 
   * 
   * This functions initiates the required variables to show or close an
   * ongoing notification.
   * @param  egnosOnOff            The status of EGNOS On/Off.
   * @param  satelliteNeTOnOff     The status of SISNeT On/Off.
   **/ 
  public void openNotifications() {
    if (GlobalState.getEgnos() == 1)
      notificationManager.notify(0, egnosNotification);
    if (GlobalState.getSISNeT() == 1)
      notificationManager.notify(1, satellitenetNotification);
    if (GlobalState.getEDAS() == 1)
      notificationManager.notify(2, edasNotification);
  }

  /**
   * exitApplication function 
   * 
   * Called when Exit is clicked. Displays an alert
   * dialog with message to exit the application. 
   * On click of OK, clear and delete any handlers,close any bluetooth 
   * connections, close the .gpx file.Cancels any ongoing notifications.
   * On click of Cancel, close dialog box.
   **/
  private void exitApplication() {
    Log.d(TAG, "EDAM | exitApplication().");
    new AlertDialog.Builder(EgnosDemoAppMain.this)
      .setMessage("Exit EGNOS Demo App?")
      .setPositiveButton(R.string.exit,
          new DialogInterface.OnClickListener() {
        public void onClick(final DialogInterface dialog,
          final int whichButton) {
        isExit = true;
        GlobalState.setisExit(isExit);
        try {
          if (isCurrent || isTrack) {
             locationHandler.removeCallbacks(currentLocation);
             new GetCoordinates().cancel(true);
          }
        } catch (NullPointerException e) {
           Log.e(TAG,"EDAM | exitApplication() Error: " + e.getMessage());
           log.logError("Exit - Unable to stop timer");
        }

        if(uBlox.sisnetSocket !=null)
        	SISNeT.closeSisnet(uBlox.sisnetSocket);
        BluetoothConnect bConnect = new BluetoothConnect(
                    getBaseContext());
        bConnect.closeConnection();
        
        getLocationDataAvailabilityPreferences();
        cancelNotification();
        closeFileConnection();
        System.exit(0);
       }
      })
     .setNegativeButton(R.string.cancel,
        new DialogInterface.OnClickListener() {
      public void onClick(final DialogInterface dialog, final int which) {
       // closes Alert Dialog.
      }}).show();
  }

  /**
   * cancelNotification function 
   * 
   * Cancels any ongoing notifications, before application is closed.
   * @param  egnosOnOff            The status of EGNOS On/Off.
   * @param  satelliteNeTOnOff     The status of SISNeT On/Off.
   **/
  private void cancelNotification() {
    if (GlobalState.getEgnos() == 1) 
      notificationManager.cancel(0);
    
    if (GlobalState.getSISNeT() == 1) 
      notificationManager.cancel(1);
    
    if (GlobalState.getEDAS() == 1) 
      notificationManager.cancel(2);
    
  }

  /**
   * closeFileConnection function
   * 
   * Closes the opened log file.
   */ 
	public void closeFileConnection() {
	  if (GlobalState.getInternalBufferedWriter() != null){
	    try {
		     GlobalState.getInternalBufferedWriter().close();
		     GlobalState.setInternalBufferedWriter(null);
			} catch (Exception e) {
				Log.e(TAG, "EDAM |  Could not close internal log file buffered writer.");
		     }
		 }
			
		if (internalLogFilewriter != null) {
	     try {
	    	 internalLogFilewriter.close();
			 } catch (Exception e) {
		        Log.e(TAG, "EDAM |  Could not close internal log file writer.");
		     }
		}
			
		if (GlobalState.getErrorBufferedWriter()!= null) {
		 try {
			  GlobalState.getErrorBufferedWriter().close();
			  GlobalState.setErrorBufferedWriter(null);
			 } catch (Exception e) {
				 Log.e(TAG, "EDAM |  Could not close error log file buffered writer.");
		     }
	    }
		
		if (errorLogFileWriter != null) {
		  try {
			  errorLogFileWriter.close();
			  } catch (Exception e) {
				  Log.e(TAG, "EDAM |  Could not close error log file writer.");
			  }
		 }
		
		if (GlobalState.getPositionBufferedWriter() != null) {
		 try {
		 	  GlobalState.getPositionBufferedWriter().close();
		 	  GlobalState.setPositionBufferedWriter(null);
			 } catch (Exception e) {
				 Log.e(TAG, "EDAM |  Could not close position log file buffered writer.");
			 }
	    }
			
	   if (positionLogFilewriter != null) {
		  try {
			  positionLogFilewriter.close();
			  } catch (Exception e) {
				  Log.e(TAG, "EDAM |  Could not close position log  file writer.");
			  }
		}
	}


  /**
   * onDestroy function
   * 
   * Deletes any handler, clears flags, clears overlays.
   * Clear and delete any handlers,close any bluetooth 
   * connections, close the gpxfile+"current data".gpx file.
   * Cancels any ongoing notifications.
   **/
  @Override
  protected final void onDestroy() {
    super.onDestroy();
    mapItemizedOverlay.removeOverlay(overlayItemGPS);
    egnosmapItemizedOverlay.removeOverlay(overlayItemEGNOS);
    listOverlay.clear();
    routePointsLat.clear();
    routePointsLong.clear();
    gpstrackPointsLat.clear();
    gpstrackPointsLong.clear();
    egnostrackPointsLat.clear();
    egnostrackPointsLong.clear();
    mapView.invalidate();
    try {
      if (isCurrent || isTrack) {
        locationHandler.removeCallbacks(currentLocation);
        new GetCoordinates().cancel(true);
      }
    } catch (NullPointerException e) {
      Log.e(TAG, "EDAM | onDestroy() Error: " + e.getMessage());
      log.logError("onDestroy - Unable to stop timer");
    }
    if(uBlox.sisnetSocket !=null)
    	SISNeT.closeSisnet(uBlox.sisnetSocket);
    isCurrent = false;
    isRoute = false;
    isTrack = false;
    BluetoothConnect bConnect = new BluetoothConnect(getBaseContext());
    bConnect.closeConnection();
    isExit = true;
    getLocationDataAvailabilityPreferences();
    cancelNotification();
  }

  /**
   * isRouteDisplayed function 
   * 
   * The function indicating if route is displayed or not.
   * @return TRUE if route is shown, otherwise FALSE.
   **/
  @Override
  protected final boolean isRouteDisplayed() {
    return false;
  }


  /**
   * onKeyDown function 
   * 
   * Called when a key is held down.
   * Displays Exit dialog box when back key is pressed.
   * @param  keyCode         The keycode of the key pressed.
   * @param  event           The description of the key envent.
   * @return TRUE if key is pressed otherwise FALSE.
   **/
  @Override
  public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      exitApplication();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}

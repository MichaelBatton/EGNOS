/**
 * @file Settings.java
 *
 * Display Settings page with Bluetooth Settings,
 * Location Data Availability, Position Integrity Display 
 * and Location Update Request.Also store settings as SharedPreferences.
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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.ec.R;
import com.ec.egnossdk.BluetoothReceiverList;
import com.ec.egnossdk.GlobalState;
import com.ec.egnossdk.LogFiles;
import com.ec.egnossdk.uBlox;

/**
 * PreferenceActivity that displays Settings page with Bluetooth Settings,
 * Location Data Availability, Position Integrity Display 
 * and Location Update Request.Also store settings as SharedPreferences.
 * Implements OnSharedPreferenceChangeListener.
 **/
public class Settings extends PreferenceActivity
implements OnSharedPreferenceChangeListener {
  public static final String KEY_SETTINGS_SHARED_PREF
  = "settingsSharedPrefKey";
public static final String KEY_BLUETOOTH_ONOFF = "bluetoothOnOffKey";
public static final String KEY_CHOOSE_BLUETOOTH_RECEIVER
                                     = "chooseBluetoothReceiverKey";


public static final String KEY_EGNOSSIS_CHECK = "egnosSISOnOffKey";
public static final String KEY_SISNET_CHECK = "sisnetOnOffKey";
public static final String KEY_EDAS_CHECK = "edasOnOffKey";

public static final String KEY_INTEGRITY_VALUE = "integrityValueKey";
public static final String KEY_POSITION_INTEGRITY_DISPLAY
                                             = "integrityDisplayKey";
public static final String KEY_FIRST_CIRCLE = "firstCircleKey";
public static final String KEY_SECOND_CIRCLE = "secondCircleKey";
public static final String KEY_THIRD_CIRCLE = "thirdCircleKey";

public static final String KEY_MIN_TIME = "minTimeKey";
public static final String KEY_MIN_DISTANCE = "minDistanceKey";

public static final String KEY_ABOUT = "aboutKey";

private CheckBoxPreference bluetoothChecked;
private Preference chooseReceiver;

public CheckBoxPreference egnossisChecked;
public CheckBoxPreference sisnetChecked;
public CheckBoxPreference edasChecked;

private SharedPreferences settingsSharedPref;
private BluetoothAdapter adapter;
private SharedPreferences.Editor prefEditor;
public static final String TOAST = "toast";
private static final int CONNECT_RECEIVER = 1;
EditTextPreference minTimePreference;
EditText minTimeText;
EditTextPreference minDistPreference;
EditText minDistText;
LogFiles log;

  

  /**
   * Called when the activity is first created and loads UI for this class
   * from /res/layout/settings.xml.
   * @param   savedInstanceState    Bundle of any saved instances.
   **/
  @Override
  protected final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.settings);
   
    log = new LogFiles();

    egnossisChecked = (CheckBoxPreference) getPreferenceScreen()
           .findPreference(KEY_EGNOSSIS_CHECK);
    sisnetChecked = (CheckBoxPreference) getPreferenceScreen()
           .findPreference(KEY_SISNET_CHECK);
    edasChecked = (CheckBoxPreference) getPreferenceScreen()
           .findPreference(KEY_EDAS_CHECK);

    settingsSharedPref = getSharedPreferences(KEY_SETTINGS_SHARED_PREF,
        MODE_WORLD_READABLE);
    prefEditor = settingsSharedPref.edit();
    
    // Bluetooth Settings
    chooseReceiver = findPreference(KEY_CHOOSE_BLUETOOTH_RECEIVER);
    //set Bluetooth Settings
    bluetoothSettings();
    
    egnossisChecked.setChecked(GlobalState.getEgnos()!=0);
    egnossisChecked.setSummary(GlobalState.getEgnos()!=0 ? "" : 
     "Turn on EGNOS Signal in Space");
  
    sisnetChecked.setChecked(GlobalState.getSISNeT()!=0);
    sisnetChecked.setSummary(GlobalState.getSISNeT()!=0 ? "" : 
     "Turn on ESA's SISNeT");
    
    edasChecked.setChecked(GlobalState.getEDAS() != 0);
    edasChecked.setSummary(GlobalState.getEDAS() != 0 ? "":
     "Requires Login");
    
    edasChecked.setChecked(false);
    edasChecked.setEnabled(false);
   
    // listens when Choose Bluetooth Receiver preference is clicked
    chooseReceiver.setOnPreferenceClickListener(
        new OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(final Preference preference) {
            return chooseBluetoothReceiver();
          }
     });

    // Position Integrity Display
    // listens when Position Integrity Circle preference is clicked
    Preference positionIntegrityDisplay = findPreference(
        KEY_POSITION_INTEGRITY_DISPLAY);
    positionIntegrityDisplay.setOnPreferenceClickListener(
        new Preference.OnPreferenceClickListener() {
          @Override
         public boolean onPreferenceClick(final Preference preference) {
            return integrityDisplay();
          }
        });
    
    //Location Update Request
    //Minimum Time 
    minTimePreference = (EditTextPreference) findPreference
                                          (KEY_MIN_TIME);
    minTimeText = (EditText)minTimePreference.getEditText();
    minTimeText.setKeyListener(DigitsKeyListener.getInstance(true,false));
    minTimeText.cancelLongPress();
    minTimeText.setMaxLines(1);
    
    //Minimum Distance 
    minDistPreference = (EditTextPreference) findPreference
                      (KEY_MIN_DISTANCE);
    minDistText = (EditText)minDistPreference.getEditText();
    minDistText.setKeyListener(DigitsKeyListener.getInstance(true,false));
    minDistText.cancelLongPress();
    minDistText.setMaxLines(1);
    
    // About EGNOS Demo App
    // listens when About preference is clicked
    Preference aboutPref = findPreference(KEY_ABOUT);
    aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		
		@Override
		public boolean onPreferenceClick(Preference preference) {
		    Intent aboutIntent = new Intent(Settings.this, About.class);
		    startActivity(aboutIntent);

			return true;
		}
	});
  }

  protected boolean connectToUblox() {
    uBlox ub = new uBlox(getApplicationContext());
    ub.init();
    return true;
  }

  /**
   * checkNetwork function
   * 
   * This function checks if the mobile device is connected to a network via 
   * 3G or Wifi.
   * @return  1  if network is available,otherwise 0.
   **/
  public final int checkNetwork() {
	  int network = 0;
    ConnectivityManager connect =
      (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo wifi = connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    NetworkInfo mobile =
      connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    if (wifi.isConnected() || mobile.isConnected()) {
    	network = 1;
    } else {
      network = 0;
    }
    GlobalState.setNetwork(network);
    return network;
  }

  /**
   * bluetoothSettings function
   * 
   * Initialization page for Settings Activity enables, disables or adds
   * any values to settings page when first created.
   **/
  private void bluetoothSettings() {
    adapter = BluetoothAdapter.getDefaultAdapter();
    bluetoothChecked = (CheckBoxPreference) getPreferenceScreen()
                         .findPreference(KEY_BLUETOOTH_ONOFF);
    if (adapter != null) {
      if (adapter.getState() == BluetoothAdapter.STATE_ON) {
        bluetoothChecked.setChecked(true);
        chooseReceiver.setEnabled(true);
      } else if (adapter.getState() == BluetoothAdapter.STATE_OFF) {
        bluetoothChecked.setChecked(false);
        chooseReceiver.setEnabled(false);
      }
    } else {
      bluetoothChecked.setEnabled(false);
      chooseReceiver.setEnabled(false);
      Toast.makeText(this, "Device does not support Bluetooth",
          Toast.LENGTH_SHORT).show();
      log.logError("Choose Bluetooth Receiver - Bluetooth is not supported.");
    }
  }

  /**
   * chooseBluetoothReceiver function
   * 
   * Called when Choose Bluetooth Receiver preference is clicked starts
   * Activity to list any bluetooth devices i.e. BluetoothReceiverList
   * activity.
   * @return  true  if click was handled.
   **/
  protected final boolean chooseBluetoothReceiver() {
    Intent bluetoothReceiverIntent = new Intent(this,
        BluetoothReceiverList.class);
    startActivityForResult(bluetoothReceiverIntent, CONNECT_RECEIVER);
    return true;
  }

  /**
   * integrityDisplay function
   * 
   * Called when Postion Integrity Display preference is clicked;
   * Creates a view from /res/layout/integritydisplay.xml.
   * @return  true   if click was handled.
   **/
  private boolean integrityDisplay() {
    LayoutInflater factory = LayoutInflater.from(Settings.this);
    View integrityDisplayView = factory.inflate(R.layout.integritydisplay,
        null);
    final CheckBox firstCircle = (CheckBox) integrityDisplayView
    .findViewById(R.id.firstCircle_check);
    final CheckBox secondCircle = (CheckBox) integrityDisplayView
     .findViewById(R.id.secondCircle_check);
    final CheckBox thirdCircle = (CheckBox) integrityDisplayView
     .findViewById(R.id.thirdCircle_check);
    
    firstCircle.setChecked(settingsSharedPref
        .getBoolean(KEY_FIRST_CIRCLE, false));
    secondCircle.setChecked(settingsSharedPref
        .getBoolean(KEY_SECOND_CIRCLE, false));
    thirdCircle.setChecked(settingsSharedPref
        .getBoolean(KEY_THIRD_CIRCLE, false));
    
    final Builder displayDialog = new AlertDialog.Builder(Settings.this);
    displayDialog.setTitle(R.string.positionIntegrityDisplay)
    .setView(integrityDisplayView)
    .setPositiveButton(R.string.ok,
                 new DialogInterface.OnClickListener() {
       @Override
       public void onClick(final DialogInterface dialog,
                   final int whichButton) {

    	  prefEditor.putBoolean(KEY_FIRST_CIRCLE, firstCircle.isChecked());
          prefEditor.putBoolean(KEY_SECOND_CIRCLE, secondCircle.isChecked());
          prefEditor.putBoolean(KEY_THIRD_CIRCLE, thirdCircle.isChecked());
          prefEditor.commit();
       }
       })
       .setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
         @Override
         public void onClick(final DialogInterface dialog,
                   final int which) {
        	 //close the alert dialog.
          }
        })
       .show();
    return true;
  }

  /**
   * onPause function
   * 
   * Called when activity is in pause state unregisters any shared
   * preference listeners.
   **/
  @Override
  protected final void onPause() {
    super.onPause();
    getPreferenceScreen().getSharedPreferences()
    .unregisterOnSharedPreferenceChangeListener(this);
  }

  /**
   * onResume function
   * 
   * Called when activity resumes state and registers any shared
   * preference listeners.
   **/
  @Override
  protected final void onResume() {
    super.onResume();
    getPreferenceScreen().getSharedPreferences()
    .registerOnSharedPreferenceChangeListener(this);
    if (null == adapter || !adapter.isEnabled()) {
      bluetoothChecked.setChecked(false);
      chooseReceiver.setEnabled(false);
    }
  }

  /**
   * onSharedPreferenceChanged function
   * 
   * Called when any preference is changed.
   * @param    sharedPreferences   A shared preference.
   * @param    key                 Key of the preference clicked.
   **/
  //@Override
  public final void onSharedPreferenceChanged(
      final SharedPreferences sharedPreferences,
      final String key) {
    NotificationManager notificationManager = 
      (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    Intent notifyIntent = new Intent(this,EgnosDemoAppMain.class);
    PendingIntent appIntent = PendingIntent.getActivity(this, 0,
        notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

     if (key.equals(KEY_BLUETOOTH_ONOFF)) {
     bluetoothChecked.setSummary(sharedPreferences
         .getBoolean(key, false) ? "" : "Turn on Bluetooth");
     if (null != adapter) {
       if (bluetoothChecked.isChecked()) {
         chooseReceiver.setEnabled(true);
         adapter.enable();
       } else {
         chooseReceiver.setEnabled(false);
         adapter.disable();
       }
     }
   }else if (key.equals(KEY_EGNOSSIS_CHECK)) {
     egnossisChecked.setSummary(sharedPreferences
         .getBoolean(key, false) ? "" : "Turn on EGNOS Signal in Space");
     if (egnossisChecked.isChecked()) {
      prefEditor.putBoolean(KEY_EGNOSSIS_CHECK, true);
      Notification egnosNotification = new Notification(R.drawable
          .ic_egnos_enabled_logo_small,"EGNOS Signal in Space is ON",  
          System.currentTimeMillis());             
     egnosNotification.flags = Notification.FLAG_ONGOING_EVENT;
     egnosNotification.setLatestEventInfo(this, "EGNOS Demo App", 
         "EGNOS Signal in Space is ON", appIntent);  
     notificationManager.notify(0, egnosNotification); 
     GlobalState.setEgnos(1);  
     } else {
       prefEditor.putBoolean(KEY_EGNOSSIS_CHECK, false);
       notificationManager.cancel(0);
       GlobalState.setEgnos(0);
     }
   } else if (key.equals(KEY_SISNET_CHECK)) {
     sisnetChecked.setSummary(sharedPreferences
         .getBoolean(key, false) ? "" : "Turn on SISNeT");
     if (sisnetChecked.isChecked()) {
       prefEditor.putBoolean(KEY_SISNET_CHECK, true);
       Notification sisnetNotification = new Notification(
           R.drawable.ic_egnos_enabled_logo_small, "SISNeT is ON", 
           System.currentTimeMillis());
       sisnetNotification.flags = Notification.FLAG_ONGOING_EVENT;
       sisnetNotification.setLatestEventInfo(this, 
           "EGNOS Demo App", "ESA's SISNeT is ON", appIntent); 
       notificationManager.notify(1, sisnetNotification); 
       GlobalState.setSISNeT(1);
       if(checkNetwork() == 0)
         Toast.makeText(getBaseContext(), "Please connect to " +
             "the Internet, to use SISNeT.", Toast.LENGTH_SHORT).show();
     } else {
      prefEditor.putBoolean(KEY_SISNET_CHECK, false);
      notificationManager.cancel(1);
      GlobalState.setSISNeT(0);
     }
   } else if (key.equals(KEY_EDAS_CHECK)) {
//     edasChecked.setSummary(sharedPreferences
//         .getBoolean(key, false) ? "" : "Turn on EDAS");
//     if (edasChecked.isChecked()) {
//       prefEditor.putBoolean(KEY_EDAS_CHECK, true);
//       Notification edasNotification = new Notification(
//           R.drawable.ic_egnos_enabled_logo_small, "EDAS is ON", 
//           System.currentTimeMillis());
//       edasNotification.flags = Notification.FLAG_ONGOING_EVENT;
//       edasNotification.setLatestEventInfo(this, 
//           "EGNOS Demo App", "EC's EDAS is ON", appIntent); 
//       notificationManager.notify(2, edasNotification); 
//       GlobalState.setEDAS(1);
//       if(checkNetwork() == 0)
//         Toast.makeText(getBaseContext(), "Please connect to " +
//             "the Internet, to use EDAS.", Toast.LENGTH_SHORT).show();
//     } else {
//       prefEditor.putBoolean(KEY_EDAS_CHECK, false);
//       notificationManager.cancel(2);
//       GlobalState.setEDAS(0);
//     }     
   } else if (key.equals(KEY_MIN_TIME)) {
     int minTimeValue = Integer
         .valueOf(minTimeText.getText().toString());
     if (minTimeValue == 1 || minTimeValue == 2) {
       Toast.makeText(this,
           "Minimum Time should be 0 or above 3 seconds.",
           Toast.LENGTH_LONG).show();
       minTimePreference.setText("3");
     }
   }
   prefEditor.commit();
  }
}

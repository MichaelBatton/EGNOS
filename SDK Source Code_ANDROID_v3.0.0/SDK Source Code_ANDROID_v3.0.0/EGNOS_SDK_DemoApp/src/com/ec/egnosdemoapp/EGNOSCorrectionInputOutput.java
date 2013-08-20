/**
 * @file EGNOSCorrectionInputOutput.java
 *
 * Called when the In/-Output menu item is tapped.
 * Displays UI to make available EGNOS corrections and enhanced
 * position using NMEA and RTCM protocols through Bluetooth,
 * Wi-Fi and File logging.
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.ec.R;
import com.ec.egnossdk.BluetoothMessageTransferService;
import com.ec.egnossdk.BluetoothSenderList;
import com.ec.egnossdk.GlobalState;
import com.ec.egnossdk.NMEAMesaages;
import com.ec.egnossdk.NMEARTCMMessages;
import com.ec.wifidirect.WiFiDirectActivity;

public class EGNOSCorrectionInputOutput extends Activity implements
    OnItemSelectedListener, OnClickListener, OnCheckedChangeListener, OnItemClickListener {

  //Message to turn on NMEA message in a uBlox GPS receiver
  public static final String TURN_ON_NMEA = "B5620600140001000000D008000000E100000700020000000000DDC3";

  //ID's for different modes of sending NMEA messages
  public static final String NMEA_BT_ID = "NMEABT";
  public static final String NMEA_WIFI_ID = "NMEAWifi";
  public static final String NMEA_FL_ID = "NMEAFile";
  
  //Messages to be displayed in the Ongoing Event for different modes of sending NMEA messages
  private static final String NMEA_BT_SEND = "Sending NMEA messages via Bluetooth";
  private static final String NMEA_WIFI_SEND = "Sending NMEA messages via Wi-Fi";
  private static final String NMEA_FL_SAVE = "Saving NMEA messages to ";
  
  //ID's for different modes of receiving NMEA and sending RTCM messages
  public static final String NMEARTCM_BT_ID= "RTCMBT";
  public static final String NMEARTCM_WIFI_ID = "RTCMWifi";
  public static final String NMEARTCM_FL_ID = "RTCMFile";
  
  //Messages to be displayed in the Ongoing Event for different modes of receiving NMEA and sending RTCM messages
  private static final String NMEARTCM_BT_RECEIVE = "Receiving NMEA messages via Bluetooth";
 // private static final String NMEARTCM_WIFI_RECEIVE  = "Receiving NMEA messages via Wi-Fi";
 //  private static final String NMEARTCM_FL_RECEIVE  = "Getting RTCM messages from file ";
  private static final String NMEARTCM_BT_SEND = "Sending RTCM messages via Bluetooth";
  private static final String NMEARTCM_WIFI_SEND = "Sending RTCM messages via Wi-Fi";
  private static final String NMEARTCM_FL_SAVE = "Saving RTCM messages to ";

  // BluetoothServerSocket serverSocket;
  public static final int CONNECED_TO_SENDER = 1;
  private static final boolean D = true;
  public static final int REQUEST_ENABLE_BT = 10;
  private String TAG = "NMEA-RTCM-SETTING";

  // nmea Buttons
  Button sendNMEAViaBlueTooth;
  static Button saveNMEAToFileButton;
  Button sendNMEAViaWIFI;

  // nmea check box
  CheckBox checkBlueToothNMEA;
  CheckBox checkFileNMEA;
  CheckBox checkWIFINMEA;

  // rtcm buttons
  Button receiveRTCMViaBluetooth;
  Button sendRTCMViaBluetooth;

  Button receiveRTCMViaWIFI;
  Button sendRTCMViaWIFI;

  Button readRTCMFromFile;
  static Button saveRTCMToFile;

  // rtcm checkbox
  CheckBox checkBlueToothRTCM;
  CheckBox checkFileRTCM;
  CheckBox checkWIFIRTCM;
  
  //Ongoing Event List View
  ListView onGoingEventListView;
  static ArrayList<String> onGoingData = new ArrayList<String>();
  static ArrayList<String> onGoingDataID = new ArrayList<String>();
  static ArrayAdapter<String> onGoingEventAdapter;
  
  // Name of the connected device
  private String mConnectedDeviceName = null;

  private Spinner spinner;
  BluetoothMessageTransferService mt;
  private View row_nmea, row_rtcm;
  BluetoothSocket connectionSocket;
  static public BluetoothSocket clientConnectionSocket;
  byte[] buffer;

  // Local Bluetooth adapter
  private BluetoothAdapter mBluetoothAdapter = null;
  public static final String DEVICE_NAME = "device_name";

  Handler nmeaDataHandler;
  NMEAMesaages nmeaMessages;

  Handler rtcmDataHandler;
  NMEARTCMMessages nmeaRTCMMessages;

  // flag to indicate if mode is NMEA for File Logging
  private static boolean isNMEA = false;
  
  //File Logging
  static SaveNMEARTCM saveToFile;
  
  static Context context;

  //Shared Preference to save state of all checkboxes in NMEA and RTCM mode
  SharedPreferences nmeaRTCMSharedPreference;
  SharedPreferences.Editor prefEditor;
  private static final String KEY_NMEARTCM_SHARED_PREF = "nmeaRTCMSharedPrefKey";
  private static final String KEY_NMEARTCM_MODE = "nmeaRTCMModeKey";
  private static final String KEY_BLUETOOTH_NMEA = "nmeaBTOnOffKey";
  private static final String KEY_BLUETOOTH_RTCM = "rtcmBTOnOffKey";
  private static final String KEY_WIFI_NMEA = "nmeaWifiOnOffKey";
  private static final String KEY_WIFI_RTCM = "rtcmWifiOnOffKey";
  private static final String KEY_FILE_NMEA = "nmeaFileOnOffKey";
  private static final String KEY_FILE_RTCM = "rtcmFileOnOffKey";
  
  private boolean isWiFiCompatible = false;
  
  
  /**
   * findAllViews function 
   * 
   * Registers all the UI elements.
   */
  private void findAllViews() {

    // nmea rtcm views
    row_nmea = findViewById(R.id.option_nmea);
    row_rtcm = findViewById(R.id.option_rtcm);

    // nmea buttons
    sendNMEAViaBlueTooth = (Button) findViewById(R.id.sendNMEAViaBluetooth);
    sendNMEAViaWIFI = (Button) findViewById(R.id.sendNMEAViaWifi);
    saveNMEAToFileButton = (Button) findViewById(R.id.saveNMEAToFile);

    // nmea check box
    checkBlueToothNMEA = (CheckBox) findViewById(R.id.chk_nmea_bluetooth_send);
    checkFileNMEA = (CheckBox) findViewById(R.id.chk_nmea_file_logging);
    checkWIFINMEA = (CheckBox) findViewById(R.id.chk_nmea_wifi_send);

    // rtcm buttons
    receiveRTCMViaBluetooth = (Button) findViewById(R.id.receiveNmeaForRtcmViaBluetooth);
    sendRTCMViaBluetooth = (Button) findViewById(R.id.sendRTCMViaBluetooth);

    receiveRTCMViaWIFI = (Button) findViewById(R.id.receiveNmeaForRtcmViaWiFi);
    sendRTCMViaWIFI = (Button) findViewById(R.id.sendRTCMViaWifi);

    readRTCMFromFile = (Button) findViewById(R.id.readRTCMToFile);
    saveRTCMToFile = (Button) findViewById(R.id.saveRTCMToFile);

    // rtcm checkbox
    checkBlueToothRTCM = (CheckBox) findViewById(R.id.chk_rtcm_bluetooth);
    checkFileRTCM = (CheckBox) findViewById(R.id.chk_rtcm_file_logging);
    checkWIFIRTCM = (CheckBox) findViewById(R.id.chk_rtcm_wifi);

    spinner = (Spinner) findViewById(R.id.nmeaOrRTCMSPINNER);
    
    onGoingEventListView = (ListView)findViewById(R.id.onGoinglistView);     
    onGoingEventAdapter = new ArrayAdapter<String>(this,R.layout.ongoingevent,onGoingData);
    onGoingEventListView.setAdapter(onGoingEventAdapter);       
  }

  /**
   * registerListeners function 
   * 
   * Registers a callback for all the UI elements, when it is clicked.
   */
  private void registerListeners() {

    // nmea buttons
    sendNMEAViaBlueTooth.setOnClickListener(this);
    sendNMEAViaWIFI.setOnClickListener(this);
    saveNMEAToFileButton.setOnClickListener(this);

    // nmea check box
    checkBlueToothNMEA.setOnCheckedChangeListener(this);
    checkFileNMEA.setOnCheckedChangeListener(this);
    checkWIFINMEA.setOnCheckedChangeListener(this);

    // rtcm buttons
    receiveRTCMViaBluetooth.setOnClickListener(this);
    sendRTCMViaBluetooth.setOnClickListener(this);

    receiveRTCMViaWIFI.setOnClickListener(this);
    sendRTCMViaWIFI.setOnClickListener(this);

    readRTCMFromFile.setOnClickListener(this);
    saveRTCMToFile.setOnClickListener(this);

    // rtcm checkbox
    checkBlueToothRTCM.setOnCheckedChangeListener(this);
    checkFileRTCM.setOnCheckedChangeListener(this);
    checkWIFIRTCM.setOnCheckedChangeListener(this);

    spinner.setOnItemSelectedListener(this);    
    
    onGoingEventListView.setOnItemClickListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setupInitialState();
  }
  

  /**
   * setupInitialState function 
   * 
   * Sets up an initial state for all the UI elements.
   */
  private void setupInitialState() {  
    spinner.setSelection(nmeaRTCMSharedPreference.getInt(KEY_NMEARTCM_MODE, 0));
    
    checkBlueToothNMEA.setChecked(nmeaRTCMSharedPreference.getBoolean(KEY_BLUETOOTH_NMEA, true));
    
    /* this section to be removed later, once receive NMEA BT is added again.*/
    //for now the Receive NMEA BT will not be displayed
    receiveRTCMViaBluetooth.setVisibility(View.GONE);
    sendRTCMViaBluetooth.setEnabled(true);
    /*   ------------- end of section to be removed later---------*/
    
    if(isWiFiCompatible)
      checkWIFINMEA.setChecked(nmeaRTCMSharedPreference.getBoolean(KEY_WIFI_NMEA, true));
    else {
      checkWIFINMEA.setChecked(false);
      checkWIFINMEA.setEnabled(false);
    }
    checkFileNMEA.setChecked(nmeaRTCMSharedPreference.getBoolean(KEY_FILE_NMEA, true));
    
    checkBlueToothRTCM.setChecked(nmeaRTCMSharedPreference.getBoolean(KEY_BLUETOOTH_RTCM, true));
    if(isWiFiCompatible)
      checkWIFIRTCM.setChecked(nmeaRTCMSharedPreference.getBoolean(KEY_WIFI_RTCM, true));
    else {
     checkWIFIRTCM.setChecked(false);
     checkWIFIRTCM.setEnabled(false);
    }
    checkFileRTCM.setChecked(nmeaRTCMSharedPreference.getBoolean(KEY_FILE_RTCM, true));
  }

  /**
   * onCreate function 
   * 
   * Called on start of activity, displays the EGNOS
   * Correction output configurations
   */
  @Override
  public final void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.egnoscorrectioninputoutput);
    context = this;
    nmeaRTCMSharedPreference = getSharedPreferences(KEY_NMEARTCM_SHARED_PREF,
        MODE_WORLD_READABLE);
    prefEditor = nmeaRTCMSharedPreference.edit();
    
    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
    if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB) {
      Toast.makeText(this, "WiFi is currently not compatible on this device", Toast.LENGTH_LONG).show();
      isWiFiCompatible = false;
    }else 
      isWiFiCompatible = true;
    
    // Get local Bluetooth adapter
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    // If the adapter is null, then Bluetooth is not supported
    if (mBluetoothAdapter == null) {
      Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG)
          .show();
      finish();
      return;
    }
    
    findAllViews();
    registerListeners();
    setupInitialState();
   // handler = new Handler();

    // Create an ArrayAdapter using the string array and a default spinner
    String[] items = { "NMEA", "RTCM" };
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item, items);
    // Specify the layout to use when the list of choices appears
    adapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (D)
      Log.e(TAG, "++ ON START ++");
  }


  /**
   * onItemSelected function 
   * 
   * Called when an item is selected from the Spinner.
   * @param   parent     The AdapterView where the selection happened 
   * @param   view       The view within the AdapterView that was clicked
   * @param   position   The position of the view in the adapter
   * @param   id         The row id of the item that is selected 
   */
  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position,
      long id) {

    switch (position) {
    case 0: // NMEA selected
      row_nmea.setVisibility(View.VISIBLE);
      row_rtcm.setVisibility(View.GONE);
      break;
    case 1: // RTCM selected
      row_nmea.setVisibility(View.GONE);
      row_rtcm.setVisibility(View.VISIBLE);
      break;
    default:
      break;
    }
    prefEditor.putInt(KEY_NMEARTCM_MODE, position);
    prefEditor.commit();
  }

  /**
   * startNMEAHandler function 
   * 
   * Starts a handler to create NMEA messages every 1 second.
   */
  private void startNMEAHandler() {
    if (nmeaDataHandler == null) {
      nmeaDataHandler = new Handler();
      nmeaMessages = new NMEAMesaages(this);
      nmeaDataHandler.postDelayed(updateNMEAData, 0);
    }
  }

  /**
   * stopNMEAHandler function 
   * 
   * Stops the handler that creates NMEA messages.
   */
  private void stopNMEAHandler() {
    if (nmeaDataHandler != null) {
      nmeaDataHandler.removeCallbacks(updateNMEAData);
    }
  }

  /**
   * updateNMEAData Runnable 
   * 
   * Creates NMEA sentences every 1 second.
   * Also if the Bluetooth server is running, sends NMEA messages out.
   */
  Runnable updateNMEAData = new Runnable() {
    
    @Override
    public void run() {
      // fetch Rx Data and create NMEA sentences
      nmeaMessages.createNMEAData();
      if (null != mt) {
        // if bt is connected to a device then send messages
        if (mt.getState() == BluetoothMessageTransferService.STATE_CONNECTED) {
          mt.sendMessage(GlobalState.getGPGGASentence());
          mt.sendMessage(GlobalState.getGPGLLSentence());
          mt.sendMessage(GlobalState.getGPGSASentence());

          for (int i = 0; i < GlobalState.getGPGSVSentence().length; i++) {
            mt.sendMessage(GlobalState.getGPGSVSentence()[i]);
            mt.sendMessage(GlobalState.getGPRMCSentence());
            mt.sendMessage(GlobalState.getGPVTGSentence());
          }
        }
      }
      nmeaDataHandler.postDelayed(updateNMEAData, 1000);
    }
  };

  /**
   * startRTCMHandler function 
   * 
   * Starts a handler to create RTCM messages every 1 second.
   */
  private void startRTCMHandler() {
    if (rtcmDataHandler == null) {
      rtcmDataHandler = new Handler();
      nmeaRTCMMessages = new NMEARTCMMessages();
      rtcmDataHandler.postDelayed(updateRTCMData, 0);
    }
  }

  /**
   * stopRTCMHandler function 
   * 
   * Stops the handler that creates RTCM messages.
   */
  private void stopRTCMHandler() {
    if (rtcmDataHandler != null) {
      rtcmDataHandler.removeCallbacks(updateRTCMData);
    }
  }

  /**
   * updateRTCMData Runnable 
   * 
   * Creates RTCM sentences every 1 second.
   * Also if the Bluetooth server is running, sends RTCM messages out.
   */
  Runnable updateRTCMData = new Runnable() {
    @Override
    public void run() {
      nmeaRTCMMessages.createRTCMData();     
      
      if (null != mt) {
        if (mt.getState() == BluetoothMessageTransferService.STATE_CONNECTED) {

          if (GlobalState.getRtcmMessagesByte1() != null) {
            char[][] RTCMMsg1 = GlobalState.getRtcmMessagesByte1();
            for (int i = 0; i < RTCMMsg1.length; i++)
              mt.sendMessage(String.valueOf(RTCMMsg1[i]) + "\n");
          }

          if (GlobalState.getRtcmMessagesByte2() != null) {
            char[][] RTCMMsg2 = GlobalState.getRtcmMessagesByte2();
            for (int i = 0; i < RTCMMsg2.length; i++)
              mt.sendMessage(String.valueOf(RTCMMsg2[i]) + "\n");
          }

          if (GlobalState.getRtcmMessagesByte1() != null) {
            char[][] RTCMMsg1 = GlobalState.getRtcmMessagesByte1();
            for (int i = 0; i < RTCMMsg1.length; i++)
              mt.sendMessage(String.valueOf(RTCMMsg1[i]) + "\n");
          }
        }
      }
      
      rtcmDataHandler.postDelayed(updateRTCMData, 1000);
    }
  };

  @Override
  public void onNothingSelected(AdapterView<?> arg0) {

  }

  /**
   * onClick function 
   * 
   * Called when one of the views have been clicked.
   * @param    v    the view that was clicked 
   */
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.sendNMEAViaBluetooth: // send NMEA data via Bluetooth
      if (checkBlueToothNMEA.isChecked()) {
        stopRTCMHandler();
        startNMEAHandler();
        if (sendNMEAViaBlueTooth.getText().equals("Send")) {
          Toast.makeText(this, "sending via bluetooth", Toast.LENGTH_LONG)
              .show();
          sendNMEAViaBlueTooth.setText("View");
          addOnGoingItems(NMEA_BT_ID,NMEA_BT_SEND);
          mt.start();
          return;
        }
        if (sendNMEAViaBlueTooth.getText().equals("View")) 
          openNMEARTCMView(NMEA_BT_ID);
      } else {
        removeOnGoingItem(NMEA_BT_ID);
        mt.stop();
      }
      break;
    case R.id.sendNMEAViaWifi:
      if (sendNMEAViaWIFI.getText().equals("Send")) {
          sendNMEAViaWIFI.setText("View");
          addOnGoingItems(NMEA_WIFI_ID,NMEA_WIFI_SEND);
          Intent wifiDirectActivity = new Intent(this, WiFiDirectActivity.class);
          startActivity(wifiDirectActivity);
          return;
      }
      if (sendNMEAViaWIFI.getText().equals("View")) 
        openNMEARTCMView(NMEA_WIFI_ID);
      break;
    case R.id.saveNMEAToFile:// save NMEA data to File
      if (checkFileNMEA.isChecked()) {
        isNMEA = true;
        stopRTCMHandler();
        startNMEAHandler();
        if (saveNMEAToFileButton.getText().equals("Save")) {
          displayFileLoggingPopup();            
          return;
        }
        if (saveNMEAToFileButton.getText().equals("View")) 
          openNMEARTCMView(NMEA_FL_ID);
      } else
        closeFile();
      break;
    case R.id.receiveNmeaForRtcmViaBluetooth:
      GlobalState.setbTMessageTransferServiceInstance(mt);
      chooseBluetoothReceiver();      
      break;
    case R.id.sendRTCMViaBluetooth:// send RTCM data via Bluetooth
      if (checkBlueToothRTCM.isChecked()) {
        stopNMEAHandler();
        startRTCMHandler();
        if (sendRTCMViaBluetooth.getText().equals("Send")) {
          Toast.makeText(this, "sending via bluetooth", Toast.LENGTH_LONG)
              .show();
          sendRTCMViaBluetooth.setText("View");
          addOnGoingItems(NMEARTCM_BT_ID,NMEARTCM_BT_SEND);
          return;
        }
        if (sendRTCMViaBluetooth.getText().equals("View")) 
          openNMEARTCMView(NMEARTCM_BT_ID);
      } else {
        removeOnGoingItem(NMEARTCM_BT_ID);
        mt.stop();
      }
      break;
    case R.id.sendRTCMViaWifi:
      if (sendRTCMViaWIFI.getText().equals("Send")) {
        sendRTCMViaWIFI.setText("View");
        addOnGoingItems(NMEARTCM_WIFI_ID,NMEARTCM_WIFI_SEND);
        Intent wifiDirectActivity = new Intent(this, WiFiDirectActivity.class);
        startActivity(wifiDirectActivity);
        return;
      }
      if (sendRTCMViaWIFI.getText().equals("View")) 
        openNMEARTCMView(NMEARTCM_WIFI_ID);      
      break;
    case R.id.saveRTCMToFile:// save RTCM Data to File
      if (checkFileRTCM.isChecked()) {
        isNMEA = false;
        stopNMEAHandler();
        startRTCMHandler();
        
        if (saveRTCMToFile.getText().equals("Save")) {
          displayFileLoggingPopup();          
          return;
        }
        if (saveRTCMToFile.getText().equals("View")) 
          openNMEARTCMView(NMEARTCM_FL_ID);          
      } else
        closeFile();
      break;      
    default:
      break;
    }
  }

  
  /**
   * onItemClick function 
   * 
   * Called when one of the views have been clicked.
   * @param    v    the view that was clicked 
   */
  @Override
  public void onItemClick(AdapterView<?> av, View v, int position, long id) {
    int idSelected = (int)onGoingEventListView.getItemIdAtPosition(position);
    openNMEARTCMView(onGoingDataID.get(idSelected));
  }
  
  /**
   * addOnGoingItems function 
   * 
   * Adding OnGoing events like send via Bluetooth, Wi-Fi or save to file
   * to the an arrayList which is populated in a ListView.
   *  
   * @param    id                 the id of the event to be added
   * @param    onGoingString      the event to be added
   */
  private static void addOnGoingItems(String id, String onGoingString) {
    onGoingData.add(onGoingString);
    onGoingDataID.add(id);   
    onGoingEventAdapter.notifyDataSetChanged();
  }
  
  /**
   * removeOnGoingItem function 
   * 
   * Remove OnGoing events like send via Bluetooth, Wi-Fi or save to file
   * from the arrayList which is populated in a ListView.
   *  
   * @param    id       the id of the event to be removed.
   */
  private void removeOnGoingItem(String id) {
    int index;
    
    index = onGoingDataID.indexOf(id);
    if (index != -1) {
      onGoingDataID.remove(index);
      onGoingData.remove(index);

      onGoingEventAdapter.notifyDataSetChanged();
    }
  }
  
  /**
   * openNMEARTCMView function 
   * 
   * Opens the NMEA/RTCM view based on the OnGoing event clicked.
   *  
   * @param    ID       the id of the event
   */
  private static void openNMEARTCMView(String ID) {
    Intent openView = null;
    
    if(ID.equals(NMEA_BT_ID) || ID.equals(NMEA_WIFI_ID) || ID.equals(NMEA_FL_ID)) 
      openView = new Intent(context, NMEAData.class);        
    else if(ID.equals(NMEARTCM_BT_ID) || ID.equals(NMEARTCM_WIFI_ID) || ID.equals(NMEARTCM_FL_ID)) 
      openView = new Intent(context, NMEARTCMActivity.class);        
    
    openView.putExtra("NMEARTCM",onGoingDataID);
    context.startActivity(openView);  
  }

  /**
   * displayFileLoggingPopup function 
   * 
   * Displays a popup when the Save button is clicked.
   * This popup contains an EditText where the name of the file 
   * can be edited. The popup contains a Save and View button also.
   * Save button, saves the NMEA/RTCM data to a file,, and View button
   * displays the NMEA/RTCM View.
   */
   private void displayFileLoggingPopup() {

    LayoutInflater factory = LayoutInflater.from(context);
    View fileLoggingView = factory.inflate(R.layout.filelogging, null);

    final EditText fileNameEditText = (EditText) fileLoggingView
        .findViewById(R.id.fileNameEditText);

    Calendar cal = Calendar.getInstance();
    Date d = cal.getTime();
    String curTime, curDate;
    SimpleDateFormat sdf;
    sdf = new SimpleDateFormat("HHMM");
    curTime = sdf.format(d);
    sdf = new SimpleDateFormat("ddMMyyyy");
    curDate = sdf.format(d);

    if (isNMEA)
      fileNameEditText
          .setText("EGNOS-NMEA-" + curDate + "-" + curTime + ".log");
    else
      fileNameEditText
          .setText("EGNOS-RTCM-" + curDate + "-" + curTime + ".log");

    final Builder fileLogDialog = new AlertDialog.Builder(context);
    fileLogDialog
        .setTitle(R.string.chk_file_logging)
        .setView(fileLoggingView)
        .setPositiveButton(R.string.save_to_file_button_string,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(final DialogInterface dialog,
                  final int whichButton) {
                Toast.makeText(EGNOSCorrectionInputOutput.context,
                    "Saving to file", Toast.LENGTH_LONG).show();
                
                saveToFile(fileNameEditText.getText().toString());
             
              }
            })
        .setNegativeButton(R.string.view,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(final DialogInterface dialog, final int which) {
                if (isNMEA)
                  openNMEARTCMView(NMEA_FL_ID);
                else
                  openNMEARTCMView(NMEARTCM_FL_ID);
              }
            }).show();
  }

  /**
   * saveToFile function
   * 
   * Saves the NMEA/RTCM data to a file.
   * 
   * @param fileName       the name of the file to be saved
   */
  protected static void saveToFile(String fileName) {
      saveToFile = new SaveNMEARTCM(isNMEA, fileName);
      if (isNMEA) {        
        addOnGoingItems(NMEA_FL_ID, NMEA_FL_SAVE+fileName);
        saveNMEAToFileButton.setText("View");
      }else { 
        addOnGoingItems(NMEARTCM_FL_ID, NMEARTCM_FL_SAVE+fileName);
        saveRTCMToFile.setText("View");
      }
  }
  
  /**
   * closeFile function
   * 
   * Closes the file that saves NMEA/RTCM data.
   */
  protected void closeFile() {
    if(null != saveToFile) {
      saveToFile.stopNMEARTCMSaveThread();
      saveToFile.closeFile();
    }
  }


  /**
   * chooseBluetoothReceiver function
   * 
   * Called when Choose Bluetooth Receiver preference is clicked starts Activity
   * to list any bluetooth devices i.e. BluetoothReceiverList activity.
   * 
   * @return true if click was handled.
   **/
  protected final boolean chooseBluetoothReceiver() {
    Intent bluetoothReceiverIntent = new Intent(this, BluetoothSenderList.class);
    startActivityForResult(bluetoothReceiverIntent, REQUEST_CONNECT_DEVICE);
    return true;
  }


  /**
   * onCheckedChanged function
   * 
   * Called when the checked state of a CheckBox has changed.
   * 
   * @param buttonView     the CheckBox whose state has been changed.
   * @param ischecked      the new state of the CheckBox.
   **/
  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    switch (buttonView.getId()) {
    case R.id.chk_nmea_bluetooth_send:
      sendNMEAViaBlueTooth.setEnabled(isChecked);
      if (!isChecked) {
        sendNMEAViaBlueTooth.setText("Send");
        removeOnGoingItem(NMEA_BT_ID);
      }else {
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
          Intent enableIntent = new Intent(
              BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
          if (mt == null){
            setupMessingEngin();
          }
        }
      }
      prefEditor.putBoolean(KEY_BLUETOOTH_NMEA, isChecked);
      break;
    case R.id.chk_nmea_wifi_send:
      sendNMEAViaWIFI.setEnabled(isChecked);
      if (!isChecked) {
        sendNMEAViaWIFI.setText("Send");
        removeOnGoingItem(NMEA_WIFI_ID);
      }
      prefEditor.putBoolean(KEY_WIFI_NMEA, isChecked);
      break;
    case R.id.chk_nmea_file_logging:
      saveNMEAToFileButton.setEnabled(isChecked);
      if (!isChecked) {
        closeFile();
        saveNMEAToFileButton.setText("Save");
        removeOnGoingItem(NMEA_FL_ID);
      }
      prefEditor.putBoolean(KEY_FILE_NMEA, isChecked);
      break;
    case R.id.chk_rtcm_bluetooth:
      //setEnabled to false instead of isChecked once Receive NMEA BT is added
      sendRTCMViaBluetooth.setEnabled(isChecked);
      receiveRTCMViaBluetooth.setEnabled(isChecked);
      if (!isChecked) {
        sendRTCMViaBluetooth.setText("Send"); 
        removeOnGoingItem(NMEARTCM_BT_ID);
      } else {
        // do this action if NMEA BT is not checked, to avoid creating another
        // object for BluetoothMessageTransferService
        if (!checkBlueToothNMEA.isChecked()) {
          // If BT is not on, request that it be enabled.
          // setupMessingEngin(); //will then be called during onActivityResult
          if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
          } else {
            if (mt == null){
              setupMessingEngin();
              }
          }
        }
      }
      prefEditor.putBoolean(KEY_BLUETOOTH_RTCM, isChecked);
      break;
    case R.id.chk_rtcm_wifi:
      sendRTCMViaWIFI.setEnabled(isChecked);
      receiveRTCMViaWIFI.setEnabled(isChecked);
      if (!isChecked) {
        sendRTCMViaWIFI.setText("Send");
        removeOnGoingItem(NMEARTCM_WIFI_ID);
      }
      prefEditor.putBoolean(KEY_WIFI_RTCM, isChecked);
      break;
    case R.id.chk_rtcm_file_logging:
      readRTCMFromFile.setEnabled(isChecked);
      saveRTCMToFile.setEnabled(isChecked);
      if (!isChecked) {
        closeFile();
        saveRTCMToFile.setText("Save");
        removeOnGoingItem(NMEARTCM_FL_ID);
      }
      prefEditor.putBoolean(KEY_FILE_RTCM, isChecked);
      break;
    default:
      break;
    }
    prefEditor.commit();
  }

  private static final int REQUEST_CONNECT_DEVICE = 15;
  
  /**
   * onCheckedChanged function
   * 
   * Called when an activity you launched exits
   *  
   * @param requestCode     The integer request code originally supplied to startActivityForResult(),
   *                        allowing you to identify who this result came from. 
   * @param resultCode      The integer result code returned by the child activity 
   *                        through its setResult().
   * @param data            An Intent, which can return result data to the caller 
   *                        (various data can be attached to Intent "extras").
   **/
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
    case REQUEST_CONNECT_DEVICE:
      // When DeviceListActivity returns with a device to connect
      if (resultCode == Activity.RESULT_OK) {
        // Get the device MAC address
        String address = data.getExtras().getString(
            BluetoothSenderList.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mt.connect(device);
      }
      break;
    case REQUEST_ENABLE_BT:
      setupMessingEngin();
      break;
    default:
      break;
    }
  }
  
  /**
   * mHandler Handler
   * 
   * The Handler that gets information back from the
   * BluetoothMessageTransferService
   **/
  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case BluetoothMessageTransferService.MESSAGE_READ:
        sendRTCMViaBluetooth.setEnabled(true);
        byte[] readBuf = (byte[]) msg.obj;
        String readMessage = new String(readBuf, 0, msg.arg1);
        Log.d(TAG, "NMEA Messages: " + readMessage);
        GlobalState.setNMEARTCMMessages(readMessage);
        addOnGoingItems(NMEARTCM_BT_ID, NMEARTCM_BT_RECEIVE);
        break;
      case BluetoothMessageTransferService.MESSAGE_DEVICE_NAME:
        // save the connected device's name
        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
        Toast.makeText(getApplicationContext(),
            "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
        break;
      case BluetoothMessageTransferService.MESSAGE_TOAST:
        Toast.makeText(getApplicationContext(),
            msg.getData().getString(BluetoothMessageTransferService.TOAST),
            Toast.LENGTH_SHORT).show();
        break;
      }
    }
  };

  
  /**
   * setupMessingEngin function
   * 
   * Initialize the BluetoothMessageTransferService to perform bluetooth
   * connections.
   **/
  private void setupMessingEngin() {
    mt = new BluetoothMessageTransferService(this, mHandler);
  }

  /**
   * Sends a message.
   * 
   * @param message    A string of text to send.
   */
  private void sendMessage(String message) {
    // Check that we're actually connected before trying anything
    if (mt.getState() != BluetoothMessageTransferService.STATE_CONNECTED) {
      Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
      return;
    }

    // Check that there's actually something to send
    if (message.length() > 0) {
      // Get the message bytes and tell the
      // BluetoothMessageTransferService to write
      byte[] send = message.getBytes();
      mt.write(send);
    }
  }

  private static String nmeaMessageString = "";

  public static String getNMEAString() {
    return nmeaMessageString;
  }

  private synchronized static void setNmeaMessageString(String s) {
    nmeaMessageString = s;
  }

  static int i;

  static String getNMEAMessage() {
    return "NMEA MESSAGE " + i++;
  }

  private class ParallelTask extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... params) {
      // TODO Auto-generated method stub
      while (true) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        publishProgress(1);
      }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      // TODO Auto-generated method stub
      super.onProgressUpdate(values);

      setNmeaMessageString(getNMEAMessage());
      if (mt.getState() == BluetoothMessageTransferService.STATE_CONNECTED) {
        Log.e("RUNNING", " Message :- " + getNMEAString());
        sendMessage(getNMEAString());
      }

      // }
    }
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
   if(mt!= null)
     mt.stop();
   
    closeFile();
  }

  //==========================================================
  public static final String TAG_WIFI = "wifidirectdemo";
  private WifiP2pManager manager;
  private Channel channel;
  private BroadcastReceiver receiver = null;
  
  /*
   * method to update on WiFiStatus and event changes 
   * */
  public void setStatus(String statusMessage){
	 //code to update UI as per messages send from broadcast receiver and or WiFiMessegingEngin
	 //[TODO] Supriya 
  }
    
  /*
   *  create TntentFilter for regeistering broadcast of specific kind (related to WiFiDirect Events
   * */
  private final IntentFilter intentFilter = new IntentFilter();
  private IntentFilter getIntentFilter(){
	  
      intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
      intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
      intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
      intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

	  return intentFilter;
  
  }
  /*
   * start chanel called in onCreat()
   * */
  private void startChannel(){
	//  channel = manager.initialize(this, getMainLooper(), null);
  }
  
  /*
   * regeister and unregeister called from onResume and onPause repectevely
   * */
//  private void registerWiFiReceiver(){
//	  receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
//      registerReceiver(receiver, intentFilter);
//  }
  private void unRegisterWiFiReceiver(){
	  unregisterReceiver(receiver);
  }
  //==========================================================
}
class BlueToothMessageSender extends Thread {
	private String TAG = "BlueToothMessageSender Thread";
	private boolean run;
	private Handler handler;
//	private TextView textView;
//	private ScrollView scroller;
	private BluetoothMessageTransferService btService;
	private Context appContext;
	int i = 0;

	public BlueToothMessageSender(Handler h,Context c) {
		handler = h;
		appContext = c;
		Log.d(TAG, "Create Bt serverInstance");
		// Initialize the BluetoothMessageTransferService to perform bluetooth
		// connections
		btService = new BluetoothMessageTransferService(appContext, handler);
		//start bt server
		btService.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		run = true;
		while (run) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//if bt is connected to a device then send messages
			if(btService.getState() == BluetoothMessageTransferService.STATE_CONNECTED){
				btService.sendMessage(GlobalState.getGPGGASentence()+"\n");
				btService.sendMessage(GlobalState.getGPGLLSentence()+"\n");
				btService.sendMessage(GlobalState.getGPGSASentence()+"\n");

				for(int i = 0; i < GlobalState.getGPGSVSentence().length; i++){
					btService.sendMessage(GlobalState.getGPGSVSentence()[i]+"\n");
					btService.sendMessage(GlobalState.getGPRMCSentence()+"\n");						
					btService.sendMessage(GlobalState.getGPVTGSentence()+"\n");
				  }
			}

		}
		return;
	}

	public void cancel() {
		run = false;
	}
}


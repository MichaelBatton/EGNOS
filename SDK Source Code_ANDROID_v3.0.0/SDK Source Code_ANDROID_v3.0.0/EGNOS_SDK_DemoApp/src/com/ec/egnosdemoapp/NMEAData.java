/**
 * @file NMEAData.java
 *
 * Activity called when the "View" button is clicked 
 * against Bluetooth, WiFi or File Logging.
 * Displays the NMEA messages 
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
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ec.R;
import com.ec.egnossdk.GlobalState;

public class NMEAData extends Activity implements OnClickListener {

  public static final int BLUETOOTH_RADIO_BUTTON = R.id.sendRadio1;
  public static final int WIFI_RADIO_BUTTON = R.id.sendRadio2;
  public static final int FILE_RADIO_BUTTON = R.id.sendRadio3;
  private Button disconnectButton, backButton, sendAlsoViaButton;
  private TextView nmeaText;
  private ScrollView nmeaScroller;

  private NMEAThread nmeaThread;

  private Handler handler;

  private ArrayList<String> mode = new ArrayList<String>();
  int currentapiVersion;
  
  
  
  private void findAllViews() {
    disconnectButton = (Button) findViewById(R.id.nmea_button_disconnect);
    backButton = (Button) findViewById(R.id.nmea_button_back);
    sendAlsoViaButton = (Button) findViewById(R.id.nmea_button_send_also_via);
    nmeaText = (TextView) findViewById(R.id.nmea_output_text);
    nmeaScroller = (ScrollView) findViewById(R.id.nmeaDataScroller);
  }

  private void registerListeners() {
    disconnectButton.setOnClickListener(this);
    backButton.setOnClickListener(this);
    sendAlsoViaButton.setOnClickListener(this);
  }

  private void stopNMEAThread() {
    nmeaThread.cancel();
  }

  private void startNMEAThread() {
    nmeaThread = new NMEAThread(handler, nmeaText, nmeaScroller);
    nmeaThread.start();
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

    setContentView(R.layout.nmeadata);
    handler = new Handler();

    currentapiVersion = android.os.Build.VERSION.SDK_INT;

    Bundle extras = getIntent().getExtras();
    if (extras != null)
      mode = extras.getStringArrayList("NMEARTCM");

    findAllViews();
    registerListeners();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    stopNMEAThread();
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopNMEAThread();
  }

  @Override
  protected void onResume() {
    super.onResume();
    startNMEAThread();
  }

  class NMEAThread extends Thread {
    private boolean run;
    private Handler handler;
    private TextView textView;
    private ScrollView scroller;
    int i = 0;

    public NMEAThread(Handler h, TextView tv, ScrollView sc) {
      handler = h;
      textView = tv;
      scroller = sc;
    }

    @Override
    public void run() {
      super.run();

      run = true;
      while (run) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        handler.post(new Runnable() {

          @Override
          public void run() {
            if (GlobalState.getGPVTGSentence() != null) {
              textView.append(GlobalState.getGPGGASentence() + "\n");
              textView.append(GlobalState.getGPGLLSentence() + "\n");
              textView.append(GlobalState.getGPGSASentence() + "\n");
              for (int i = 0; i < GlobalState.getGPGSVSentence().length; i++)
                textView.append(GlobalState.getGPGSVSentence()[i] + "\n");
              textView.append(GlobalState.getGPRMCSentence() + "\n");
              textView.append(GlobalState.getGPVTGSentence() + "\n");
            }
            if (textView.getLineCount() > 400) {
              textView.setText("");
            }
            scroller.postDelayed(new Runnable() {
              public void run() {
                scroller.fullScroll(ScrollView.FOCUS_DOWN);
              }
            }, 100);
          }
        });
      }
      return;
    }

    public void cancel() {
      run = false;
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.nmea_button_disconnect:
      stopNMEAThread();
      break;
    case R.id.nmea_button_send_also_via:
      if (mode.contains(EGNOSCorrectionInputOutput.NMEA_BT_ID)
          && mode.contains(EGNOSCorrectionInputOutput.NMEA_WIFI_ID)
          && mode.contains(EGNOSCorrectionInputOutput.NMEA_FL_ID)) {
      } else if (mode.contains(EGNOSCorrectionInputOutput.NMEA_BT_ID)
          && mode.contains(EGNOSCorrectionInputOutput.NMEA_FL_ID))
        if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB) {

        } else
          displaySendAlsoViaPopup();
      else
        displaySendAlsoViaPopup();
      break;
    case R.id.nmea_button_back:
      finish();
      break;
    default:
      break;
    }
  }

  private void displaySendAlsoViaPopup() {

    LayoutInflater factory = LayoutInflater.from(this);
    View sendAlsoViaView = factory.inflate(R.layout.sendalsovia, null);

    TextView sendViaBT = (TextView) sendAlsoViaView
        .findViewById(R.id.send1_textView);
    RadioButton sendBTRB = (RadioButton) sendAlsoViaView
        .findViewById(R.id.sendRadio1);

    TextView sendViaWifi = (TextView) sendAlsoViaView
        .findViewById(R.id.send2_textView);
    RadioButton sendWifiRB = (RadioButton) sendAlsoViaView
        .findViewById(R.id.sendRadio2);

    TextView saveToFile = (TextView) sendAlsoViaView
        .findViewById(R.id.send3_textView);
    RadioButton saveTofileRB = (RadioButton) sendAlsoViaView
        .findViewById(R.id.sendRadio3);
    final RadioGroup sendRadioGroup = (RadioGroup) sendAlsoViaView
        .findViewById(R.id.sendAlsoradioGroup);

    if (mode.contains(EGNOSCorrectionInputOutput.NMEA_BT_ID)) {
      sendViaBT.setEnabled(false);
      sendViaBT.setVisibility(View.GONE);
      sendBTRB.setEnabled(false);
      sendBTRB.setVisibility(View.GONE);
    } else {
      sendViaBT.setEnabled(true);
      sendBTRB.setEnabled(true);
    }

    if (mode.contains(EGNOSCorrectionInputOutput.NMEA_WIFI_ID)) {
      sendViaWifi.setEnabled(false);
      sendViaWifi.setVisibility(View.GONE);
      sendWifiRB.setEnabled(false);
      sendWifiRB.setVisibility(View.GONE);
    } else {
      if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB) {
        sendViaWifi.setEnabled(false);
        sendViaWifi.setVisibility(View.GONE);
        sendWifiRB.setEnabled(false);
        sendWifiRB.setVisibility(View.GONE);
      } else {
        sendViaWifi.setEnabled(true);
        sendWifiRB.setEnabled(true);
      }
    }

    if (mode.contains(EGNOSCorrectionInputOutput.NMEA_FL_ID)) {
      saveToFile.setEnabled(false);
      saveToFile.setVisibility(View.GONE);
      saveTofileRB.setEnabled(false);
      saveTofileRB.setVisibility(View.GONE);
    } else {
      saveToFile.setEnabled(true);
      saveTofileRB.setEnabled(true);
    }

    final Builder sendAlsoViaDialog = new AlertDialog.Builder(this);
    sendAlsoViaDialog
        .setTitle(R.string.sendAlsoVia)
        .setView(sendAlsoViaView)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(final DialogInterface dialog,
              final int whichButton) {
            ;
            sendalsoVia(sendRadioGroup.getCheckedRadioButtonId());
          }
        })
        .setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(final DialogInterface dialog, final int which) {

              }
            }).show();
  }

  protected void sendalsoVia(int checkedRadioButtonId) {
    switch (checkedRadioButtonId) {
    case BLUETOOTH_RADIO_BUTTON:
      break;
    case WIFI_RADIO_BUTTON:
      break;
    case FILE_RADIO_BUTTON:
      displayNMEAFileLoggingPopup();
      break;
    }

  }

  void displayNMEAFileLoggingPopup() {

    LayoutInflater factory = LayoutInflater.from(this);
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

    fileNameEditText.setText("EGNOS-NMEA-" + curDate + "-" + curTime + ".log");

    final Builder fileLogDialog = new AlertDialog.Builder(this);
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

                EGNOSCorrectionInputOutput.saveToFile(fileNameEditText
                    .getText().toString());

              }
            })
        .setNegativeButton(R.string.view,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(final DialogInterface dialog, final int which) {

              }
            }).show();
  }

}
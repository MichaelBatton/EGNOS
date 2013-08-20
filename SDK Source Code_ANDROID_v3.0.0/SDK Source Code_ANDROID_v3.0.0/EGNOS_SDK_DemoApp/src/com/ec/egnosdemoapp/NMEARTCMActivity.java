/**
 * @file NMEARTCMActivity.java
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

public class NMEARTCMActivity extends Activity implements OnClickListener{
  public static final int BLUETOOTH_RADIO_BUTTON = R.id.sendRadio1;
  public static final int WIFI_RADIO_BUTTON = R.id.sendRadio2;
  public static final int FILE_RADIO_BUTTON = R.id.sendRadio3;
	private Button disconnectButton,backButton,sendAlsoViaButton;
	private TextView nmeaText,rtcmText;
	private ScrollView nmeaScroller,rtcmScroller;
	
  private NMEARTCMThread nmeaRTCMThread;

  private Handler handler;
  private ArrayList<String> mode = new ArrayList<String>();
  
  int currentapiVersion;

	private void findAllViews(){
		disconnectButton  = (Button) findViewById(R.id.rtcm_button_disconnect);
		backButton = (Button) findViewById(R.id.rtcm_button_back);
		sendAlsoViaButton = (Button) findViewById(R.id.rtcm_button_sendAlsoVia);
	//nmeaText = (TextView) findViewById(R.id.rtcm_output_nmea_window);
		rtcmText = (TextView) findViewById(R.id.rtcm_output_rtcm_window);
		
	//nmeaScroller = (ScrollView) findViewById(R.id.nmeartcmscrollView);
		rtcmScroller = (ScrollView) findViewById(R.id.rtcmscrollView);
	}
	private void registerListeners(){
		disconnectButton.setOnClickListener(this);
		backButton.setOnClickListener(this);
		sendAlsoViaButton.setOnClickListener(this);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.nmeartcm_alt);
	    handler = new Handler();
      
	    currentapiVersion = android.os.Build.VERSION.SDK_INT;
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) 
	      mode = extras.getStringArrayList("NMEARTCM");
	    	    
	    findAllViews();
	    registerListeners();
	}
	
  private void stopNMEARTCMThread() {
    nmeaRTCMThread.cancel();
  }

  private void startNMEARTCMThread() {
    nmeaRTCMThread = new NMEARTCMThread(handler, nmeaText, nmeaScroller,
        rtcmText, rtcmScroller);
    nmeaRTCMThread.start();
  }
  @Override
  protected void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
    stopNMEARTCMThread();
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    stopNMEARTCMThread();
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    startNMEARTCMThread();
  }
	  
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.rtcm_button_disconnect:
		  stopNMEARTCMThread();
			break;
		case R.id.rtcm_button_sendAlsoVia:
      if(mode.contains(EGNOSCorrectionInputOutput.NMEARTCM_BT_ID) &&
          mode.contains(EGNOSCorrectionInputOutput.NMEARTCM_WIFI_ID)&& 
          mode.contains(EGNOSCorrectionInputOutput.NMEARTCM_FL_ID)) {}
       else 
         if(mode.contains(EGNOSCorrectionInputOutput.NMEARTCM_BT_ID) &&
            mode.contains(EGNOSCorrectionInputOutput.NMEARTCM_FL_ID))
             if(currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB) {
               
             }else 
               displaySendAlsoViaPopup();
         else 
         displaySendAlsoViaPopup();
			break;
	  case R.id.rtcm_button_back:
	      finish();
	      break;
		default:
			break;
		}
	}	

  class NMEARTCMThread extends Thread {
    private boolean run;
    private Handler handler;
    private TextView nmeaTextView;
    private TextView rtcmtextView;
    private ScrollView nmeascroller;
    private ScrollView rtcmscroller;
    int i = 0;

    public NMEARTCMThread(Handler h, TextView tv, ScrollView sc, TextView tv1,
        ScrollView sc1) {
      handler = h;
      nmeaTextView = tv;
      rtcmtextView = tv1;
      nmeascroller = sc;
      rtcmscroller = sc1;
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
        handler.post(new Runnable() {

          @Override
          public void run() {
              rtcmtextView.setText("");
             
              if(GlobalState.getRtcmMessage1()!= null) {
                  rtcmtextView.append(String.valueOf(GlobalState.getRtcmMessage1())+"\n");
              }
              
              if(GlobalState.getRtcmMessage2()!= null) {
                  rtcmtextView.append(String.valueOf(GlobalState.getRtcmMessage2())+"\n");
              }
              
              if(GlobalState.getRtcmMessage3()!= null) {
                  rtcmtextView.append(String.valueOf(GlobalState.getRtcmMessage3())+"\n");
              }
              
//            if(rtcmtextView.getLineCount() > 400){
//                rtcmtextView.setText("");
//            }
            
            rtcmscroller.postDelayed(new Runnable() {
              public void run() {
                rtcmscroller.fullScroll(ScrollView.FOCUS_DOWN);
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

    if (mode.contains(EGNOSCorrectionInputOutput.NMEARTCM_BT_ID)) {
      sendViaBT.setEnabled(false);
      sendViaBT.setVisibility(View.GONE);
      sendBTRB.setEnabled(false);
      sendBTRB.setVisibility(View.GONE);
    }else {
      sendViaBT.setEnabled(true);
      sendBTRB.setEnabled(true);
    }
    
    if (mode.contains(EGNOSCorrectionInputOutput.NMEARTCM_WIFI_ID)) {
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
      }else {
       sendViaWifi.setEnabled(true);
       sendWifiRB.setEnabled(true);
      }
    }
      
    if (mode.contains(EGNOSCorrectionInputOutput.NMEARTCM_FL_ID)) {
      saveToFile.setEnabled(false);
      saveToFile.setVisibility(View.GONE);
      saveTofileRB.setEnabled(false);
      saveTofileRB.setVisibility(View.GONE);
    }else {
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
      displayRTCMFileLoggingPopup();
      break;
    }
  }
  
  void displayRTCMFileLoggingPopup() {

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

    fileNameEditText
          .setText("EGNOS-RTCM-" + curDate + "-" + curTime + ".log");


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
                
                EGNOSCorrectionInputOutput.saveToFile(fileNameEditText.getText().toString());
             
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



package com.ec.egnosdemoapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ec.R;
import com.ec.egnossdk.BluetoothMessageTransferService;
import com.ec.egnossdk.GlobalState;

public class EGNOSIoSettingActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private static String TAG = "EGNOS-IO-Setting";

	private PreferenceScreen preferenceScreen;

	private ListPreference nmeaOrRtcmPref;
	private PreferenceCategory nmea_setting_options, rtcm_setting_options;
	private CheckBoxPreference nmea_bt;
	private CheckBoxPreference nmea_wifi;
	private CheckBoxPreference nmea_file;

	private CheckBoxPreference rtcm_bt;
	private CheckBoxPreference rtcm_wifi;
	private CheckBoxPreference rtcm_file;
	
	private BlueToothMessageSender blueToothServerInstance;

	private void findAllActivityElements() {

		preferenceScreen = (PreferenceScreen) findPreference(getResources()
				.getString(R.string.egnos_io_preference_screen_key));

		nmeaOrRtcmPref = (ListPreference) findPreference("nmea-or-rtcm-pref");
		nmea_setting_options = (PreferenceCategory) findPreference("nmea_setting_options");
		rtcm_setting_options = (PreferenceCategory) findPreference("rtcm_setting_options");

		nmea_bt = (CheckBoxPreference) findPreference("nmea_bt");
		nmea_wifi = (CheckBoxPreference) findPreference("nmea_wifi");
		nmea_file = (CheckBoxPreference) findPreference("nmea_file");

		rtcm_bt = (CheckBoxPreference) findPreference("rtcm_bt");
		rtcm_wifi = (CheckBoxPreference) findPreference("rtcm_wifi");
		rtcm_file = (CheckBoxPreference) findPreference("rtcm_file");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.egnos_io_setting);
		findAllActivityElements();
		processInitialState();
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, key);
		if (key.equals("nmea-or-rtcm-pref")) {
			// NMEA or RTCM option is selected
			Log.v(TAG, "MEdgdfgsdfgsdfg");
			String val = sharedPreferences.getString("nmea-or-rtcm-pref",
					"Default list prefs");
			Log.v(TAG, val);
			if (val.contains("NMEA")) {

				preferenceScreen.addPreference(nmea_setting_options);
				preferenceScreen.removePreference(rtcm_setting_options);

			}
			if (val.contains("RTCM")) {
				preferenceScreen.addPreference(rtcm_setting_options);
				preferenceScreen.removePreference(nmea_setting_options);
			}
		}
		if (key.equals("nmea_setting_options")) {
			// NMEA option is selected
		}
		if (key.equals("rtcm_setting_options")) {
			// RTCM option is selected
		}

		if (key.equals("nmea_bt")) {
			// RTCM option is selected
			if(sharedPreferences.getBoolean(key, false)){
				if(blueToothServerInstance == null){
					blueToothServerInstance = new BlueToothMessageSender(new Handler(), this);
					blueToothServerInstance.start();
					nmea_bt.setSummary("Server Started");
				}
			} else{
				if(blueToothServerInstance != null){
					blueToothServerInstance.cancel();
					blueToothServerInstance = null;
					nmea_bt.setSummary("Server Stopped");
				}
			}
			return;
		}
		if (key.equals("nmea_wifi")) {
			// RTCM option is selected
			return;
		}
		if (key.equals("nmea_file")) {
			// RTCM option is selected
			return;
		}

		if (key.equals("rtcm_bt")) {
			// RTCM option is selected
			return;
		}
		if (key.equals("rtcm_wifi")) {
			// rtcm_wifi option is selected
			return;
		}
		if (key.equals("rtcm_file")) {
			// rtcm_file option is selected
			return;
		}
	}

	private void processInitialState() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

	
			// NMEA or RTCM option is selected
			Log.v(TAG, "MEdgdfgsdfgsdfg");
			String val = prefs.getString("nmea-or-rtcm-pref","");
			Log.v(TAG, val);
			if (val.contains("NMEA")) {

				preferenceScreen.addPreference(nmea_setting_options);
				preferenceScreen.removePreference(rtcm_setting_options);

			}
			if (val.contains("RTCM")) {
				preferenceScreen.addPreference(rtcm_setting_options);
				preferenceScreen.removePreference(nmea_setting_options);
			}
		
		if (prefs.getBoolean("nmea_bt", false)) {

		}
		if (prefs.getBoolean("nmea_wifi", false)) {

		}
		if (prefs.getBoolean("nmea_file", false)) {

		}

		if (prefs.getBoolean("rtcm_bt", false)) {

		}
		if (prefs.getBoolean("rtcm_wifi", false)) {

		}
		if (prefs.getBoolean("rtcm_file", false)) {

		}
	}
	
	public static boolean isNMEASelected(Context c){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		return prefs.getString("nmea-or-rtcm-pref","").contains("NMEA");
	}
	public static boolean isSendingNMEAViaBlueTooth(Context c){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		return prefs.getBoolean("nmea_bt", false);
	}
	public static boolean isSendingNMEAViaWifi(Context c){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		return prefs.getBoolean("nmea_wifi", false);
	}
	public static boolean isSavingNMEAToFile(Context c){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		return prefs.getBoolean("nmea_file", false);
	}
	public static boolean isSendingRTCMViaBlueTooth(Context c){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		return prefs.getBoolean("rtcm_bt", false);
	}
	public static boolean isSendingRTCMViaWifi(Context c){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		return prefs.getBoolean("rtcm_wifi", false);
	}
	public static boolean isSavingRTCMToFile(Context c){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		return prefs.getBoolean("rtcm_file", false);
	}
	class BlueToothMessageSender extends Thread {
		private boolean run;
		private Handler handler;
//		private TextView textView;
//		private ScrollView scroller;
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
	class WiFiMessageSender extends Thread {
		private boolean run;
		private Handler handler;
//		private TextView textView;
//		private ScrollView scroller;
		private BluetoothMessageTransferService btService;
		private Context appContext;
		int i = 0;

		public WiFiMessageSender(Handler h,Context c) {
			handler = h;
			//textView = tv;
			//scroller = sc;
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

}

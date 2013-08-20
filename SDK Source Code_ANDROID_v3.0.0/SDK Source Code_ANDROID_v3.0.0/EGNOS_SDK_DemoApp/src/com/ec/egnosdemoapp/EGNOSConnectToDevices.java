package com.ec.egnosdemoapp;

import org.w3c.dom.Text;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ec.R;

public class EGNOSConnectToDevices extends Activity implements
		OnItemSelectedListener {

	private NMEAThread nmeaThread;
	private RTCMThread rtcmThread;
	private Handler handler;
	private TextView outputWindow;

	private void stopNMEAThread() {
		nmeaThread.cancle();

	}

	private void stopRTCMThread() {
		rtcmThread.cancle();

	}

	private void startNMEAThread() {
		nmeaThread = new NMEAThread(handler, outputWindow);
		nmeaThread.start();
	}

	private void startRTCMThread() {
		rtcmThread = new RTCMThread(handler, outputWindow);
		rtcmThread.start();
	}

	/**
	 * onCreate function Called on start of activity, displays the EGNOS
	 * Correction output configirations
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		//outputWindow = (TextView) findViewById(R.id.iodump);
//		nmeaThread = new NMEAThread(handler, outputWindow);
//		rtcmThread = new RTCMThread(handler, outputWindow);
		setContentView(R.layout.egnoscorrectioninputoutput);
		Spinner spinner = (Spinner) findViewById(R.id.nmeaOrRTCMSPINNER);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		String[] items = { "NMEA", "RTCM" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, items);

		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch (arg2) {
		case 0: // NMEA case
			 stopRTCMThread();
			 startNMEAThread();
			// Toast.makeText(this, arg0.getItemAtPosition(arg2).toString(),
			// Toast.LENGTH_LONG).show();
			break;
		case 1: // RTCM
			 stopNMEAThread();
			 startRTCMThread();
			break;
		default:
			break;
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	class NMEAThread extends Thread {
		private boolean run;
		private Handler handler;
		private TextView textView;
		int i = 0;

		public NMEAThread(Handler h, TextView tv) {
			handler = h;
			textView = tv;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			run = true;
			while (run) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						textView.setText("NMEA Thread" + i++);
					}
				});
			}

			return;
		}

		public void cancle() {
			run = false;
		}
	}

	class RTCMThread extends Thread {
		private boolean run;
		private Handler handler;
		private TextView textView;
		int i = 0;

		public RTCMThread(Handler h, TextView tv) {
			handler = h;
			textView = tv;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			run = true;
			while (run) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						textView.setText("RTCM Thread" + i++);
					}
				});
			}

			return;
		}

		public void cancle() {
			run = false;
		}
	}

}

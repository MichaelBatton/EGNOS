/**
 * @file ARView.java
 *
 * Called when the ARView button is clicked in the Skyplot View.
 * Starts the camera view. Gets all the satellite details, that have 
 * already been obtained in the Skyplot View. Updates the satellite 
 * information in the ARSatellitesView class.
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

import com.ec.egnossdk.GlobalState;
import com.ec.egnossdk.LogFiles;
import com.ec.egnossdk.SatelliteData;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Class thats called when the ARView button is clicked in the Skyplot View.
 * Starts the camera view. Gets all the satellite details, that have 
 * already been obtained in the Skyplot View. Updates the satellite 
 * information in the ARSatellitesView class.
 **/
public class ARView extends Activity {
	private static final String TAG_ARVIEW = "arview";
	ARSatellitesLayout arSatLayout;

	CameraView cameraView;
	static LogFiles logFiles;

	/**
	 * onCreate function
	 * 
	 * Called when the activity is first created.
	 * @param savedInstanceState    The bundle of any saved instances.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			GlobalState.setisSkyplot(true);
			logFiles = new LogFiles();
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			arSatLayout = new ARSatellitesLayout(this);

			requestWindowFeature(Window.FEATURE_NO_TITLE);

			WindowManager w = getWindowManager();
			Display d = w.getDefaultDisplay();
			int width = d.getWidth();
			int height = d.getHeight();
			ARSatellitesLayout.screenHeight = height;
			ARSatellitesLayout.screenWidth = width;

			cameraView = new CameraView(this);

			FrameLayout rl = new FrameLayout(getApplicationContext());
			rl.addView(cameraView, width, height);
			rl.addView(arSatLayout, width, height);
			setContentView(rl);

			if (arSatLayout.countARViews() != SatelliteData.satelliteCount)
				addLoadingLayouts();

		} catch (Exception e) {
			Log.e(TAG_ARVIEW, "Error while creating AR view: " + e);
		}
	}

  /**
   * addLoadingLayouts function. 
   * 
   * Adds the information of every satellite obtained from NORAD to the 
   * ARSatellitesView class.
   * The information that is related to the satellites is the Satellite number, 
   * satellite name, type of the satellite, the color of the satellite, the image of the 
   * satellite, the satellite position of that satellite in the last 24 hours.
   **/
	private void addLoadingLayouts() {
		double[] sat_data = new double[2];
		int gpsSatType, egnosSatType;
		double[][] satelliteDetails = GlobalState.getSatelliteDetails();
		double[][] gpsSatelliteTypes = GlobalState.getGPSSatelliteType();
		double[][] egnosSatelliteTypes = GlobalState.getEGNOSSatelliteType();
		double[][] rndSatelliteTypes = GlobalState.getRnDSatelliteType();
		try {
			for (int i = 0; i < satelliteDetails.length; i++) {
				ARSatellitesView arSatView = new ARSatellitesView(
						this.getApplicationContext());
				arSatView.satellitePRN = (int) satelliteDetails[i][0];
				arSatView.satelliteName = getSatelliteName((int) satelliteDetails[i][0]);

				if (satelliteDetails[i][0] >= 120
						& satelliteDetails[i][0] <= 138
						|| satelliteDetails[i][0] == 183) {
					arSatView.satelliteType = (int) satelliteDetails[i][1];
				} else {
					gpsSatType = SkyplotView.searchSatelliteUsed(
							gpsSatelliteTypes, satelliteDetails[i][0]);
					egnosSatType = SkyplotView.searchSatelliteUsed(
							egnosSatelliteTypes, satelliteDetails[i][0]);
					arSatView.rndSatelliteType = SkyplotView
							.searchSatelliteUsed(rndSatelliteTypes,
									satelliteDetails[i][0]);

					arSatView.satelliteType = egnosSatType;

					if (egnosSatType == 0)
						arSatView.satelliteType = gpsSatType;

					if (SkyplotStatus.egnosPos_selected == 0)
						if (egnosSatType == 1)
							arSatView.satelliteType = egnosSatType;
						else
							arSatView.satelliteType = gpsSatType;
				}
				SkyplotView.compute_All(satelliteDetails[i],
						arSatView.satelliteAziEle);
				arSatView.satelliteAzimuth = (float) satelliteDetails[i][146];
				arSatView.satelliteElevation = (float) satelliteDetails[i][147];

				if (arSatView.satellitePRN == 22) {
					Log.d(TAG_ARVIEW,
							"updateSatelliteDetailsafterOne | Satellite Azimuth: "
									+ arSatView.satelliteAzimuth
									+ "Satellite Elevation: "
									+ arSatView.satelliteElevation);
				}

				// if(SkyplotStatus.fromBTReceiver) {
				getSat_Data(gpsSatelliteTypes, (int) satelliteDetails[i][0],
						sat_data);
				arSatView.satelliteDistance = sat_data[0];
				arSatView.satelliteSNR = sat_data[1];
				// }

				System.arraycopy(satelliteDetails[i], 2,
						arSatView.satellitePastValues, 0, 145);
				System.arraycopy(satelliteDetails[i], 148,
						arSatView.satelliteFutureValues, 0, 144);

				Log.d(TAG_ARVIEW, "Satellite Details: "
						+ arSatView.satellitePRN + " : "
						+ arSatView.satelliteAzimuth + " : "
						+ arSatView.satelliteElevation);

				arSatLayout.addARView(arSatView);
			}
		} catch (Exception e) {
			Log.e(TAG_ARVIEW, "Error occurred while getting AR View data: " + e);
			logFiles.logError("AR View | Error occurred while getting AR View data: "
					+ e);
		}
	}

	/**
	 * getSat_Data function
	 * 
	 * Gets the distance and the signal to noise ratio of the satellites.
	 * 
	 * @param satelliteTypes        an array containing the satellite number, 
	 *                              type of the satellite, the distance of the 
	 *                              satellite and the snr.
	 * @param prn                   the satellite number
	 * @param sat_data              contains the distance and snr of a satellite.
	 */
	public static void getSat_Data(double[][] satelliteTypes, int prn,
			double[] sat_data) {
		boolean typeFound = false;
		for (int i = 0; i < satelliteTypes.length; i++) {
			if (prn == satelliteTypes[i][0]) {
				sat_data[0] = satelliteTypes[i][2];// satellite distance
				sat_data[1] = satelliteTypes[i][3];// snr
				typeFound = true;
				break;
			}
		}
		if (!typeFound) { // get satellite distance and snr
			sat_data[0] = 0; // SkyplotStatus.getSatelliteDistance(prn);
			sat_data[1] = 0;
		}
	}

	/**
	 * getSatelliteName function
	 * 
	 * Gets the satellite name of a particular satellite
	 * 
	 * @param prn        the satellite number
	 * @return sat_name  the satellite name
	 */
	private String getSatelliteName(int prn) {
		String sat_name = " ";
		String[][] norad_data = GlobalState.getNORADData();
		for (int i = 0; i < norad_data.length; i++) {
			if (norad_data[i][0] != null) {
				if (prn == Integer.valueOf(norad_data[i][2]))
					sat_name = norad_data[i][0];// Satellite Name
			}
		}
		return sat_name;
	}


	/**
	 * onDestroy function
	 * Perform any final cleanup before an activity is destroyed.
	 */
	public void onDestroy() {
		super.onDestroy();
	}
}
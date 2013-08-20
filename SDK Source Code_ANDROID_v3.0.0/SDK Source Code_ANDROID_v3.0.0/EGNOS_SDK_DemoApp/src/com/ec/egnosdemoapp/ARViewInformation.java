/**
 * @file ARViewInformation.java
 *
 * Called when the AR View info button on the augmented reality screen 
 * is clicked. Displays information of about all the items that 
 * are shown on the augmented reality view.
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

import com.ec.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Class thats called when the AR View info button on the augmented reality screen 
 * is clicked. Displays information of about all the items that 
 * are shown on the augmented reality view.
 **/
public class ARViewInformation extends Activity {

	/**
	 * onCreate function 
	 * 
	 * Called when the activity is first created.
	 * @param savedInstanceState   The bundle of any saved instances.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arinfoview);

		TextView startTextView = (TextView) this
				.findViewById(R.id.startTextView);
		startTextView
				.setText("The \"Augmented Reality\" feature provides a live-video stream of the sky"
						+ " in the direction your device is pointed at. \n\n"
						+ " This view shows the current position of the satellites on the sky."
						+ " By moving the device the different satellites"
						+ " ranging from GPS to SBAS satellites with different colours can be seen. Along with this the path the "
						+ " satellites has taken(same color as satellite) or will take in the future (path shown in white)"
						+ " can be seen. \n");

		TextView gpsTextView = (TextView) this.findViewById(R.id.gpstextView);
		gpsTextView
				.setText("- indicates this satellite was used to get a GPS position");

		TextView egnosTextView = (TextView) this
				.findViewById(R.id.egnostextView);
		egnosTextView
				.setText("- indicates this satellite was used to get an EGNOS position");

		TextView sbasTextView = (TextView) this
				.findViewById(R.id.sbasSattextView);
		sbasTextView.setText("- indicates this is an SBAS satellite");

		TextView notusedTextView = (TextView) this
				.findViewById(R.id.notUsedSattextView);
		notusedTextView.setText("- indicates this satellite was not used");

		TextView rndTextView1 = (TextView) this.findViewById(R.id.rndtextView1);
		rndTextView1.setTextColor(Color.MAGENTA);
		rndTextView1.setText("*");

		TextView rndTextView2 = (TextView) this.findViewById(R.id.rndtextView2);
		rndTextView2
				.setText("- indicates this satellite was used to get a R&D position ");

		TextView midTextView = (TextView) this.findViewById(R.id.midtextView);
		midTextView
				.setText("Further details of the satellite can be obtained by clicking on the satellite,"
						+ " a popup appears providing details like prn, azimuth, elevation, distance and snr.\n\n"
						+ "The direction the device is pointing is displayed on the right most top corner of the "
						+ " view.For e.g 20°N.\n\n" + "Others");
		TextView redArrowTextView = (TextView) this
				.findViewById(R.id.redArrowtextView);
		redArrowTextView
				.setText("- guide to view the EGNOS satellites 120, 124 and 126. Beside the arrow the satellite number"
						+ " and the distance remaining to view the satellite");

		TextView horizonTextView = (TextView) this
				.findViewById(R.id.horizontextView);
		horizonTextView
				.setText("- shows the horizon, with a vertical line in the center "
						+ "indicating the reference of the direction of the device");
	}

}

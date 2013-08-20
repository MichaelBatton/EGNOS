/**
 * @file RnDPositionType.java
 *
 * Displays the popup screen for the R&D Position Type.
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
 * http://ec.europa.eu/idabc/eupl.html
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package com.ec.egnosdemoapp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.ec.R;
import com.ec.egnossdk.GlobalState;

/**
 * Class the displays a popup screen for the R&D Position Type.
 */
public class RnDPositionType{

	Context context;

	SharedPreferences rndSharedPref; 
	private SharedPreferences.Editor rndPrefEditor;
	public static final int AUTOMATIC_RADIO_BUTTON = R.id.type8aradioButton;
	public static final int FORCED_RADIO_BUTTON = R.id.type8bradioButton;
	public static final int OFF_RADIO_BUTTON = R.id.type8cradioButton;
	public static final String KEY_RnD_SHARED_PREF = "rndSharedPrefKey";
	public static final String KEY_RnDTYPE_ONE = "type1Key";
	public static final String KEY_RnDTYPE_TWO = "type2Key";
	public static final String KEY_RnDTYPE_THREE = "type3Key";
	public static final String KEY_RnDTYPE_FOUR = "type4Key";
	public static final String KEY_RnDTYPE_FIVE = "type5Key";
	public static final String KEY_RnDTYPE_SIX = "type6Key";
	public static final String KEY_RnDTYPE_SEVEN = "type7Key";
	public static final String KEY_RnDTYPE_EIGHT = "type8Key";
	private static final int MODE_WORLD_READABLE = 1;


  /**
   *  RnDPositionType Constructor
   * 
   *  A constructor for popup Screen that sets the
   *  Persistent Store for the R&D position type.
   *  @param  context    the context of the application
   *  
   */
	public RnDPositionType(Context context) {
		this.context = context;
		rndSharedPref = context.getSharedPreferences(KEY_RnD_SHARED_PREF,
				MODE_WORLD_READABLE);
		rndPrefEditor = rndSharedPref.edit();
	}

	/**
   * rndPositionType function 
   * 
   * Draws a layout that displays the different R&D position types
   */
	public void rndPositionType() {
		int radioButtonID = 0;
		int sbasRanging_Mode = 0;

		// ToDo change colour of the Alert Dialog
		LayoutInflater factory = LayoutInflater.from(context);
		View rndPositionView = factory.inflate(R.layout.rndpositiontype, null);

		// Increased Satellite constellation
		final CheckBox rndType1 = (CheckBox) rndPositionView
				.findViewById(R.id.type1checkBox);
		// Best satellite constellation
		final CheckBox rndType2 = (CheckBox) rndPositionView
				.findViewById(R.id.type2checkBox);
		// 2D positioning
		final CheckBox rndType3 = (CheckBox) rndPositionView
				.findViewById(R.id.type3checkBox);
		// Positioning with RAIM
	  final CheckBox rndType4 = (CheckBox) rndPositionView
		    .findViewById(R.id.type4checkBox);
		// Fast correction with no RRC
		final CheckBox rndType5 = (CheckBox) rndPositionView
				.findViewById(R.id.type5checkBox);
		// Best weight matrix
		final CheckBox rndType6 = (CheckBox) rndPositionView
				.findViewById(R.id.type6checkBox);
//		// INS enhanced position
//		 final CheckBox rndType7 = (CheckBox) rndPositionView
//		    .findViewById(R.id.type7checkBox);
		// SBAS Ranging function (MT9 and MT17)
		final RadioGroup rndType8 = (RadioGroup) rndPositionView
				.findViewById(R.id.type8radioGroup);

		rndType1.setChecked(rndSharedPref.getBoolean(KEY_RnDTYPE_ONE, false));
		rndType2.setChecked(rndSharedPref.getBoolean(KEY_RnDTYPE_TWO, false));
		rndType3.setChecked(rndSharedPref.getBoolean(KEY_RnDTYPE_THREE, false));
	  rndType4.setChecked(rndSharedPref.getBoolean(KEY_RnDTYPE_FOUR, false));
		rndType5.setChecked(rndSharedPref.getBoolean(KEY_RnDTYPE_FIVE, false));
		rndType6.setChecked(rndSharedPref.getBoolean(KEY_RnDTYPE_SIX, false));
	 // rndType7.setChecked(rndSharedPref.getBoolean(KEY_RnDTYPE_SEVEN, false));

		sbasRanging_Mode = rndSharedPref.getInt(KEY_RnDTYPE_EIGHT, 0);
		if (sbasRanging_Mode == 0)
			radioButtonID = AUTOMATIC_RADIO_BUTTON;// Automatic Mode
		else if (sbasRanging_Mode == 1)
			radioButtonID = FORCED_RADIO_BUTTON;// Forced Mode
		else if (sbasRanging_Mode == 2)
			radioButtonID = OFF_RADIO_BUTTON;// OFF Mode

		rndType8.check(radioButtonID);

		rndType8.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == FORCED_RADIO_BUTTON)
					Toast
					.makeText(
							context,
							"Turning ON Forced mode can cause an error in " +
									"the position accuracy since EGNOS currently does not offer " +
									"a ranging capability",
									Toast.LENGTH_LONG).show();        
			}
		});

		rndType2
		.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked)
					Toast
					.makeText(
							context,
							"This R&D position type is intended for situations with a "
									+ "bad satellite constellation; In situations with a "
									+ "good constellation it can decrease the position accuracy",
									Toast.LENGTH_LONG).show();

			}
		});

		Builder rndDisplayBox = new AlertDialog.Builder(context);
		rndDisplayBox.setView(rndPositionView);
		rndDisplayBox.setTitle(R.string.rndPositionTypeTitle);
		rndDisplayBox.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int sbasRangingMode = 0;
				int[] rndPositionTypes = new int[8];

				// Increased Satellite constellation
				rndPrefEditor.putBoolean(KEY_RnDTYPE_ONE, rndType1.isChecked());
				rndPositionTypes[0] = rndType1.isChecked() == true ? 1 : 0;

				// Best satellite constellation
				rndPrefEditor.putBoolean(KEY_RnDTYPE_TWO, rndType2.isChecked());
				rndPositionTypes[1] = rndType2.isChecked() == true?1:0;

				// 2D positioning
				rndPrefEditor.putBoolean(KEY_RnDTYPE_THREE, rndType3.isChecked());
				rndPositionTypes[2] = rndType3.isChecked() == true ? 1 : 0;

				// Positioning with RAIM
			  rndPrefEditor.putBoolean(KEY_RnDTYPE_FOUR, rndType4.isChecked());
				rndPositionTypes[3] = rndType4.isChecked() == true?1:0;

				// Fast correction with no RRC
				rndPrefEditor.putBoolean(KEY_RnDTYPE_FIVE, rndType5.isChecked());
				rndPositionTypes[4] = rndType5.isChecked() == true ? 1 : 0;

				// Best weight matrix
				rndPrefEditor.putBoolean(KEY_RnDTYPE_SIX, rndType6.isChecked());
				rndPositionTypes[5] = rndType6.isChecked() == true?1:0;

				// INS enhanced position
			//	rndPrefEditor.putBoolean(KEY_RnDTYPE_SEVEN, rndType7.isChecked());
				rndPositionTypes[6] = 0;//rndType7.isChecked() == true?1:0;

				// SBAS Ranging function (MT9 and MT17)
				int id = rndType8.getCheckedRadioButtonId();
				if (id == AUTOMATIC_RADIO_BUTTON)
					sbasRangingMode = 0;// Automatic Mode
				else if (id == FORCED_RADIO_BUTTON)
					sbasRangingMode = 1;// Forced Mode
				else if (id == OFF_RADIO_BUTTON)
					sbasRangingMode = 2;// OFF Mode

				rndPrefEditor.putInt(KEY_RnDTYPE_EIGHT, sbasRangingMode);
				rndPositionTypes[7] = sbasRangingMode;// rndType8.getCheckedRadioButtonId();

				rndPrefEditor.commit();

				GlobalState.setRndPositionType(rndPositionTypes);
			}
		});
		rndDisplayBox.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// close the alert dialog.

			}
		});
		rndDisplayBox.show();
	}
}


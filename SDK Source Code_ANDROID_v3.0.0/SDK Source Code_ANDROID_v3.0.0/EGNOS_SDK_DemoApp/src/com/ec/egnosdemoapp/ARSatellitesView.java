/**
 * @file ARSatellitesView.java
 *
 * Draws all satellites with a valid xy position on the 
 * augmented reality view.Displays satellite number, 
 * as well as the historic and future path of the satellite.
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

import com.ec.R;
import com.ec.egnossdk.GlobalState;
import com.ec.egnossdk.uBlox;

/**
 * Class that draws all satellites with a valid xy position on the 
 * augmented reality view.Displays satellite number, 
 * as well as the historic and future path of the satellite.
 **/
public class ARSatellitesView extends View {
	private static final String TAG_ARSATELLITEVIEW = "arsatView";

	public volatile float satelliteAzimuth;
	public volatile float satelliteElevation;
	public String satelliteName;
	public int satellitePRN;
	public int satelliteType;
	public int rndSatelliteType;
	public double satelliteSNR;
	public double satelliteDistance;// in km
	public Bitmap gpsSatelliteBitmap;
	public int satPosX;
	public int satPosY;
	public boolean isSatelliteDrawn = false;
	public boolean isSatellitePopupOpen = false;
	public boolean isEGNOSArrowDrawn = false;

	public double[] satellitePastValues = new double[146];
	public double[] satelliteFutureValues = new double[144];

	public double[] satelliteAziEle = new double[8642];

	private float bitmapAdjustment;

	public double satPastX1;
	public double satPastY1;

	public double satPastX2;
	public double satPastY2;

	public double satFutureX1;
	public double satFutureY1;

	public double satFutureX2;
	public double satFutureY2;

	public ARSatellitesView(Context context) {
		super(context);
		gpsSatelliteBitmap = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.ic_gps_satellite)).getBitmap();

		bitmapAdjustment = gpsSatelliteBitmap.getHeight() / 2;
	}

	/**
	 * draw function
	 * 
	 * Draws the satellite on the augmented reality, along with the 
	 * satellite name, the satellites are drawn in different colors 
	 * based on the type of the satellite. 
	 * 
	 * @param canvas	The Canvas to be viewed
	 */
	public void draw(Canvas canvas) {
		boolean satToDisplayorNot = false;
		Bitmap bitmapToDisplay = null;
		int prnTextColor = 0;
		int gpsSatType, egnosSatType;

		satToDisplayorNot = SkyplotView.displaySatelliteorNot(satelliteType);
		if (satToDisplayorNot) {

			Paint satellitePaint = new Paint();

			satellitePaint.setTextSize(20);

			if (satellitePRN != 0) {
				if (satelliteType == 5 || satelliteType == 6
						|| satelliteType == 7 || satelliteType == 8
						|| satelliteType == 9) {// SBAS satellites
					satelliteType = 5;
					prnTextColor = SkyplotView.getSatelliteTextColor(5);
					if (uBlox.svId == satellitePRN) {
						bitmapToDisplay = SkyplotView.getBitmapToDisplay(5);
					} else if (uBlox.svId != satellitePRN || uBlox.svId == 0)
						bitmapToDisplay = SkyplotView.getBitmapToDisplay(0);
				} else {
					gpsSatType = SkyplotView.searchSatelliteUsed(
							GlobalState.getGPSSatelliteType(), satellitePRN);
					egnosSatType = SkyplotView.searchSatelliteUsed(
							GlobalState.getEGNOSSatelliteType(), satellitePRN);
					satelliteType = egnosSatType;

					if (egnosSatType == 0)
						satelliteType = gpsSatType;

					if (SkyplotStatus.egnosPos_selected == 0)
						if (egnosSatType == 1)
							satelliteType = egnosSatType;
						else
							satelliteType = gpsSatType;
					prnTextColor = SkyplotView
							.getSatelliteTextColor(satelliteType);
					bitmapToDisplay = SkyplotView
							.getBitmapToDisplay(satelliteType);
				}

				if (satelliteType != 5)
					if (SkyplotStatus.skyplotStatusSharedPreferences
							.getBoolean(
									SkyplotStatus.KEY_SATELLITEPATH_CHECKED,
									true) == true)
						drawSatellitePath(canvas, prnTextColor);

				satellitePaint.setColor(prnTextColor);
				canvas.drawText(String.valueOf(satellitePRN), getLeft() - 20,
						getTop() - 20, satellitePaint);

				canvas.drawBitmap(bitmapToDisplay, satPosX - bitmapAdjustment,
						satPosY - bitmapAdjustment, satellitePaint);

				if (rndSatelliteType == 3
						&& SkyplotStatus.skyplotStatusSharedPreferences.getInt(
								SkyplotStatus.KEY_RnDPOS_SELECTED, 1) == 1) {
					satellitePaint.setColor(Color.MAGENTA);
					canvas.drawText("*", satPosX + 20, satPosY + 20,
							satellitePaint);
				}

				isSatelliteDrawn = false;
				if (satPosX > 0 && satPosY > 0)
					if (satPosX < ARSatellitesLayout.screenWidth
							&& satPosY < ARSatellitesLayout.screenHeight)
						isSatelliteDrawn = true;
			}
		}
	}

	/**
	 * drawSatellitePath function
	 * 
	 * Draws the historic path and future path of the satellite.
	 * The color of the historic path is based on the type of the satellite, 
	 * while the colour of the future path is white.
	 * @param canvas         The canvas to be viewed
	 * @param prnTextColor   The color of the historic path based on the 
	 *                       type of the satellite
	 */
	private void drawSatellitePath(Canvas canvas, int prnTextColor) {
		float[] pastXY = new float[satellitePastValues.length];

		Paint historicalpathPaint = new Paint();
		historicalpathPaint.setAntiAlias(true);
		historicalpathPaint.setStyle(Style.STROKE);
		historicalpathPaint.setStrokeWidth(2);
		historicalpathPaint.setColor(prnTextColor);

		Path historicalPath = new Path();

		ARSatellitesLayout.computeXY(satellitePastValues, pastXY);

		historicalPath.moveTo(satPosX, satPosY);

		historicalPath.cubicTo(satPosX, satPosY, pastXY[142], pastXY[143],
				pastXY[140], pastXY[141]);
		canvas.drawPath(historicalPath, historicalpathPaint);

		Paint futurepathPaint = new Paint();
		futurepathPaint.setAntiAlias(true);
		futurepathPaint.setStyle(Style.STROKE);
		futurepathPaint.setStrokeWidth(2);

		Path futurePath = new Path();

		float[] futureXY = new float[satelliteFutureValues.length];
		ARSatellitesLayout.computeXY(satelliteFutureValues, futureXY);

		futurePath.moveTo(satPosX, satPosY);

		futurePath.cubicTo(satPosX, satPosY, futureXY[0], futureXY[1],
				futureXY[2], futureXY[3]);

		futurepathPaint.setColor(Color.WHITE);

		canvas.drawPath(futurePath, futurepathPaint);
	}
}

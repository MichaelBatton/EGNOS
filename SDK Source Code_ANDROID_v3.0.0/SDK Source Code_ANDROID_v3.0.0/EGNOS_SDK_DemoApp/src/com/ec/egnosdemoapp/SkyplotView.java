/**
 * @file SkyplotView.java
 *
 * Displays the Normal Skyplot View.
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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.ec.R;
import com.ec.egnossdk.GlobalState;
import com.ec.egnossdk.LogFiles;
import com.ec.egnossdk.SatelliteData;
import com.ec.egnossdk.uBlox;

/**
 * Class that displays the Normal Skyplot View.
 **/
public class SkyplotView extends View {

	private static final String TAG_SYKPLOT_VIEW = "skyplot";
	private Paint mGridPaint;
	private Paint mDirectionPaint;
	private Paint satTextPaint;
	private Paint mBackground;

	private static Bitmap gpsPosBitmap;
	private static Bitmap egnosPosBitmap;
	private static Bitmap sbasSatelliteBitmap;
	private static Bitmap notUsedSatelliteBitmap;
	private float bitmapAdjustment;
	double x, y;

	static double[][] gpsSatelliteTypes;
	static double[][] egnosSatelliteTypes;
	static double[][] rndSatelliteTypes;
	static Context context;

	private double[][] satelliteData = null;

	private double[] satelliteAziEle = new double[2901];

	private int time;

	private static SharedPreferences satelliteSharedPreferences;
	public int countTime = 0;

	int countGPSPos = 0;
	int countEGNOSPos = 0;
	int countRnDPos = 0;
	int countSBASPos = 0;
	int countNotUsedPos = 0;

	int count = 0;

	static LogFiles logFiles;

	public SkyplotView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public SkyplotView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		logFiles = new LogFiles();

		mGridPaint = new Paint();
		mGridPaint.setColor(Color.rgb(16, 136, 216));
		mGridPaint.setAntiAlias(true);
		mGridPaint.setStyle(Style.STROKE);
		mGridPaint.setStrokeWidth(1.5f);

		mBackground = new Paint();
		mBackground.setColor(Color.BLACK);

		satTextPaint = new Paint();
		satTextPaint.setTextSize(17.0f);
		satTextPaint.setTextAlign(Align.CENTER);

		mDirectionPaint = new Paint();
		mDirectionPaint.setColor(Color.WHITE);
		mDirectionPaint.setTextSize(17.0f);

		gpsPosBitmap = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.ic_gps_satellite)).getBitmap();
		egnosPosBitmap = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.ic_egnos_sat)).getBitmap();
		sbasSatelliteBitmap = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.ic_sbas_satellite)).getBitmap();
		notUsedSatelliteBitmap = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.ic_notused_satellite)).getBitmap();
		bitmapAdjustment = sbasSatelliteBitmap.getHeight() / 2;
	}

	public SkyplotView(Context context) {
		super(context);
	}

	/**
	 * setSatelliteData function
	 * 
	 * Sets the satellite data that contains satellite number, type of satellite,
	 * satellite position over a period of 24 hours for every 10 minutes. 
	 * Sets the value of the time slider.
	 * 
	 * @param	satelliteData          the satellite data         
	 * @param	timeSliderProgress     the value of the time slider
	 * @param	countTime              the count
	 */
	public void setSatelliteData(double[][] satelliteData,
			int timeSliderProgress, int countTime) {
		this.satelliteData = satelliteData;
		this.countTime = countTime;

		this.countTime = countTime;
		time = timeSliderProgress * 2;
	}

	/**
	 * onDraw function
	 * 
	 * Draws all the satellites with an elevation and azimuth above 0°.
   * The satellites are displayed in different colors based on the type of 
   * the satellite.
	 * @param	canvas	The canvas to be viewed
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		countGPSPos = 0;
		countEGNOSPos = 0;
		countRnDPos = 0;
		countSBASPos = 0;
		countNotUsedPos = 0;

		int gpsSatType = 0;
		int egnosSatType = 0;
		int rndSatType = 0;

		double[] satellite_data = new double[290];

		float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		float[] mXY = new float[2];
		int satType = 0;
		double satPRN = 0, satAzimuth = 0, satElevation = 0;
		boolean satToDisplayorNot = false;
		Bitmap bitmapToDisplay = null;
		int prnTextColor = 0;
		float[] xxyy = new float[2];
		double satAzx1, satEley1, satAzx2, satEley2;

		float centerY = getHeight() / 2;
		float centerX = getWidth() / 2;
		int radius = (int) (getHeight() / 2) - 8;

		final Paint gridPaint = mGridPaint;
		final Paint textPaint = satTextPaint;
		final Paint dirPaint = mDirectionPaint;

		canvas.drawPaint(mBackground);
		canvas.drawCircle(centerX, centerY, radius, gridPaint);
		canvas.drawCircle(centerX, centerY, radius * 3 / 4, gridPaint);
		canvas.drawCircle(centerX, centerY, radius >> 1, gridPaint);
		canvas.drawCircle(centerX, centerY,  (radius >> 2)-8, gridPaint);

		canvas.drawLine(centerX, centerY, centerX, centerY - radius + 2, gridPaint);
		canvas.drawText("N", centerX - 4, centerY - radius + 5, dirPaint);

		canvas.drawLine(centerX, centerY, centerX, centerY + radius - 2, gridPaint);
		canvas.drawText("S", centerX - 4, centerY + radius, dirPaint);

		canvas.drawLine(centerX, centerY, centerX - radius + 2, centerY, gridPaint);
		canvas.drawText("W", centerX - radius -1, centerY + 5, dirPaint);

		canvas.drawLine(centerX, centerY, centerX + radius - 2, centerY, gridPaint);
		canvas.drawText("E", centerX + radius - 8, centerY + 5, dirPaint);

		canvas.drawText("25°", centerX - 9, centerY + radius * 3 / 4 + 5, dirPaint);
		canvas.drawText("25°", centerX - 9, centerY - radius * 3 / 4 + 5, dirPaint);
		canvas.drawText("45°", centerX - 9, centerY + (radius >> 1) + 5, dirPaint);
		canvas.drawText("45°", centerX - 9, centerY - (radius >> 1) + 5, dirPaint);
		canvas.drawText("70°", centerX - 9, centerY +  (radius >> 2)-8 + 5, dirPaint);
		canvas.drawText("70°", centerX - 9, centerY -  (radius >> 2)-8+ 20, dirPaint);

		gpsSatelliteTypes = GlobalState.getGPSSatelliteType();
		egnosSatelliteTypes = GlobalState.getEGNOSSatelliteType();
		rndSatelliteTypes = GlobalState.getRnDSatelliteType();    

		try {
			for (int i = 0; i < SatelliteData.satelliteCount; i++) {
				if (satelliteData != null) {
					System.arraycopy(satelliteData[i], 2, satellite_data, 0, 290);

					satPRN = satelliteData[i][0];

					if (satelliteData[i][0] >= 120 & satelliteData[i][0] <= 138
							|| satelliteData[i][0] == 183) {
						satType = (int) satelliteData[i][1];
					} else {
						gpsSatType = searchSatelliteUsed(gpsSatelliteTypes,
								satelliteData[i][0]);
						egnosSatType = searchSatelliteUsed(egnosSatelliteTypes,
								satelliteData[i][0]);
						rndSatType = searchSatelliteUsed(rndSatelliteTypes,
								satelliteData[i][0]);
						satType = egnosSatType;

						if (egnosSatType == 0)
							satType = gpsSatType;

						if (SkyplotStatus.egnosPos_selected == 0)
							if (egnosSatType == 1)
								satType = egnosSatType;
							else
								satType = gpsSatType;

						Log.d(TAG_SYKPLOT_VIEW, " Sat PRN: " + satPRN + "  satType: "
								+ satType);
						Log.d(TAG_SYKPLOT_VIEW, " GPS SatType: " + gpsSatType
								+ "  EGNOSSatType: " + egnosSatType);
					}

					satToDisplayorNot = displaySatelliteorNot(satType);
					if (satToDisplayorNot) {

						if (satType < 5) {

							computeAll(satellite_data, satelliteAziEle);
							satAzimuth = satelliteAziEle[time + countTime];
							satElevation = satelliteAziEle[time + 1 + countTime];


						} else {
							satAzimuth = satellite_data[144];
							satElevation = satellite_data[145];
						}

						Log.d(TAG_SYKPLOT_VIEW, "countTime: " + countTime);
						Log.d(TAG_SYKPLOT_VIEW, "Satellite PRN: " + satPRN
								+ " Satellite Azimuth: " + satAzimuth + " Satellite Elevation: "
								+ satElevation);

						if ((int)satElevation <= 0 || (int)satAzimuth <= 0 || satPRN <= 0) {
							continue;
						}

						computeXY(satAzimuth, satElevation, radius, centerX, centerY, mXY);
						x = mXY[0];
						y = mXY[1];

						if (satType == 5 || satType == 6 || satType == 7 || satType == 8
								|| satType == 9) {// SBAS satellites
							satType = 5;
							prnTextColor = getSatelliteTextColor(satType);
							if (uBlox.svId == satelliteData[i][0])
								bitmapToDisplay = getBitmapToDisplay(5);
							else if (uBlox.svId != satelliteData[i][0] || uBlox.svId == 0)
								bitmapToDisplay = getBitmapToDisplay(0);

							getSatelliteCount(satType);
						} else {
							prnTextColor = getSatelliteTextColor(satType);
							bitmapToDisplay = getBitmapToDisplay(satType);           

							if(gpsSatType == 0 && egnosSatType == 0)
								getSatelliteCount(satType);
							else {
								if(gpsSatType != 0)
									getSatelliteCount(gpsSatType);
								if(egnosSatType != 0)
									getSatelliteCount(egnosSatType);
							}
						}         

						Log.d(TAG_SYKPLOT_VIEW, "satType: " + satType);
						Log.d(TAG_SYKPLOT_VIEW, "gpsSatType: " + gpsSatType);
						Log.d(TAG_SYKPLOT_VIEW, "egnosSatType: " + egnosSatType);
						Log.d(TAG_SYKPLOT_VIEW, "rndSatType: " + rndSatType);

						if (satType != 5) {
							if (SkyplotStatus.skyplotStatusSharedPreferences.getBoolean(
									SkyplotStatus.KEY_SATELLITEPATH_CHECKED, true) == true) {
								int t = convertTimeSlider(time + countTime);

								Paint historicPaint = new Paint();
								historicPaint.setAntiAlias(true);
								historicPaint.setStyle(Style.STROKE);
								historicPaint.setStrokeWidth(1);
								historicPaint.setColor(prnTextColor);

								Path historicalpath = new Path();
								historicalpath.moveTo((float)x+ bitmapAdjustment , (float)y+ bitmapAdjustment);

								for (int p = t-40; p >=0; p = p - 20) {

									satAzx1 = satelliteAziEle[p];
									satEley1 = satelliteAziEle[p + 1];

									computeXY(satAzx1, satEley1, radius, centerX, centerY, xxyy);
									x1 = xxyy[0];
									y1 = xxyy[1];

									satAzx2 = satelliteAziEle[(p + 20)];
									satEley2 = satelliteAziEle[p + 20 + 1];

									computeXY(satAzx2, satEley2, radius, centerX, centerY, xxyy);
									x2 = xxyy[0];
									y2 = xxyy[1];

									historicalpath.quadTo(x1 + bitmapAdjustment, y1
											+ bitmapAdjustment, x2 + bitmapAdjustment, y2
											+ bitmapAdjustment);
								}
								canvas.drawPath(historicalpath, historicPaint);
							}
						}
						canvas.drawBitmap(bitmapToDisplay, (float) x, (float) y, gridPaint);
						textPaint.setColor(prnTextColor);
						if(satType != 5) 
							canvas.drawText(String.valueOf((int) satPRN), (float) x + 15,
									(float) y - 2, textPaint);
						else 
							canvas.drawText(String.valueOf((int) satPRN), (float) x +bitmapToDisplay.getWidth()+15,
									(float) y+bitmapToDisplay.getWidth()-15, textPaint);

						if (rndSatType == 3 && SkyplotStatus.skyplotStatusSharedPreferences.getInt(
								SkyplotStatus.KEY_RnDPOS_SELECTED, 1) == 1) {
							getSatelliteCount(rndSatType);
							textPaint.setColor(Color.MAGENTA);
							canvas.drawText("*", (float) x + 30, (float) y + 2, textPaint);
						}
					}
				}
			}
		}catch(Exception e) {
			Log.e(TAG_SYKPLOT_VIEW, "Error occurred while drawing: "+e);
			logFiles.logError("Skyplot View | Error occurred while drawing Sykplot View: "+e);
		}
		setSatelliteCount();    
	}


	private void interpolateXY(float[] XY, double[] satXY) {
		float[] point1 = new float[2];
		float[] point2 = new float[2];
		float[] pointXY = new float[2];
		int counts = 0;

		for (int j = 0; j < 288; j = j + 2) {
			point1[0] = (float) XY[j];
			point1[1] = (float) XY[j + 1];

			point2[0] = (float) XY[j + 2];
			point2[1] = (float) XY[j + 3];

			satXY[counts + 0] = XY[j];
			satXY[counts + 1] = XY[j + 1];

			satXY[counts + 20] = XY[j + 2];
			satXY[counts + 20 + 1] = XY[j + 3];
			int countk = counts;
			for (int k = 1; k < 10; k++) {
				interpolate(point1, point2, (float) k / 10, pointXY);
				satXY[countk + 1 + k] = (double) pointXY[0];
				satXY[countk + 1 + k + 1] = (double) pointXY[1];
				countk++;
			}
			counts = counts + 20;
		}
	}


	private void computeXYALL(double[] satellite_data, int radius, float centerX,
			float centerY, float[] XY) {

		double azimuth = 0, elevation = 0;
		for (int sat = 0; sat < 290; sat = sat + 2) {
			float x, y;
			azimuth = satellite_data[sat];
			elevation = satellite_data[sat + 1];

			double theta = -(azimuth - 90);
			double rad = theta * Math.PI / 180.0;

			x = (float) Math.cos(rad);
			y = -(float) Math.sin(rad);

			elevation = 90 - elevation;
			double a = elevation * (radius / 90.0);

			XY[sat] = (int) Math.round(centerX + (x * a) - bitmapAdjustment);
			XY[sat + 1] = (int) Math.round(centerY + (y * a) - bitmapAdjustment);
		}
	}


	private int convertTimeSlider(int time) {
		int progressTime = time / 2;
		int modTime = progressTime % 10;

		if (modTime != 0)
			modTime = 10 - modTime;
		progressTime = progressTime + modTime;
		return progressTime * 2;
	}

	/**
	 * compute_All function
	 * 
	 * Computes the satellite position for a satellite for 24 hours for every 1 minute.
	 * 
	 * @param	satellite_data     an array that contains the satellite position 
	 *                           for 24 hours for every 10 mins
	 * @param	satelliteAziEle    an array that contains the satellite position 
   *                           for 24 hours for every 1 min
	 */
	public static void compute_All(double[] satellite_data,
			double[] satelliteAziEle) {
		float[] point1 = new float[2];
		float[] point2 = new float[2];
		float[] pointXY = new float[2];
		int counts = 0;
		double kk;
		int countk;

		for (int j = 0; j < 288; j = j + 2) {
			point1[0] = (float) satellite_data[j];
			point1[1] = (float) satellite_data[j + 1];

			point2[0] = (float) satellite_data[j + 2];
			point2[1] = (float) satellite_data[j + 3];

			satelliteAziEle[counts + 0] = satellite_data[j];
			satelliteAziEle[counts + 1] = satellite_data[j + 1];

			satelliteAziEle[counts + 60] = satellite_data[j + 2];
			satelliteAziEle[counts + 60 + 1] = satellite_data[j + 3];
			countk = counts;
			for (int k = 20; k < 600; k = k + 20) {
				kk = (float) k / 60;
				interpolate(point1, point2, (float) kk / 10, pointXY);
				satelliteAziEle[countk + 2] = (double) pointXY[0];
				satelliteAziEle[countk + 2 + 1] = (double) pointXY[1];
				countk = countk + 2;
			}
			counts = counts + 60;
		}
		point1[0] = (float) satellite_data[288];
		point1[1] = (float) satellite_data[288 + 1];

		point2[0] = (float) satellite_data[0];
		point2[1] = (float) satellite_data[1];
		countk = 2880;
		for (int k = 1; k < 10; k++) {
			interpolate(point1, point2, (float) k / 10, pointXY);
			satelliteAziEle[countk + 1 + k] = (double) pointXY[0];
			satelliteAziEle[countk + 1 + k + 1] = (double) pointXY[1];
			countk++;
		}

	}

  /**
   * computeAll function
   * 
   * Computes the satellite position for a satellite for 24 hours for every 1 minute.
   * 
   * @param satellite_data     an array that contains the satellite position 
   *                           for 24 hours for every 10 mins
   * @param satelliteAziEle    an array that contains the satellite position 
   *                           for 24 hours for every 1 min
   */
	public static void computeAll(double[] satellite_data,
			double[] satelliteAziEle) {
		float[] point1 = new float[2];
		float[] point2 = new float[2];
		float[] pointXY = new float[2];
		int counts = 0;
		int countk;

		for (int j = 0; j < 288; j = j + 2) {
			point1[0] = (float) satellite_data[j];
			point1[1] = (float) satellite_data[j + 1];

			point2[0] = (float) satellite_data[j + 2];
			point2[1] = (float) satellite_data[j + 3];

			satelliteAziEle[counts + 0] = satellite_data[j];
			satelliteAziEle[counts + 1] = satellite_data[j + 1];

			satelliteAziEle[counts + 20] = satellite_data[j + 2];
			satelliteAziEle[counts + 20 + 1] = satellite_data[j + 3];
			countk = counts;
			for (int k = 1; k < 10; k++) {
				interpolate(point1, point2, (float) k / 10, pointXY);
				satelliteAziEle[countk + 1 + k] = (double) pointXY[0];
				satelliteAziEle[countk + 1 + k + 1] = (double) pointXY[1];
				countk++;
			}
			counts = counts + 20;
		}

		point1[0] = (float) satellite_data[288];
		point1[1] = (float) satellite_data[288 + 1];

		point2[0] = (float) satellite_data[0];
		point2[1] = (float) satellite_data[1];
		countk = 2880;
		for (int k = 1; k < 10; k++) {
			interpolate(point1, point2, (float) k / 10, pointXY);
			satelliteAziEle[countk + 1 + k] = (double) pointXY[0];
			satelliteAziEle[countk + 1 + k + 1] = (double) pointXY[1];
			countk++;
		}

	}

	/**
	 * setSatelliteCount function
	 * 
	 * Sets the count of all the satellites used to get a GPS Position,
	 * EGNOS Position, R&D Position, SBAS satellite and Not Used satellites 
	 * on the Skyplot View
	 */
	private void setSatelliteCount() {
		if (SkyplotStatus.fromBTReceiver) {
			if (SkyplotStatus.gpsPos_selected == 0)
				SkyplotStatus.gpsPos_count.setText("GPS Pos: " + 0);
			else
				SkyplotStatus.gpsPos_count.setText("GPS Pos: "
						+ countGPSPos);
			SkyplotStatus.egnosPos_count.setText("EGNOS Pos: " + countEGNOSPos);
			SkyplotStatus.rndPos_count.setText("*R&D Pos: " + countRnDPos);
			SkyplotStatus.sbasSat_count.setText("SBAS: " + countSBASPos);
			SkyplotStatus.notused_count.setText("Not Used: " + countNotUsedPos);
   	}
	}

	/**
	 * interpolate function
	 * 
	 * Interpolates 10 satellite position between every 10 mins.
	 * 
	 * @param	point0	        The satellite position at the 0th minute
	 * @param	point1          The satellite position at the 10th minute
	 * @param	timeRatio       The time ratio for every minute
	 * @param	point           The satellite position for every minute.
	 */
	private static void interpolate(float[] point0, float[] point1,
			double timeRatio, float[] point) {

		int factor = 1;
		double distance;
		double angle;

		if (Math.abs(point1[0] - point0[0]) > 200) {
			factor = -1;
			if (point0[0] > point1[0]) {
				distance = Math.sqrt((Math.pow(point1[0] + 360 - point0[0], 2) + Math
						.pow(point1[1] - point0[1], 2)));
				angle = Math.atan((point1[1] - point0[1])
						/ (point1[0] + 360 - point0[0]));
			} else {
				distance = Math.sqrt((Math.pow(point1[0] - (point0[0] + 360), 2) + Math
						.pow(point1[1] - point0[1], 2)));
				angle = Math.atan((point1[1] - point0[1])
						/ (point1[0] - (point0[0] + 360)));
			}
		} else {
			distance = Math.sqrt((Math.pow(point1[0] - point0[0], 2) + Math.pow(
					point1[1] - point0[1], 2)));
			angle = Math.atan((point1[1] - point0[1]) / (point1[0] - point0[0]));
		}

		distance = distance * timeRatio;

		angle = Math.abs(angle);
		double dx = distance * Math.cos(angle);
		double dy = distance * Math.sin(angle);

		if (point1[0] > point0[0])
			point[0] = (float) (point0[0] + dx * factor);
		else
			point[0] = (float) (point0[0] - dx * factor);

		if (point[0] < 0)
			point[0] += 360;

		if (point[0] > 360)
			point[0] -= 360;

		if (point1[1] > point0[1])
			point[1] = (float) (point0[1] + dy);
		else
			point[1] = (float) (point0[1] - dy);
	}

	/**
	 * getSatelliteCount function
	 * 
	 * Gets the count of all the satellites used to get a GPS Position,
   * EGNOS Position, R&D Position, SBAS satellite and Not Used satellites 
   * on the Skyplot View  based on the satellite type
	 * @param	satType	      the type of the satellite
	 */
	private void getSatelliteCount(int satType) {
		switch (satType) {
		case 0:
			countNotUsedPos++;
			break;
		case 1:
			countGPSPos++;
			break;
		case 2:
			countEGNOSPos++;
			break;
		case 3:
			countRnDPos++;
			break;
		case 4:
			countRnDPos++;
			break;
		case 5:
			countSBASPos++;
			break;
		}
	}

	/**
	 * searchSatelliteUsed function
	 * 
	 * Gets the type of the satellite for every satellite number
	 * 
	 * @param	 satelliteTypes	        an array of satellite types
	 * @param	 prn                    the satellite number
	 * @return sat_used               the satellite type
	 */
	static int searchSatelliteUsed(double[][] satelliteTypes, double prn) {
		int sat_used = 0;
		for (int i = 0; i < satelliteTypes.length; i++) {
			if (prn == satelliteTypes[i][0])
				sat_used = (int) satelliteTypes[i][1];
		}
		return sat_used;
	}

	/**
	 * computeXY function
	 * 
	 * Computes the XY position every satellite based on the azimuth-elevation
   * of the satellite, the radius of each skyplot circle, and the center XY 
   * of the Skyplot View.
   * 
	 * @param	azimuth	           azimuth of the satellite
	 * @param	elevation	         elevation of the satellite
	 * @param	radius	           radius of the skyplot
	 * @param	centerX	           center x value of the skyplot circle
	 * @param	centerY	           center y value of the skyplot circle
	 * @param	XY	               an array that contains the screen coordinates XY.
	 */
	private void computeXY(double azimuth, double elevation, int radius,
			float centerX, float centerY, float[] XY) {
		float x, y;
		double theta = -(azimuth - 90);
		double rad = theta * Math.PI / 180.0;

		x = (float) Math.cos(rad);
		y = -(float) Math.sin(rad);

		elevation = 90 - elevation;
		double a = elevation * (radius / 90.0);

		XY[0] = (int) Math.round(centerX + (x * a) - bitmapAdjustment);
		XY[1] = (int) Math.round(centerY + (y * a) - bitmapAdjustment);
	}

  /**
   * getBitmapToDisplay function. 
   * 
   * The Bitmap to be displayed based on the type of the satellite
   * 
   * @param satType           type of the satellite
   * @return  bitmapToDisplay     the image of the satellite to be displayed
   **/
	static Bitmap getBitmapToDisplay(int satType) {
		Bitmap bitmapToDisplay = null;
		switch (satType) {
		case 0:
			bitmapToDisplay = notUsedSatelliteBitmap;
			break;
		case 1:
			bitmapToDisplay = gpsPosBitmap;
			break;
		case 2:
			bitmapToDisplay = egnosPosBitmap;
			break;
		case 5:
			bitmapToDisplay = sbasSatelliteBitmap;
			break;
		}
		return bitmapToDisplay;
	}

  /**
   * getSatelliteTextColor function. 
   * 
   * the text color of the satellite to be displayed
   * @param satType     type of the satellite
   * @return  color     the color of the satellite to be displayed
   **/
	static int getSatelliteTextColor(int satType) {
		int color = 0;
		switch (satType) {
		case 0:
			color = Color.GRAY;
			break;
		case 1:
			color = context.getResources().getColor(R.color.Slate_Blue);
			break;
		case 2:
			color = context.getResources().getColor(R.color.Lime_Green);
			break;
		case 5:
			color = context.getResources().getColor(R.color.Red);
			break;
		}
		return color;
	}

	/**
	 * displaySatelliteorNot function
	 * 
	 * Gets the information, if the satellite has to be displayed
	 * based on the users choice.
	 * 
	 * @param	satType	    type of the satellite
	 * @return  toDisplay  true if satellite has to be displayed otherwise false
	 */
	 static boolean displaySatelliteorNot(int satType) {
		 satelliteSharedPreferences = context.getSharedPreferences(
				 SkyplotStatus.KEY_SATELLITE_SHARED_PREF, 1);

		 if (satType == 5 || satType == 6 || satType == 7 || satType == 8
				 || satType == 9)
			 satType = 5;
		 boolean toDisplay = false;
		 switch (satType) {
		 case 0:
			 if (satelliteSharedPreferences.getInt(SkyplotStatus.KEY_NOTUSED_SELECTED,
					 1) == 1)
				 toDisplay = true;
			 break;
		 case 1:
			 if (satelliteSharedPreferences.getInt(SkyplotStatus.KEY_GPSPOS_SELECTED,
					 1) == 1)
				 toDisplay = true;
			 break;
		 case 2:
			 if (satelliteSharedPreferences.getInt(
					 SkyplotStatus.KEY_EGNOSPOS_SELECTED, 1) == 1)
				 toDisplay = true;
			 break;
		 case 5:
			 if (satelliteSharedPreferences.getInt(
					 SkyplotStatus.KEY_SBASSATELLITE_SELECTED, 1) == 1)
				 toDisplay = true;
			 break;
		 }
		 return toDisplay;
	 }
}

/**
 * @file CameraView.java
 *
 * Displays the camera view for the augmented reality view.
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
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

public class CameraView extends SurfaceView {

	Camera camera;
	SurfaceHolder cameraSurfaceHolder;
	ImageView arInfoImage;

	/**
	 * Class thats displays the camera view for the augmented reality view.
	 **/
	public CameraView(Context context) {
		super(context);
		cameraSurfaceHolder = this.getHolder();
		cameraSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		cameraSurfaceHolder.addCallback(surfaceHolderListener);
		setBackgroundColor(Color.TRANSPARENT);
	}

	/**
	 * Callback for the surfaceholder
	 * 
	 */
	SurfaceHolder.Callback surfaceHolderListener = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			camera = Camera.open();
			try {
				camera.setPreviewDisplay(cameraSurfaceHolder);
			} catch (Throwable t) {

			}
		}

		/**
		 * surfaceChanged function
		 * This is called immediately after any structural changes (format or size) 
		 * have been made to the surface. You should at this point update the 
		 * imagery in the surface. 
		 * @param holder	The SurfaceHolder whose surface has changed. 
		 * @param format	The new PixelFormat of the surface.
		 * @param width		The new width of the surface.
		 * @param height	The new height of the surface.  
		 */
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Parameters parameters = camera.getParameters();
			parameters.setPictureFormat(PixelFormat.JPEG);
			setCameraDisplayOrientation(getContext(), 0, camera);
			camera.setParameters(parameters);
			camera.startPreview();
		}

		/**
		 * surfaceDestroyed function
		 * This is called immediately before a surface is being destroyed. 
		 * @param holder	The SurfaceHolder whose surface is being destroyed. 
		 */
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (camera != null) {
				camera.stopPreview();
				camera.setPreviewCallback(null);
				camera.release();
				camera = null;
			}      
		}
	};

	/**
	 * setCameraDisplayOrientation function
	 * 
	 * Displays the camera based on the orientation of the device
	 * 
	 * @param context           the base context
	 * @param cameraId          the id of the camera
	 * @param camera            object of a camera class
	 */
	public static void setCameraDisplayOrientation(Context context, int cameraId,
			android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	/**
	 * closeCamera function
	 * 
	 * closes the camera, when the augmented reality view is closed.
	 */
	public void closeCamera() {
		if (camera != null)
			camera.release();
	}
}

/**
 * @file SaveNMEARTCM.java
 *
 * Saves NMEA/RTCM messages to a log file in the sd card of the device. 
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.ec.egnossdk.GlobalState;

public class SaveNMEARTCM {
     private String TAG = "NMEA-RTCM-SETTING";
     
     boolean isNMEA = false;
     String fileName ="";
     
     // File Logging
     File root = Environment.getExternalStorageDirectory();
     File fileToLog;
     FileWriter fileToLogWriter;
     BufferedWriter fileBufferedWriter;
     
     Handler handler;
     
     public SaveNMEARTCM(boolean isNMEA, String fileName) {
      this.isNMEA = isNMEA;
      this.fileName = fileName;  
      handler = new Handler();
      
      File directory = new File(root,"/EGNOSDemoApp/");
      
      if(!directory.exists())
        directory.mkdir();
      
      fileToLog = new File(directory, fileName);
      try {
        fileToLogWriter = new FileWriter(fileToLog);
        fileBufferedWriter = new BufferedWriter(fileToLogWriter);
        startNMEARTCMSaveThread();
      } catch (IOException e) {
        e.printStackTrace();
      }
      
     }
     
     public void stopNMEARTCMSaveThread() {
      if (handler != null) 
         handler.removeCallbacks(NMEARTCMSaveThread);       
     }

     private void startNMEARTCMSaveThread() {
       
       handler = new Handler();
       handler.postDelayed(NMEARTCMSaveThread, 0);
     }
          
     protected void writeToFile(String dataToWrite) {
       if (null != fileBufferedWriter) {
         try {
           fileBufferedWriter.write(dataToWrite);
           fileBufferedWriter.flush();
         } catch (IOException e) {
           Log.d(TAG, "Error " + e + "occurred while writing to file");
         }
       }
     }

     Runnable NMEARTCMSaveThread = new Runnable() {
       
       @Override
       public void run() {
         
         if(isNMEA) {// save NMEA data
           if(GlobalState.getGPVTGSentence() != null) {
             writeToFile(GlobalState.getGPGGASentence()+"\n");
             writeToFile(GlobalState.getGPGLLSentence()+"\n");
             writeToFile(GlobalState.getGPGSASentence()+"\n");
             for(int i = 0; i < GlobalState.getGPGSVSentence().length; i++)
               writeToFile(GlobalState.getGPGSVSentence()[i]+"\n");
             writeToFile(GlobalState.getGPRMCSentence()+"\n");           
             writeToFile(GlobalState.getGPVTGSentence()+"\n");
           }
         }else {// save RTCM data
           if(GlobalState.getRtcmMessagesByte1()!= null) {
             char[][] RTCMMsg1= GlobalState.getRtcmMessagesByte1();
             for(int i = 0; i < RTCMMsg1.length; i++)
               writeToFile(String.valueOf(RTCMMsg1[i])+"\n");
           }
           
           if(GlobalState.getRtcmMessagesByte3()!= null) {
             char[][] RTCMMsg3= GlobalState.getRtcmMessagesByte3();
             for(int i = 0; i < RTCMMsg3.length; i++)
               writeToFile(String.valueOf(RTCMMsg3[i])+"\n");
           }
           
         }
         handler.postDelayed(NMEARTCMSaveThread, 1000);
       }
     };
     
     protected void closeFile() {
       if (fileBufferedWriter != null) {
         try {
           fileBufferedWriter.close();
           fileBufferedWriter = null;
         } catch (Exception e) {
           Log.e(TAG, "ECIO |  Could not close internal log file buffered writer.");
         }
       }

       if (fileToLogWriter != null) {
         try {
           fileToLogWriter.close();
         } catch (Exception e) {
           Log.e(TAG, "ECIO |  Could not close internal log file writer.");
         }
       }
     }
}

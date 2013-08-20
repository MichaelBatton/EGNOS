/**
 * @file DeviceDetailFragment.java
 *
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
package com.ec.wifidirect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ec.R;
import com.ec.egnossdk.GlobalState;
import com.ec.wifidirect.DeviceListFragment.DeviceActionListener;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                        );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
//                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                        intent.setType("image/*");
//                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                        
                        connectClient((TextView) mContentView.findViewById(R.id.status_text),new Handler());
                    }
                });


        
        return mContentView;
    }
    private void connectClient(TextView tv,Handler handle){
    	String host = info.groupOwnerAddress.getHostAddress();
    	// TODO this String Builder will have updates from
    	//remote server
    	Client c = new Client(host, WiFiDirectActivity.SERVER_PORT, WiFiDirectActivity.sb,handle,tv);
    	c.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
        	
        	/*
        	 * start server if it is not started or if previous instances is 
        	 * terminated then create a new one and start server 
        	 * 
        	 * */
        	if (WiFiDirectActivity.sendingServer.getState() == Thread.State.NEW)
        	{
        		WiFiDirectActivity.sendingServer.start();
        	}
        	if (WiFiDirectActivity.sendingServer.getState() == Thread.State.TERMINATED)
        	{
        		WiFiDirectActivity.sendingServer = new Server(WiFiDirectActivity.SERVER_PORT);
        		WiFiDirectActivity.sendingServer.start();
        	}
//        	DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//        	fragmentList.enableStartStopView(true);
//            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }


}
 class Server extends Thread{

	private static String SERVER_TAG = "WIFI-SERVER";
	private static ServerSocket serverSocket;
	private int port;
	private UpdateClientMessage UpdateClientMessageThread;
	private StringBuffer nmeaMessage;
	private static boolean isRunning = true;
	public Server(int port){
		this.port = port;
		

		nmeaMessage = new StringBuffer();
		UpdateClientMessageThread = new UpdateClientMessage(nmeaMessage);
		
	}
	public void startServer(){
		
	}
	@Override
	public void run() {
		
		super.run();
		if(serverSocket == null){
			try {
				serverSocket = new ServerSocket(port);
			} catch (IOException e) {
				 
				Log.e(SERVER_TAG, e.toString());
			}
		}
		UpdateClientMessageThread.start(); //start thread for updating message after constant interval of time 
		while(isRunning){
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				HandleClient newClient =  new HandleClient(clientSocket,nmeaMessage);
				newClient.start();
			} catch (IOException e) {
				 
				Log.e(SERVER_TAG, e.toString());
			}
		}
	}
	public void cancle(){
		isRunning= false;
	}
	/*
	 * Thread class to handle individual clients connecting to this server
	 * */
	static class HandleClient extends Thread{
		Socket client;
		OutputStream os;
		StringBuffer localBuf;
		/*
		 *  @Socket that represent the connection with the client and server 
		 *  @StringBuffer the point from where the message will be picked it's value is updated by other thread
		 * */
		public HandleClient(Socket s,StringBuffer sb){
			super(s.getRemoteSocketAddress().toString());
			this.client = s;
			localBuf = sb;
		}
		@Override
		public void run() {
			/*
			 * Enter in infinite loop to send message to client 
			 * get message from StringBuilder object ( UpdateClientMessage thread is updating it's value)
			 * and write this message to the client socket
			 * */
			super.run();
			try {
				os = client.getOutputStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while(isRunning){
				try {
					
						os.write(localBuf.toString().getBytes());
						System.out.println(localBuf.toString() +"  " +this.getName());
						Thread.sleep(2000);
	
				}
				catch (SocketException e) {
					 
					Log.e(SERVER_TAG, e.toString());
					break; // end this loop and complete this thread 
				}
				catch (IOException e) {
					 
					Log.e(SERVER_TAG, e.toString());
					break; // end this loop and complete this thread 
				} 
				catch (InterruptedException e) {
					 
					Log.e(SERVER_TAG, e.toString());
					break; // end this loop and complete this thread 
				}
				
				
			}
			
		}
		
	}
	private static class UpdateClientMessage extends Thread{
	
		StringBuffer msg;
		public UpdateClientMessage(StringBuffer sb) {
			this.msg = sb;
		}
		@Override
		public void run() {
			/*
			 *  get the message from message pump and set it's value in StringBuffer instance
			 *  and go to sleep (give chance to other Threads)
			 *  
			 * */
			super.run();
			while(isRunning){
				String message = getMessage();
			try {
					msg.setLength(0);
					msg.append(message);
					Thread.sleep(2000);
					
				} catch (InterruptedException e) {
					 
					Log.e(SERVER_TAG, e.toString());
				}
			}
			
		}
	}
	static int var = 0;
	synchronized static String getMessage(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(GlobalState.getGPGGASentence()+"\n");
		sb.append(GlobalState.getGPGLLSentence()+"\n");
		sb.append(GlobalState.getGPGSASentence()+"\n");

		for(int i = 0; i < GlobalState.getGPGSVSentence().length; i++){
			sb.append(GlobalState.getGPGSVSentence()[i]+"\n");
			sb.append(GlobalState.getGPRMCSentence()+"\n");						
			sb.append(GlobalState.getGPVTGSentence()+"\n");
		  }
		return sb.toString() ; 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server s = new Server(5000);
		s.start();
	}

}
 class Client extends Thread{
		private boolean D;
		private String TAG_CLIENT = "WIFI-CLIENT";
		private Socket clientSocket;
		private InputStream inputStream;
		private StringBuilder stringBuilder;
		private String host;
		private int port;
		TextView textView;
		Handler handler;
		/*
		 * This StringBuilder object will provide the update to the int
		 * -urested listener
		 * */
		public Client(String host,int port,StringBuilder stringBuilder,Handler handle,TextView tv){
			this.stringBuilder = stringBuilder;
			this.host = host;
			this.port = port;
			
			this.handler = handle;
			this.textView = tv;
		}
		public void setDubugMode(boolean mode){
			D = mode;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				clientSocket = new Socket(host, port);
				
			} catch (UnknownHostException e) {
					Log.e(TAG_CLIENT, e.toString());
			} catch (IOException e) {
				Log.e(TAG_CLIENT, e.toString());
			}
			if(clientSocket != null){
				try {
					inputStream = clientSocket.getInputStream();
				} catch (IOException e) {
					Log.e(TAG_CLIENT, e.toString());	
				}
				byte[] buf = new byte[1024];
				int len;
				while(true){
					try {
						len = inputStream.read(buf);
						final String messageFromServer = new String(buf,0,len);
						if(D){
							System.out.println(messageFromServer);
						}
						stringBuilder.setLength(0);
						stringBuilder.append(messageFromServer);
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								textView.append(messageFromServer.toString());
								if(textView.getLineCount() > 200){
									textView.setText("");
								}
							}
						});
					} catch (IOException e) {
						Log.e(TAG_CLIENT, e.toString());	
						break;
					}
				}
				
			}
			
		}
//		/**
//		 * @param args
//		 */
//		public static void main(String[] args) {
//			// TODO Auto-generated method stub
//			StringBuilder sb = new StringBuilder();
//			Client c = new Client("localhost", 5000,sb);
//			c.setDubugMode(true);
//			c.start();
////			while(true){
////				Log.e("Message ", sb.toString());	
////			}
//		}

	}


package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.discover.DiscoverInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.filetransferservice.FileTransferServiceInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.CameraFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceDetailFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceListFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectconnect.WifiDirectConnectInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectreceive.WifiDirectReceiveInfo;
import no.ntnu.item.arctis.examples.realtransmissions.Message;
import no.ntnu.item.arctis.runtime.Block;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class WiFiDirectApplication extends Block {

	private Channel channel;
	private final static String TAG = "wifidirectapplication";
	public WifiP2pManager manager;
	public WiFiDirectApplicationActivity activity;
	public boolean wifiP2pStateEnabled = false;
	public boolean progressBarVisible = false;
	public android.view.Menu menu;
	public WifiP2pInfo connectionInfo;
	public WifiP2pGroup groupInfo;
	private WifiP2pDevice device;
	private List<WifiP2pDevice> connectionList = new ArrayList<WifiP2pDevice>();
	private List<WifiP2pInfo> connectionInfoList = new ArrayList<WifiP2pInfo>();
	public no.ntnu.item.arctis.android.erlendga.wifidirect.groupowner.GroupOwnerInfo groupOwnerInfo;
	public final int port = 8988;
	private String senderIP;
	private String message;
	public static final String MESSAGE_SUFFIX = "</v:Envelope>";
	private static final int INITIAL = 0;
	private static final int START_CAMERA = 1;
	private static final int SWITCH_CAMERA = 2;
	private static final int TAKE_PHOTO = 3;
	private Switch groupOwnerSwitch;
	private boolean clientIpReceived = false;
	private boolean cameraOn = false;
	
	public void initialize() {
		manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
		
		activity.runOnUiThread(new Runnable() {

			public void run() {
				groupOwnerSwitch = (Switch) activity.findViewById(R.id.group_owner_switch);
			}
		});
		
		channel = manager.initialize(activity, activity.getMainLooper(), new ChannelListener() {
			
			public void onChannelDisconnected() {
				Log.d(TAG, "Channel lost for the first time. Trying again.");
				
				activity.runOnUiThread(new Runnable() {
					
					public void run() {
						Toast.makeText(activity, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
					}
				});
				
				resetData();
				
				manager.initialize(activity, activity.getMainLooper(), new ChannelListener() {
					
					public void onChannelDisconnected() {
						Log.d(TAG, "Channel is probably lost permanently.");
						activity.runOnUiThread(new Runnable() {
							
							public void run() {
								Toast.makeText(activity, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
							}
						});
					}
				});
			}
		});
		
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				getCameraFragment().getCameraView().findViewById(R.id.camera).setVisibility(View.GONE);
			}
		});
		
		activity.setParentID(blockID);
	}

	private void resetData() {
		if (getDeviceListFragment() != null) {
			getDeviceListFragment().clearPeers();
		}
		if (getDeviceDetailFragment() != null) {
			getDeviceDetailFragment().resetViews();
		}
	}
	
	private DeviceListFragment getDeviceListFragment() {
		return (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
	}
	
	

	/** Receives available P2P devices and sets up a WPS with each device if this is not previously been done. //TODO TEST UPDATES AV UI
	 * 
	 * @param peerList The list available P2P devices
	 */
	public void peersAvailable(final WifiP2pDeviceList peerList) {
		for (WifiP2pDevice device : peerList.getDeviceList()) {
			if (!connectionList.contains(device)) {
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
	            config.wps.setup = WpsInfo.PBC;
	            if (groupOwnerSwitch.isChecked()) {
	            	config.groupOwnerIntent = 15;
				}
				else config.groupOwnerIntent = 0;
				connectionList.add(device);
		        sendToBlock("CONNECT", config);
			}
		}
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				List<WifiP2pDevice> peers = getDeviceListFragment().getPeers();
				peers.clear();
				peers.addAll(peerList.getDeviceList());
				getDeviceListFragment().notifyDataSetChanged();
				
				if (peers.size() == 0) {
		            getDeviceDetailFragment().resetViews();
		        } 
			}
		});
	}

	public void updateThisDevice(final WifiP2pDevice device) {
		this.device = device;
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (!wifiP2pStateEnabled) {
					getDeviceListFragment().resetThisDevice();
				}
				else getDeviceListFragment().updateThisDevice(device);
			}
		});
	}
	
	
    
    private DeviceDetailFragment getDeviceDetailFragment() {
		return (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
	}
	
	private String getReason(int reasonCode) {
		String reason = null;
		switch (reasonCode) {
			case WifiP2pManager.ERROR:
				reason = "Internal error";
				break;
			case WifiP2pManager.P2P_UNSUPPORTED:
				reason = "P2P is unsupported on the device";
				break;
			case WifiP2pManager.BUSY:
				reason = "The framework is busy and unable to service the request";
				break;
			default:
				reason = "Unknown error";
				break;
		}
		return reason;
	}

	// Do not edit this constructor.
	public WiFiDirectApplication() {
	}

	public void startWirelessSettings() {
		if (manager != null && channel != null) {
					activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
		}
		else {
			Log.e(TAG, "channel or manager is null");
		}
	}

	public void discoverySuccess() {
		enableProgressBar(true);
	}

	public void p2pOffWarning() {
		enableProgressBar(false);
		createAlertDialog("Enable P2P from action bar button above or system settings");
	}

	public void discoveryFailed(final int reasonCode) {
		createAlertDialog("Discover Peers Failed. Reason: " + getReason(reasonCode));
	}

	public void connectFailed(final int reasonCode) {
		createAlertDialog("Connect failed. Reason: " + getReason(reasonCode));
	}
	
	private void createAlertDialog(final String message) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Builder builder = new Builder(activity);
				builder.setMessage(message).setCancelable(false).setPositiveButton("OK", null);
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		});
	}
	
	private void enableProgressBar(final boolean enable) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (enable) {
					if (!progressBarVisible) {
						activity.onPrepareOptionsMenu(menu);
						progressBarVisible = true;
					}
				}
				if (!enable) {
					if (progressBarVisible) {
						activity.onPrepareOptionsMenu(menu);
						progressBarVisible = false;
					}
				}
			}
		});
	}

	public void cancelConnectSuccess() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				getDeviceDetailFragment().resetViews();
				Toast.makeText(activity, "Connection aborted", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void cancelConnectFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Connect connect request failed. Reason: " + getReason(reasonCode), Toast.LENGTH_LONG).show();
			}
		});
	}

	public DiscoverInfo initDiscover() {
		if (!connectionList.isEmpty()) {
			connectionList.clear();
		}
		DiscoverInfo discoverInfo = new DiscoverInfo();
		discoverInfo.channel = channel;
		discoverInfo.wifiP2pStateEnabled = wifiP2pStateEnabled;
		discoverInfo.manager = manager;
		return discoverInfo;
	}

	public WifiDirectConnectInfo initWifiDirectConnect(WifiP2pConfig config) {
		WifiDirectConnectInfo connectInfo = new WifiDirectConnectInfo();
		connectInfo.channel = channel;
		connectInfo.config = config;
		connectInfo.manager = manager;
		return connectInfo;
	}
	
	/** The connection operation is successful. The progress dialog is dismissed and a cancel connect operation will not be able to be performed.
	 * 
	 */
	public void connectSuccess() {
		ProgressDialog progressDialog = getDeviceDetailFragment().getDeviceDetailProgressDialog();
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
	}

	public void removeGroupFailed(final int reasonCode) {
		createAlertDialog("Disconnection failed. Reason: " + getReason(reasonCode));
	}

	/** The device notices that it has no Wi-Fi connection. This means that something went wrong.
	 * 
	 */
	public void notConnected() {
		Log.d(TAG, "Not connected");
		enableProgressBar(false);
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
//				createAlertDialog("Error! No Wi-Fi connectivity");
				if (device.status != WifiP2pDevice.CONNECTED) {
					getDeviceDetailFragment().resetViews();
				}
			}
		});
	}

	public void updateConnectionInfo() {
        enableProgressBar(false);
        
        if (!connectionInfoList.contains(connectionInfo)) {
			activity.runOnUiThread(new Runnable() {
			
				public void run() {
	
			        if (connectionInfo.groupFormed) {
			        	Resources resources = getDeviceDetailFragment().getResources();
			        	final CompoundButton cameraSwitch = (CompoundButton) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.swich_camera_switch);
			        	cameraSwitch.setVisibility(View.GONE);
			        
			        	if (connectionInfo.isGroupOwner) {
			        		((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.status_text)).setText(resources.getString(R.string.server_text));
			        		
			        		final Button cameraBtn = (Button) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_start_client);
			        		
			        		final Button takePhotoBtn = (Button) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_take_picture);
			        				        		
			        		cameraBtn.setOnClickListener(new OnClickListener() {
			        			
								public void onClick(View v) {									
									if (!cameraOn) {
										cameraOn = true;
										cameraBtn.setText("Close Camera");
										takePhotoBtn.setVisibility(View.VISIBLE);
										cameraSwitch.setVisibility(View.VISIBLE);
									}
									else {
										cameraOn = false;
										cameraBtn.setText("Open Camera");
										takePhotoBtn.setVisibility(View.GONE);
										cameraSwitch.setVisibility(View.GONE);
									}
									sendToBlock("SEND_MESSAGE", START_CAMERA);
								}
							});
							
							takePhotoBtn.setOnClickListener(new OnClickListener() {
								
								public void onClick(View v) {
									sendToBlock("SEND_MESSAGE", TAKE_PHOTO);
								}
							});
	
			        		cameraSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {	

								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
									sendToBlock("SEND_MESSAGE", SWITCH_CAMERA);
								}
							});
				        }
				        else {
				        	((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.status_text)).setText(resources.getString(R.string.client_text));
				            sendToBlock("SEND_MESSAGE", INITIAL);
				        }
					}
				}
			});
		connectionInfoList.add(connectionInfo);
		}
	}
	
	public Activity getActivity() {
    	return activity;
	}
	
	public FileTransferServiceInfo transfer(URI uri) {
//		activity.runOnUiThread(new Runnable() {
//		
//			public void run() {
//				getCameraFragment().getCameraView().findViewById(R.id.camera).setVisibility(View.GONE);
//			}
//		});
		
		FileTransferServiceInfo info = new FileTransferServiceInfo();
		info.URIFilePath = uri.toString();
		info.receiverIP = senderIP;
		return info;
	}

	public void removeGroupSuccess() {
		Log.d(TAG, "Disconnect success");
		
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				getDeviceDetailFragment().resetViews();
			}
		});
	}

	public void showGroupInfo() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.group_details).setVisibility(View.VISIBLE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.detail_view).setVisibility(View.GONE);
				
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_disconnect).setVisibility(View.GONE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_group_info).setVisibility(View.GONE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_take_picture).setVisibility(View.GONE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.swich_camera_switch).setVisibility(View.GONE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_start_client).setVisibility(View.GONE);
				
				((TextView)getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.interface_name)).setText("Interface Name: " + groupInfo.getInterface());
				((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.network_name)).setText("SSID: " + groupInfo.getNetworkName());
				((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.go_ip_address)).setText("Group Owner IP Address: " + connectionInfo.groupOwnerAddress.getHostAddress());
				((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.is_go))
					.setText("Is Group Owner: " 
							+ ((groupInfo.isGroupOwner() == true) ? getDeviceDetailFragment().getResources().getString(R.string.yes) : getDeviceDetailFragment().getResources().getString(R.string.no)));
				if (connectionInfo.isGroupOwner) {
					if (senderIP != null) {
						((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.clients)).setText("Client IP Address: " + senderIP);
					}
					((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.passfrase)).setText("Passfrase: " + groupInfo.getPassphrase());
				}
			}
		});
	}

	public void showConnectionInfo() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (connectionInfo.groupFormed) {
					getCameraFragment().getCameraView().findViewById(R.id.camera).setVisibility(View.GONE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.frag_detail).setVisibility(View.VISIBLE);
				
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.detail_view).setVisibility(View.VISIBLE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_disconnect).setVisibility(View.VISIBLE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_group_info).setVisibility(View.VISIBLE);
	
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.group_details).setVisibility(View.GONE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.swich_camera_switch).setVisibility(View.GONE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_connect).setVisibility(View.GONE);
			        getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_back).setVisibility(View.GONE);
			        if (connectionInfo.isGroupOwner && clientIpReceived) {
						getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
					}
				}	
			}
		});
	}

	private File getTempFile() {
		final File path = new File(Environment.getExternalStorageDirectory(), activity.getPackageName());
		if (!path.exists()) {
			path.mkdir();
		}
		return new File(path, "image.tmp");
	}

	public void startCamera(final boolean cameraOn) {	
	
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (cameraOn) {
					getCameraFragment().getCameraView().findViewById(R.id.camera).setVisibility(View.VISIBLE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.frag_detail).setVisibility(View.GONE);
				}
				else {
					getCameraFragment().getCameraView().findViewById(R.id.camera).setVisibility(View.GONE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.frag_detail).setVisibility(View.VISIBLE);
				}
				
//				getCameraFragment().takePicture();
			}
		});
	}
	
	private CameraFragment getCameraFragment() {
		return (CameraFragment) activity.getFragmentManager().findFragmentById(R.id.camera);
	}

	public Message serialize(int prefix) {
		Message message = new Message();
		message.receiverPort = port;
		Gson gson = new Gson();
		
		String json = null;
		
		switch (prefix) {
			case INITIAL:
				message.receiverIP = connectionInfo.groupOwnerAddress.getHostAddress();		
				json = Integer.toString(INITIAL);
				break;
			case START_CAMERA:
				message.receiverIP = senderIP;				
				json = Integer.toString(START_CAMERA) + gson.toJson(cameraOn);
				break;
			case SWITCH_CAMERA:
				message.receiverIP = senderIP;
				json = Integer.toString(SWITCH_CAMERA);// + gson.toJson(cameraSwitchChecked);
				break;
			case TAKE_PHOTO:
				message.receiverIP = senderIP;
				json = Integer.toString(TAKE_PHOTO);
				break;
		}
		message.message = json;
		return message;
	}

	public void extract(Message message) {
		this.senderIP = message.senderIP;
		if (message.message.contains(MESSAGE_SUFFIX)) {
			this.message = message.message.substring(0, message.message.length() - MESSAGE_SUFFIX.length());
		}
		else this.message = message.message;
	}

	public void deserialize() {
		Gson gson = new Gson();
		if (message.startsWith(Integer.toString(INITIAL))) {
			clientIpReceived = true;
			
			activity.runOnUiThread(new Runnable() {
				
				public void run() {
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
				}
			});
			
		}
		else if (message.startsWith(Integer.toString(START_CAMERA))) {
			if (!message.contentEquals(Integer.toString(START_CAMERA))) {
				message = message.substring(Integer.toString(START_CAMERA).length(), message.length());
				boolean cameraOn = gson.fromJson(message, boolean.class);
				sendToBlock("START_CAMERA", cameraOn);
			}
		}
		else if (message.startsWith(Integer.toString(SWITCH_CAMERA))) {
			sendToBlock("SWITCH_CAMERA");
		}
		else if (message.startsWith(Integer.toString(TAKE_PHOTO))) {
			sendToBlock("TAKE_PHOTO");
		}
	}

	public void viewImage(String result) {
		if (result != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + result), "image/*");
			activity.startActivity(intent);
		}
	}

	public WifiDirectReceiveInfo initWifiDirectReceive() {
		WifiDirectReceiveInfo receiveInfo = new WifiDirectReceiveInfo();
		receiveInfo.channel = channel;
		receiveInfo.manager = manager;
		return receiveInfo;
	}

	public void switchCamera() {
		activity.runOnUiThread(new Runnable() {
				
				public void run() {
					getCameraFragment().switchCamera();
				}
			});
	}

	public void takePhoto() {
		activity.runOnUiThread(new Runnable() {

			public void run() {
				getCameraFragment().takePicture();
			}
		});
	}
}
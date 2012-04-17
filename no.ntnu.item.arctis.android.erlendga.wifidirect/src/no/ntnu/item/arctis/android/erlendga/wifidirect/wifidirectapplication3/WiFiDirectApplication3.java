package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication3;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.filetransferservice.FileTransferServiceInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.CameraFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceDetailFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceListFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirect1.WifiDirect1;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirect4.WifiDirect4;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirect4.WifiDirect4Update;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication.WiFiDirectApplicationActivity;
import no.ntnu.item.arctis.examples.realtransmissions.Message;
import no.ntnu.item.arctis.runtime.Block;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

public class WiFiDirectApplication3 extends Block {

	public WifiP2pInfo connectionInfo;
	public WifiP2pGroup groupInfo;
	private WifiP2pDevice thisDevice;
	public Menu menu;
	
	public WiFiDirectApplicationActivity activity;
	
	public boolean progressBarVisible = false;
	private boolean clientIpReceived = false;
	private boolean cameraOn = false;
	
	private static final String TAG = "wifidirectapplication";
	public static final String MESSAGE_SUFFIX = "</v:Envelope>";
	private String senderIP;
	private String message;
	
	public final int port = 8988;
	private static final int INITIAL = 0;
	private static final int START_CAMERA = 1;
	private static final int SWITCH_CAMERA = 2;
	private static final int TAKE_PHOTO = 3;
	private static final int NOT_CONNECTED = 3;
	private static final int WIFI_P2P_DISABLED = 4;
	
	private List<WifiP2pInfo> connectionInfoList = new ArrayList<WifiP2pInfo>();

	private DeviceListFragment deviceListFragment;
	private DeviceDetailFragment deviceDetailFragment;
	private CameraFragment cameraFragment;
	
	private Resources deviceDetailFragmentResources;

	private CompoundButton cameraSwitch;
	private Switch groupOwnerSwitch;
	
	private TextView deviceDetailTextView;
	private TextView interfaceNameTextView;
	private TextView networkNameTextView;
	private TextView goIpAddressTextView;
	private TextView isGoTextView;
	private TextView clientsTextView;
	private TextView passfraseTextView;
	
	private View cameraView;
	private View deviceDetailView;
	
	private LinearLayout groupDetailLinearLayout;
	private LinearLayout detailLinearLayout;
	
	private Button cameraButton;
	private Button takePhotoButton;
	private Button backButton;
	private Button disconnectButton;
	private Button groupInfoButton;
	private Button connectButton;
	
	private ProgressDialog progressDialog;
	public boolean disconnected = false;
	
	public void initialize() {
		activity.runOnUiThread(new Runnable() {

			public void run() {	
				deviceListFragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
				deviceDetailFragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
				cameraFragment = (CameraFragment) activity.getFragmentManager().findFragmentById(R.id.camera);
				
				groupOwnerSwitch = (Switch) activity.findViewById(R.id.group_owner_switch);	
				//TODO: From CompundButton to Switch?
				cameraSwitch = (CompoundButton) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.swich_camera_switch);
				
				deviceDetailFragmentResources = deviceDetailFragment.getResources();

				groupDetailLinearLayout = (LinearLayout) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.group_details);
				//TODO: Maybe remove from here and XML
				detailLinearLayout = (LinearLayout) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.detail_view);
				
				cameraButton = (Button) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.btn_start_client);
				takePhotoButton = (Button) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.btn_take_picture);
				backButton = (Button) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.btn_back);
				disconnectButton = (Button) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.btn_disconnect);
				groupInfoButton = (Button) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.btn_group_info);
				connectButton = (Button) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.btn_connect);
				
				deviceDetailTextView = ((TextView) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.status_text));
				interfaceNameTextView = (TextView) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.interface_name);
				networkNameTextView = (TextView) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.network_name);
				goIpAddressTextView = (TextView) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.go_ip_address);
				isGoTextView = (TextView) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.is_go);
				clientsTextView = (TextView) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.clients);
				passfraseTextView = (TextView) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.passfrase);
				
				deviceDetailView = (View) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.frag_detail);
				cameraView = (View) cameraFragment.getCameraView().findViewById(R.id.camera);
				
				cameraView.setVisibility(View.GONE);
				
				progressDialog = deviceDetailFragment.getDeviceDetailProgressDialog();
			}
		});
		
		activity.setParentID(blockID);
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

	public void resetDeviceDetailFragment() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				deviceDetailFragment.resetViews();
				deviceListFragment.resetThisDevice();
				if (cameraOn)
					cameraView.setVisibility(View.GONE);
			}
		});
	}
	
	/** The connection operation is successful. The progress dialog is dismissed and a cancel connect operation will not be able to be performed.
	 * 
	 */
	public void dismissProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        enableProgressBar(false);
	}
	
	public Activity getActivity() {
    	return activity;
	}
	
	public FileTransferServiceInfo transfer(URI uri) {
		FileTransferServiceInfo info = new FileTransferServiceInfo();
		info.URIFilePath = uri.toString();
		info.receiverIP = senderIP;
		return info;
	}

	public void showGroupInfo() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				groupDetailLinearLayout.setVisibility(View.VISIBLE);
				
				detailLinearLayout.setVisibility(View.GONE);
				
				backButton.setVisibility(View.VISIBLE);
				disconnectButton.setVisibility(View.GONE);
				groupInfoButton.setVisibility(View.GONE);
				takePhotoButton.setVisibility(View.GONE);
				cameraButton.setVisibility(View.GONE);
				
				cameraSwitch.setVisibility(View.GONE);
				
				interfaceNameTextView.setText("Interface Name: " + groupInfo.getInterface());
				networkNameTextView.setText("SSID: " + groupInfo.getNetworkName());
				goIpAddressTextView.setText("Group Owner IP Address: " + connectionInfo.groupOwnerAddress.getHostAddress());
				isGoTextView.setText("Is Group Owner: " + ((groupInfo.isGroupOwner() == true) ? deviceDetailFragmentResources.getString(R.string.yes) : deviceDetailFragmentResources.getString(R.string.no)));
				
				if (connectionInfo.isGroupOwner) {
					if (senderIP != null) {
						clientsTextView.setText("Client IP Address: " + senderIP);
					}
					passfraseTextView.setText("Passfrase: " + groupInfo.getPassphrase());
				}
			}
		});
	}

	public void showDeviceDetailFragment() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (connectionInfo.groupFormed) {
					cameraView.setVisibility(View.GONE);
					deviceDetailView.setVisibility(View.VISIBLE);
					detailLinearLayout.setVisibility(View.VISIBLE);
					
					groupDetailLinearLayout.setVisibility(View.GONE);
					
					disconnectButton.setVisibility(View.VISIBLE);
					groupInfoButton.setVisibility(View.VISIBLE);
					connectButton.setVisibility(View.GONE);
					backButton.setVisibility(View.GONE);

					if (cameraOn) {
						takePhotoButton.setVisibility(View.VISIBLE);
						cameraSwitch.setVisibility(View.VISIBLE);
					}
					else {
						cameraSwitch.setVisibility(View.GONE);
					}

			        if (connectionInfo.isGroupOwner && clientIpReceived) {
			        	cameraButton.setVisibility(View.VISIBLE);
					}
					
					
				}	
			}
		});
	}

	public void startCamera(final boolean cameraOn) {	
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (cameraOn) {
					cameraView.setVisibility(View.VISIBLE);
					deviceDetailView.setVisibility(View.GONE);
				}
				else {
					cameraView.setVisibility(View.GONE);
					deviceDetailView.setVisibility(View.VISIBLE);
				}
			}
		});
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
				json = Integer.toString(SWITCH_CAMERA);
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
		if (message.message.contains(MESSAGE_SUFFIX))
			this.message = message.message.substring(0, message.message.length() - MESSAGE_SUFFIX.length());
		else 
			this.message = message.message;
	}

	public void deserialize() {
		Gson gson = new Gson();
		
		if (message.startsWith(Integer.toString(INITIAL))) {
			clientIpReceived = true;
			
			activity.runOnUiThread(new Runnable() {
				
				public void run() {
					cameraButton.setVisibility(View.VISIBLE);
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
		else if (message.startsWith(Integer.toString(SWITCH_CAMERA)))
			sendToBlock("SWITCH_CAMERA");
		else if (message.startsWith(Integer.toString(TAKE_PHOTO)))
			sendToBlock("TAKE_PHOTO");
	}

	public void viewImage(String result) {
		if (result != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + result), "image/*");
			activity.startActivity(intent);
		}
	}

	public void switchCamera() {
		activity.runOnUiThread(new Runnable() {
		
				public void run() {
					cameraFragment.switchCamera();
				}
			});
	}

	public void takePhoto() {
		activity.runOnUiThread(new Runnable() {

			public void run() {
				cameraFragment.takePicture();
			}
		});
	}

	public void groupOwner() {
		disconnected = false;
		
		activity.runOnUiThread(new Runnable() {

			public void run() {
				deviceDetailTextView.setText(deviceDetailFragmentResources.getString(R.string.server_text));
			        				        		
				cameraButton.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {									
						if (!cameraOn) {
							cameraOn = true;
							cameraButton.setText("Close Camera");
							takePhotoButton.setVisibility(View.VISIBLE);
							cameraSwitch.setVisibility(View.VISIBLE);
						}
						else {
							cameraOn = false;
							cameraButton.setText("Open Camera");
							takePhotoButton.setVisibility(View.GONE);
							cameraSwitch.setVisibility(View.GONE);
						}
						
						sendToBlock("SEND_MESSAGE", START_CAMERA);
					}
				});
				
				takePhotoButton.setOnClickListener(new OnClickListener() {
					
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
		});
	}

	public void groupClient() {
		disconnected = false;
		
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				deviceDetailTextView.setText(deviceDetailFragmentResources.getString(R.string.client_text));
	            sendToBlock("SEND_MESSAGE", INITIAL);
			}
		});
	}

	public WifiP2pConfig setWifiP2pConfig() {
		enableProgressBar(true);
	
		WifiP2pConfig config = new WifiP2pConfig();
		config.wps.setup = WpsInfo.PBC;
		
		if (groupOwnerSwitch.isChecked())
			config.groupOwnerIntent = 15;
		else config.groupOwnerIntent = 0;
		
		return config;
	}

	public void getFailure(int failureCode) {
		switch (failureCode) {
			case WifiDirect4.ERROR:
				createAlertDialog("Internal error.");
				break;
			case WifiDirect4.P2P_UNSUPPORTED:
				createAlertDialog("P2P is unsupported on the device.");
				break;
			case WifiDirect4.BUSY:
				createAlertDialog("The framework is busy and unable to service the request.");
				break;
			case WifiDirect4.NOT_CONNECTED:
				Log.d(TAG, "Not connected");
				enableProgressBar(false);
				activity.runOnUiThread(new Runnable() {
					
					public void run() {
						if (thisDevice != null) {
							if (thisDevice.status != WifiP2pDevice.CONNECTED) {
								deviceDetailFragment.resetViews();
								if (cameraOn)
									cameraView.setVisibility(View.GONE);
							}
						}
					}
				});
				break;
			case WifiDirect4.WIFI_P2P_DISABLED:
				enableProgressBar(false);
				createAlertDialog("Enable P2P from action bar button above or system settings.");
				break;
			case WifiDirect4.CHANNEL_LOST_1:
				createAlertDialog("Channel lost. Trying again");
			
				if (deviceListFragment != null)
					deviceListFragment.clearPeers();
					
				if (deviceDetailFragment != null)
					deviceDetailFragment.resetViews();
				break;
			case WifiDirect4.CHANNEL_LOST_2:
				createAlertDialog("Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.");
				break;
			default:
				createAlertDialog("Unknown error.");
				break;
		}
	}

	public void startWirelessSettings() {
//		if (manager != null && channel != null) {
					activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
//		}
//		else {
//			Log.e(TAG, "channel or manager is null");
//		}
	}

	public void getUpdate(WifiDirect4Update update) {
		if (update.connectionInfo != connectionInfo) {
			connectionInfo = update.connectionInfo;
			if (!connectionInfoList.contains(connectionInfo)) {
	        	if (connectionInfo.groupFormed) {   
		        	if (connectionInfo.isGroupOwner)
		        		sendToBlock("GROUP_OWNER", connectionInfo);
			        else 
			        	sendToBlock("GROUP_CLIENT", connectionInfo);
				}
	        	connectionInfoList.add(connectionInfo);
			}
		}
		
		if (update.groupInfo != groupInfo) {
			groupInfo = update.groupInfo;
		}
		
		if (update.thisDevice != thisDevice) {
			thisDevice = update.thisDevice;
			activity.runOnUiThread(new Runnable() {
			
				public void run() {
					//TODO: Test this!!!
					
					if (thisDevice.status == WifiP2pDevice.UNAVAILABLE) {
						deviceListFragment.resetThisDevice();
					}
					else {
						deviceListFragment.updateThisDevice(thisDevice);
						if (cameraOn) {
							cameraView.setVisibility(View.GONE);
						}
					}
					deviceListFragment.updateThisDevice(thisDevice);
				}
			});
		}
	}

	public void setDisconnectedTrue() {
		disconnected = true;
	}
}
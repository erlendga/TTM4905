package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication4;


import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.CameraFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceDetailFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceListFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.remotecameraservice.RemoteCameraServiceInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirect4.WifiDirect4;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirect4.WifiDirect4Update;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication.WiFiDirectApplicationActivity;
import no.ntnu.item.arctis.runtime.Block;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class WiFiDirectApplication4 extends Block {

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
	
	public final int port = 8988;
	
	private List<WifiP2pInfo> connectionInfoList = new ArrayList<WifiP2pInfo>();

	private DeviceListFragment deviceListFragment;
	private DeviceDetailFragment deviceDetailFragment;
	private CameraFragment cameraFragment;
	
	private Resources deviceDetailFragmentResources;

	private CompoundButton cameraSwitch;
	private Switch groupOwnerSwitch;

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
	private RemoteCameraServiceInfo cameraServiceInfo = new RemoteCameraServiceInfo();
//	
	public boolean disconnected;
	public no.ntnu.item.arctis.android.erlendga.wifidirect.removegroup.RemoveGroupInfo removeGroupInfo;
	public boolean responder;
	
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
			cameraServiceInfo.connectionInfo = update.connectionInfo;
			if (!connectionInfoList.contains(connectionInfo)) {
	        	if (connectionInfo.groupFormed) {
	        		disconnected = false;
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

	public void setClientIpReceivedTrue() {
		clientIpReceived = true;
	}
}
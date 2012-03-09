package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication;


import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.filetransferservice.FileTransferServiceInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceDetailFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceListFragment;
import no.ntnu.item.arctis.examples.realtransmissions.Message;
import no.ntnu.item.arctis.runtime.Block;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkInfo;
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
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
	private BroadcastReceiver receiver;
	public android.view.Menu menu;
	public WifiP2pInfo connectionInfo;
	public WifiP2pGroup groupInfo;
	private WifiP2pDevice device;
	private List<WifiP2pDevice> connectionList = new ArrayList<WifiP2pDevice>();
	private Socket clientSocket;
	private List<WifiP2pInfo> connectionInfoList = new ArrayList<WifiP2pInfo>();
	public no.ntnu.item.arctis.android.erlendga.wifidirect.groupowner.GroupOwnerInfo groupOwnerInfo;
	public final int port = 8988;
	private String senderIP;
	private String message;
	public static final String MESSAGE_SUFFIX = "</v:Envelope>";
	private static final int START_CAMERA = 0;
	private static final int TAKE_PHOTO_CODE = 20;
	
	public void initialize() {
		manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
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
		sendToBlock("LISTEN");
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

	public ArrayList<Object> initDiscover() {
		if (!connectionList.isEmpty()) {
			connectionList.clear();
		}
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(wifiP2pStateEnabled);
		objects.add(manager);
		return objects;
	}

	public ArrayList<Object> initWifiDirectConnect(WifiP2pConfig config) {
		Log.d(TAG, "Connect button is clicked. Starting Wifi Direct Connect block...");
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(config);
		objects.add(wifiP2pStateEnabled);
		objects.add(manager);
		return objects;
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
        
        activity.runOnUiThread(new Runnable() {
					
			public void run() {
				Toast.makeText(activity, "Connection is successful", Toast.LENGTH_SHORT).show();
			}
		});
        
        if (!connectionInfoList.contains(connectionInfo)) {
			activity.runOnUiThread(new Runnable() {
			
				public void run() {
	
			        if (connectionInfo.groupFormed) {
			        	Resources resources = getDeviceDetailFragment().getResources();
			        
			        	if (connectionInfo.isGroupOwner) {
			        		((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.status_text)).setText(resources.getString(R.string.server_text));
				        }
				        else {
				            ((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.status_text)).setText(resources.getString(R.string.client_text));
				               
				            getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_start_client).setOnClickListener(new OnClickListener() {
								
								public void onClick(View v) {
									sendToBlock("GROUP_CLIENT", START_CAMERA);
								}
							});
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
	
	public FileTransferServiceInfo transfer(final Intent intent) {
		FileTransferServiceInfo info = new FileTransferServiceInfo();
		info.filePath = Uri.fromFile(getTempFile()).toString();
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

	public void registerBroadcastReceiver() {
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_STATE_CHANGED_ACTION", intent);
				}
				else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION", intent);
				}
				else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_CONNECTION_CHANGED_ACTION", intent);
				}
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		
		getContext().registerReceiver(receiver, filter);
	}
	
	private Context getContext() {
		return (Context) getProperty("Android");
	}

	public boolean isWifiP2pStateEnabled(Intent intent) {
		int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
		if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
			return true;
		}
		else return false;
	}

	public WifiP2pDevice getWifiP2pDevice(Intent intent) {
		return (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
	}

	public void unregisterBroadcastReceiver() {
		getContext().unregisterReceiver(receiver);
	}

	public void checkConnectivity(Intent intent) {
		NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
		WifiP2pInfo wifiP2pInfo = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
		Log.d("broadcastreceiver2", "Checking connectivity...\n" + networkInfo.toString() + "\n" + wifiP2pInfo.toString());
		if (networkInfo.isConnected()) {
			manager.requestConnectionInfo(channel, new ConnectionInfoListener() {
				
				public void onConnectionInfoAvailable(WifiP2pInfo connectionInfo) {
					sendToBlock("ON_CONNECTION_INFO_AVAILABLE", connectionInfo);
				}
			});
			manager.requestGroupInfo(channel, new GroupInfoListener() {
				
				public void onGroupInfoAvailable(WifiP2pGroup groupInfo) {
					sendToBlock("ON_GROUP_INFO_AVAILABLE", groupInfo);
				}
			});
		}
		else sendToBlock("NOT_CONNECTED");
	}

	public void showGroupInfo() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.group_details).setVisibility(View.VISIBLE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.detail_view).setVisibility(View.GONE);
				
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_disconnect).setVisibility(View.GONE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_group_info).setVisibility(View.GONE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_start_client).setVisibility(View.GONE);
				
				((TextView)getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.interface_name)).setText("Interface Name: " + groupInfo.getInterface());
				((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.network_name)).setText("SSID: " + groupInfo.getNetworkName());
				((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.go_ip_address)).setText("Group Owner IP Address: " + connectionInfo.groupOwnerAddress.getHostAddress());
				((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.is_go)).setText("Is Group Owner: " + ((groupInfo.isGroupOwner() == true) ? getDeviceDetailFragment().getResources().getString(R.string.yes) : getDeviceDetailFragment().getResources().getString(R.string.no)));
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
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.frag_detail).setVisibility(View.VISIBLE);
				
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.detail_view).setVisibility(View.VISIBLE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_disconnect).setVisibility(View.VISIBLE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_group_info).setVisibility(View.VISIBLE);
	
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.group_details).setVisibility(View.GONE);
					getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_connect).setVisibility(View.GONE);
			        getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_back).setVisibility(View.GONE);
			        if (!connectionInfo.isGroupOwner) {
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

	public void startCamera(Intent intent) {	
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
		getDeviceDetailFragment().startActivityForResult(intent, TAKE_PHOTO_CODE);
	}

	public Message serialize(int prefix) {
		Message message = new Message();
		Gson gson = new Gson();
		String json = null;
		switch (prefix) {
		case START_CAMERA:
			
			message.receiverIP = connectionInfo.groupOwnerAddress.getHostAddress();
			message.receiverPort = port;

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			json = Integer.toString(START_CAMERA) + gson.toJson(intent);
			
			message.message = json;
			
			break;
		}
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
		if (message.startsWith(Integer.toString(START_CAMERA))) {
			message = message.substring(Integer.toString(START_CAMERA).length(), message.length());
			
			Intent intent = gson.fromJson(message, Intent.class);
			sendToBlock("START_CAMERA", intent);
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
}
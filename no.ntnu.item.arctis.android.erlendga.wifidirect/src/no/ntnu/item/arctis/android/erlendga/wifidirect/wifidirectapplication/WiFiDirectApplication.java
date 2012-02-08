package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication;


import java.util.ArrayList;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.devicedetailfragment.DeviceDetailFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.devicelistfragment.DeviceListFragment;
import no.ntnu.item.arctis.runtime.Block;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class WiFiDirectApplication extends Block {

	private Channel channel;
	private final static String TAG = "wifidirectapplication";
	public android.net.wifi.p2p.WifiP2pManager manager;
	public no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication.WiFiDirectApplicationActivity activity;
	public boolean isWifiP2pEnabled = false;
	public boolean connected = false;
	public boolean wifiP2pPeersTimedOut = false;
	
	public void initialize() {
		manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(activity, activity.getMainLooper(), null);
		activity.setParentID(blockID);
	}

	public void setWifiP2pEnabled(Intent intent) {
		int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
		if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
			this.isWifiP2pEnabled = true;
		}
		else {
			this.isWifiP2pEnabled = false;
			resetData();
		}
		Log.d(TAG, "P2P state  changed - " + state);
	}
	
	public void channelLostWarning() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
			}
		});
		resetData();
	}

	private void resetData() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				DeviceListFragment fragmentList = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
				DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
				
				if (fragmentList != null) {
					fragmentList.clearPeers();
				}
				if (fragmentDetails != null) {
					fragmentDetails.resetViews();
				}
			}
		});
	}

	public void requestPeers() {
		if (manager != null) {
			activity.runOnUiThread(new Runnable() {
				
				public void run() {
					manager.requestPeers(channel, (PeerListListener) activity.getFragmentManager().findFragmentById(R.id.frag_list));
//					manager.requestPeers(channel, new PeerListListener() {
//						
//						public void onPeersAvailable(WifiP2pDeviceList arg0) {
//							// TODO Auto-generated method stub
//							
//						}
//					});
				}
			});
		}
	}

	public void requestConnectionInfo(Intent intent) {
		if (manager == null) {
			return;
		}
		
		NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
		
		if (networkInfo.isConnected()) {
			activity.runOnUiThread(new Runnable() {
				
				public void run() {
					DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
					manager.requestConnectionInfo(channel, fragment);     
				}
			});       
		}
		else {
			resetData();
		}
		
	}

	public void updateThisDevice(final Intent intent) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
				fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
			}
		});
	}

	public void showDetails(final WifiP2pDevice device) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
				fragment.showDetails(device);
			}
		});	
	}

	public void disconnect() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				final DeviceDetailFragment fragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
				fragment.resetViews();
				
				manager.removeGroup(channel, new ActionListener() {
					
					public void onSuccess() {
						fragment.getView().setVisibility(View.GONE);
						connected = false;
					}
					
					public void onFailure(int reasonCode) {
						Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
					}
				});
			}
		});
	}
	
	private String getReason(int reasonCode) {
		String reason = null;
		switch (reasonCode) {
			case WifiP2pManager.ERROR:
				reason = "Operation failed due to an internal error";
				break;
			case WifiP2pManager.P2P_UNSUPPORTED:
				reason = "Operation failed because p2p is unsupported on the device";
				break;
			case WifiP2pManager.BUSY:
				reason = "Operation failed because the framework is busy and unable to service the request";
				break;
			default:
				reason = "Operation failed due to an unknown error";
				break;
		}
		return reason;
	}
	public Activity getActivity() {
		return activity;
	}

	// Do not edit this constructor.
	public WiFiDirectApplication() {
	}

	public void channelLost() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
			}
		});
	}

	public void startWirelessSettings() {
		if (manager != null && channel != null) {
					activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
		}
		else {
			Log.e(TAG, "channel or manager is null");
		}
	}

	public void discoveryPeersStarted() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				final DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
				fragment.onInitiateDiscovery();
			}
		});
	}
	
	private ArrayList<Object> mergeChannelAndManager() {
		ArrayList<Object> channelAndManagerList = new ArrayList<Object>();
		channelAndManagerList.add(channel);
		channelAndManagerList.add(manager);
		return channelAndManagerList;
	}

	public void discoverySuccess() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Discovery Initiated", Toast.LENGTH_SHORT).show();
			}
		});
		wifiP2pPeersTimedOut = false;
	}

	public void p2pOffWarning() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void discoveryFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Discovery Failed : " + getReason(reasonCode), Toast.LENGTH_SHORT).show();
			}
		});
	}

	public ArrayList<Object> initConnect(WifiP2pConfig config) {
		ArrayList<Object> mergedList = new ArrayList<Object>();
		mergedList.add(channel);
		mergedList.add(config);
		mergedList.add(manager);
		return mergedList;
	}

	public void connectFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
					
			public void run() {
				Toast.makeText(activity, "Connect failed. Reason: " + getReason(reasonCode), Toast.LENGTH_SHORT).show();
			}
		});
	}

	public ArrayList<Object> initCancelConnect() {
		return mergeChannelAndManager();
	}

	public void setConnectedEnabled() {
		connected = true;
	}

	public void cancelConnectSuccess() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Connection aborted", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void cancelConnectFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Connect abort request failed. Reason: " + getReason(reasonCode), Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void wifiP2pPeersChangedTimedOut() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Discovery process timed out", Toast.LENGTH_SHORT).show();
			}
		});
		wifiP2pPeersTimedOut = true;
	}

	public void dismissProgressDialog() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				final DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
				fragment.dismissProgressDialog();
			}
		});
	}

	public ArrayList<Object> initDiscover() {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(isWifiP2pEnabled);
		objects.add(manager);
		return objects;
	}
}
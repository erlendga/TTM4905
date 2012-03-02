package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectconnect;

import java.util.ArrayList;

import no.ntnu.item.arctis.runtime.Block;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.util.Log;

public class WifiDirectConnect extends Block {

	private static final String TAG = "wifidirectconnect";
	private Channel channel;
	private WifiP2pConfig config;
	private WifiP2pManager manager;
	public boolean isWifiP2pEnabled;
	public boolean isConnected;
	
	// Instance parameter. Edit only in overview page.
	public final int channelElement;
	// Instance parameter. Edit only in overview page.
	public final int configElement;
	// Instance parameter. Edit only in overview page.
	public final int managerElement;
	// Instance parameter. Edit only in overview page.
	public final int isWifiP2pEnabledElement;
	private BroadcastReceiver receiver;
	
	public void unwrap(ArrayList<Object> objects) {
		Log.d(TAG, "Inside Wifi Direct Connect block. Unwrapping.");
		channel = (Channel) objects.get(channelElement);
		config = (WifiP2pConfig) objects.get(configElement);
		isWifiP2pEnabled = (Boolean) objects.get(isWifiP2pEnabledElement);
		manager = (WifiP2pManager) objects.get(managerElement);
	}

	public ArrayList<Object> initConnect() {
		Log.d(TAG, "Initiating a connect. Going into Connect block...");
		isConnected = false;
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(config);
		objects.add(manager);
		return objects;
	}
	
	private ArrayList<Object> addChannelAndManager() {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(manager);
		return objects;
	}

	public ArrayList<Object> initRemoveGroup() {
		return addChannelAndManager();
	}

	public ArrayList<Object> initCancelConnect() {
		return addChannelAndManager();
	}

	// Do not edit this constructor.
	public WifiDirectConnect(int channelElement, int configElement, int managerElement, int isWifiP2pEnabledElement) {
	    this.channelElement = channelElement;
	    this.configElement = configElement;
	    this.managerElement = managerElement;
	    this.isWifiP2pEnabledElement = isWifiP2pEnabledElement;
	}

	public ArrayList<Object> wrap() {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(manager);
		return objects;
	}

	public void registerBroadcastReceiver() {
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(intent.getAction())) {
					sendToBlock("WIFI_P2P_CONNECTION_CHANGED_ACTION", intent);
				}

			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		
		getContext().registerReceiver(receiver, filter);
	}
	
	private Context getContext() {
		return (Context) getProperty("Android");
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

	public void unregisterBroadcastReceiver() {
		getContext().unregisterReceiver(receiver);
	}
}

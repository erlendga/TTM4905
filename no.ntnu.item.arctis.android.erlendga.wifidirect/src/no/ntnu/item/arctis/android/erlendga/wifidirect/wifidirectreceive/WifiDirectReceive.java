package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectreceive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import no.ntnu.item.arctis.runtime.Block;

public class WifiDirectReceive extends Block {

	private BroadcastReceiver receiver;
	private WifiP2pManager manager;
	private Channel channel;

	public void registerBroadcastReceiver(WifiDirectReceiveInfo receiveInfo) {
		manager = receiveInfo.manager;
		channel = receiveInfo.channel;
		
		receiver = new BroadcastReceiver() {
			
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

	public void checkConnectivity(Intent intent) {
		NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
		WifiP2pInfo wifiP2pInfo = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
		
		if (networkInfo.isConnected()) {
			manager.requestConnectionInfo(channel, new ConnectionInfoListener() {
				
				public void onConnectionInfoAvailable(WifiP2pInfo connectionInfo) {
					sendToBlock("ON_CONNECTION_INFO_AVAILABLE", connectionInfo);
				}
			});
		}
		else sendToBlock("NOT_CONNECTED");
		
		if (wifiP2pInfo.groupFormed) {
			manager.requestGroupInfo(channel, new GroupInfoListener() {
				
				public void onGroupInfoAvailable(WifiP2pGroup groupInfo) {
					sendToBlock("ON_GROUP_INFO_AVAILABLE", groupInfo);
				}
			});
		}
	}

	public void unregisterBroadcastReceiver() {
		getContext().unregisterReceiver(receiver);
	}

}

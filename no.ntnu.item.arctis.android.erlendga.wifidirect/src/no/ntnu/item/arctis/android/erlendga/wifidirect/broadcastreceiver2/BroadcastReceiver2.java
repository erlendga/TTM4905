package no.ntnu.item.arctis.android.erlendga.wifidirect.broadcastreceiver2;

import java.util.ArrayList;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import no.ntnu.item.arctis.runtime.Block;

public class BroadcastReceiver2 extends Block {

	private WifiP2pManager manager;
	private Channel channel;

	public boolean isWifiP2pStateEnabled(Intent intent) {
		int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
		if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
			return true;
		}
		else return false;
	}

	public void requestPeers() {
		manager.requestPeers(channel, new PeerListListener() {
			
			public void onPeersAvailable(WifiP2pDeviceList deviceList) {
				sendToBlock("ON_PEERS_AVAILABLE", deviceList);
			}
		});
	}

	public void unwrap(ArrayList<Object> objects) {
		channel = (Channel) objects.get(0);
		manager = (WifiP2pManager) objects.get(1);
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
//			manager.requestGroupInfo(channel, new GroupInfoListener() {
//				
//				public void onGroupInfoAvailable(WifiP2pGroup groupInfo) {
//					Log.d(TAG, "Group info available. Sending the info to the environment");
//					sendToBlock("ON_GROUP_INFO_AVAILABLE", groupInfo);
//				}
//			});
		}
		else sendToBlock("NOT_CONNECTED");
	}

	public WifiP2pDevice getWifiP2pDevice(Intent intent) {
		return (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
	}
}

package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectconnect;

import no.ntnu.item.arctis.android.erlendga.wifidirect.cancelconnect.CancelConnectInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.connect.ConnectInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.removegroup.RemoveGroupInfo;
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

	private Channel channel;
	private WifiP2pConfig config;
	private WifiP2pManager manager;
	
	private BroadcastReceiver receiver;
	public boolean isConnected;
	public boolean isWifiP2pEnabled;
	public ConnectInfo connectInfo = new ConnectInfo();
	public CancelConnectInfo cancelConnectInfo = new CancelConnectInfo();
	public RemoveGroupInfo removeGroupInfo = new RemoveGroupInfo();
	public boolean connected = false;
	
	public void setParameters(WifiDirectConnectInfo wifiDirectConnectInfo) {
		channel = wifiDirectConnectInfo.channel;
		config = wifiDirectConnectInfo.config;
		manager = wifiDirectConnectInfo.manager;
		
		connectInfo.channel = channel;
		connectInfo.config = config;
		connectInfo.manager = manager;
		
		cancelConnectInfo.channel = channel;
		cancelConnectInfo.manager = manager;
		
		removeGroupInfo.channel = channel;
		removeGroupInfo.manager = manager;
	}

	public void registerReceiver() {
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
		Log.d("wifidirectconnect", "Checking connectivity...\n" + networkInfo.toString() + "\n" + wifiP2pInfo.toString());
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

	// Do not edit this constructor.
	public WifiDirectConnect() {
	}

	public void setConnectedTrue() {
		connected = true;
	}
}

package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirect2;

import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.erlendga.wifidirect.discover.DiscoverInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectconnect.WifiDirectConnectInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectreceive.WifiDirectReceiveInfo;
import no.ntnu.item.arctis.runtime.Block;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.util.Log;

public class WifiDirect2 extends Block {

	private static final String TAG = "wifidirect2";
	public WifiDirectReceiveInfo wifiDirectReceiveInfo = new WifiDirectReceiveInfo();
	private WifiP2pManager manager;
	private Channel channel;
	private DiscoverInfo discoverInfo = new DiscoverInfo();
	public static final int NOT_CONNECTED = 3;
	public static final int WIFI_P2P_DISABLED = 4;
	public WifiP2pConfig config;
	public WifiDirectConnectInfo connectInfo = new WifiDirectConnectInfo();
	private List<WifiP2pInfo> connectionInfoList = new ArrayList<WifiP2pInfo>();
	public int notConnected;
	public int wifiP2pDisabled;
	public boolean discovering = false;
	public boolean paused = false;
	public boolean connecting = false;
	
	public void setStateEnabled(boolean stateEnabled) {
		discoverInfo.wifiP2pStateEnabled = stateEnabled;
	}

	public void peersAvailable(WifiP2pDeviceList peerList) {
		discovering = false;
		
		for (WifiP2pDevice device : peerList.getDeviceList()) {
			config.deviceAddress = device.deviceAddress;
			sendToBlock("CONNECT", config);
		}
	}

	public WifiDirectConnectInfo initWifiDirectConnect(WifiP2pConfig config) {
		connecting = true;
		connectInfo.config = config;
		return connectInfo;
	}

	public void updateConnectionInfo(WifiP2pInfo connectionInfo) {
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

	public void initialize() {
		manager = (WifiP2pManager) getContext().getSystemService(Context.WIFI_P2P_SERVICE);
		
		channel = manager.initialize(getContext(), getContext().getMainLooper(), new ChannelListener() {
			
			public void onChannelDisconnected() {
				Log.d(TAG, "Channel lost for the first time. Trying again.");				
				sendToBlock("CHANNEL_LOST", 1);
				
				manager.initialize(getContext(), getContext().getMainLooper(), new ChannelListener() {
					
					public void onChannelDisconnected() {
						Log.d(TAG, "Channel is probably lost permanently.");
						sendToBlock("CHANNEL_LOST", 2);
					}
				});
			}
		});
		
		wifiDirectReceiveInfo.manager = manager;
		wifiDirectReceiveInfo.channel = channel;
		
		discoverInfo.channel = channel;
		discoverInfo.manager = manager;
		
		connectInfo.channel = channel;
		connectInfo.config = config;
		connectInfo.manager = manager;
	}

	private Context getContext() {
		return (Context) getProperty("Android");
	}

	public int getNotConnected() {
		return NOT_CONNECTED;
	}

	public int getWifiP2pDisabled() {
		discovering = false;
		return WIFI_P2P_DISABLED;
	}

	public DiscoverInfo discover() {
		discovering = true;
		return discoverInfo;
	}

	public int discoverFailure(int reasonCode) {
		discovering = false;
		return reasonCode;
	}

	public void paused() {
		paused = true;
	}

	public void resumed() {
		paused = false;
	}

	public int connectFailure(int reasonCode) {
		connecting = false;
		return reasonCode;
	}
}

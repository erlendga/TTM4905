package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirect5;

import no.ntnu.item.arctis.android.erlendga.wifidirect.discover.DiscoverInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.removegroup.RemoveGroupInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirect4.WifiDirect4Update;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectconnect.WifiDirectConnectInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectreceive.WifiDirectReceiveInfo;
import no.ntnu.item.arctis.runtime.Block;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.util.Log;

public class WifiDirect5 extends Block {

	private static final String TAG = "wifidirect3";
	public WifiDirectReceiveInfo wifiDirectReceiveInfo = new WifiDirectReceiveInfo();
	private WifiP2pManager manager;
	private Channel channel;
	private DiscoverInfo discoverInfo = new DiscoverInfo();
	public static final int ERROR = 0;
	public static final int P2P_UNSUPPORTED = 1;
	public static final int BUSY = 2;
	public static final int NOT_CONNECTED = 3;
	public static final int WIFI_P2P_DISABLED = 4;
	public static final int CHANNEL_LOST_1 = 5;
	public static final int CHANNEL_LOST_2 = 6;
	public WifiP2pConfig config;
	public WifiDirectConnectInfo connectInfo = new WifiDirectConnectInfo();
	public int notConnected;
	public int wifiP2pDisabled;
	public boolean discovering = false;
	public boolean stopping = false;
	public boolean connecting = false;
	public WifiDirect4Update update = new WifiDirect4Update();
	private WifiP2pInfo connectionInfo;
	private WifiP2pGroup groupInfo;
	private WifiP2pDevice thisDevice;
	public boolean disconnecting = false;
	public RemoveGroupInfo removeGroupInfo = new RemoveGroupInfo();
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
		
		removeGroupInfo.channel = channel;
		removeGroupInfo.manager = manager;
		
		update.connectionInfo = null;
		update.groupInfo = null;
		update.thisDevice = null;
	}

	private Context getContext() {
		return (Context) getProperty("Android");
	}

	public int getNotConnected() {
		connecting = false;
		return NOT_CONNECTED;
	}

	public int getWifiP2pDisabled() {
		discovering = false;
		return WIFI_P2P_DISABLED;
	}

	public DiscoverInfo setDiscoveringTrue() {
		discovering = true;
		return discoverInfo;
	}

	public int setDiscoveringFalse(int reasonCode) {
		discovering = false;
		return reasonCode;
	}

	public int setConnectingFalse(int reasonCode) {
		connecting = false;
		return reasonCode;
	}

	public void setThisDevice(WifiP2pDevice device) {
		this.thisDevice = device;
	}

	public void setConnectionInfo(WifiP2pInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	public void setGroupInfo(WifiP2pGroup groupInfo) {
		this.groupInfo = groupInfo;
	}

	public void checkForUpdate() {
		if (thisDevice != update.thisDevice) {
			update.thisDevice = thisDevice;
			sendToBlock("UPDATE", update);
		}
		if (connectionInfo != update.connectionInfo) {
			update.connectionInfo = connectionInfo;
			sendToBlock("UPDATE", update);
		}
		if (groupInfo != update.groupInfo) {
			update.groupInfo = groupInfo;
			sendToBlock("UPDATE", update);
		}
	}

	public void setStoppingTrue() {
		stopping = true;
	}

	public void setDisconnectingTrue() {
		disconnecting = true;
	}

	public int setDisconnectingFalse1(int reasonCode) {
		disconnecting = false;
		return reasonCode;
	}

	public void setConAndDisconFalse() {
		connecting = false;
		disconnecting = false;
	}

	public void setDisconnectingFalse2() {
		disconnecting = false;
	}

	// Do not edit this constructor.
	public WifiDirect5() {
	}
}

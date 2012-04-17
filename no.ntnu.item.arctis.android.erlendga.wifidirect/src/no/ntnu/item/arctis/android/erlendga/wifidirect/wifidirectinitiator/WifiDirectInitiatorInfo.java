package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectinitiator;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

public class WifiDirectInitiatorInfo {
	
	public boolean wifiP2pStateEnabled;
	public WifiP2pConfig config;
	public WifiP2pManager manager;
	public Channel channel;
	
}

package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectconnect;

import java.util.ArrayList;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import no.ntnu.item.arctis.runtime.Block;

public class WifiDirectConnect extends Block {

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
	
	public void unwrap(ArrayList<Object> objects) {
		channel = (Channel) objects.get(channelElement);
		config = (WifiP2pConfig) objects.get(configElement);
		isWifiP2pEnabled = (Boolean) objects.get(isWifiP2pEnabledElement);
		manager = (WifiP2pManager) objects.get(managerElement);
	}

	public ArrayList<Object> initConnect() {
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

	public void setIsConnectedEnabled() {
		isConnected = true;
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
}

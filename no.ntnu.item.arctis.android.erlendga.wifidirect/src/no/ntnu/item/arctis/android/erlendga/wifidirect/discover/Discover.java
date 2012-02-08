package no.ntnu.item.arctis.android.erlendga.wifidirect.discover;

import java.util.ArrayList;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import no.ntnu.item.arctis.runtime.Block;

public class Discover extends Block {

	public boolean isWifiP2pEnabled;
	private Channel channel;
	private WifiP2pManager manager;
	
	public void unwrap(ArrayList<Object> objects) {
		channel = (Channel) objects.get(0);
		isWifiP2pEnabled = (Boolean) objects.get(1);
		manager = (WifiP2pManager) objects.get(2);
	}

	public ArrayList<Object> initDiscoverPeers() {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(manager);
		return objects;
	}
}

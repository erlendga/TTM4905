package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication2;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication.WiFiDirectApplicationActivity;
import no.ntnu.item.arctis.runtime.Block;

public class WifiDirectApplication2 extends Block {

	public WiFiDirectApplicationActivity activity;
	private WifiP2pManager manager;
	private Channel channel;
	public boolean wifiP2pStateEnabled;
	public android.net.wifi.p2p.WifiP2pConfig config;

	public Activity getActivity() {
		return activity;
	}

	public void initialize() {
		manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(activity, activity.getMainLooper(), new ChannelListener() {
			
			public void onChannelDisconnected() {
				sendToBlock("ON_CHANNEL_DISCONNECTED");
			}
		});
		activity.setParentID(blockID);
	}

	public ArrayList<Object> wrap() {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(manager);
		return objects;
	}
	
	public ArrayList<Object> wrap2() {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(config);
		objects.add(manager);
		return objects;
	}

	public void wifiP2pStateDisabled() {
	}

	public void discoverPeersSucceeded() {
	}

	public void discoverPeersFailed(int reasonCode) {
	}

	public void reInitialize() {
	}

	public void cancelConnectSucceeded() {
	}

	public void cancelConnectFailed(int reasonCode) {
	}

	public void connectSucceeded() {
	}

	public void connectFailed(int reasonCode) {
	}

	public void channelLost() {
	}

	public void startWirelessSettings() {
	}

}

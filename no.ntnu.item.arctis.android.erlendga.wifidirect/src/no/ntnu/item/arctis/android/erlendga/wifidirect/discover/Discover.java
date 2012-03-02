package no.ntnu.item.arctis.android.erlendga.wifidirect.discover;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import no.ntnu.item.arctis.runtime.Block;

public class Discover extends Block {

	private static final String TAG = "discover";
	public boolean isWifiP2pEnabled;
	private Channel channel;
	private WifiP2pManager manager;
	private BroadcastReceiver receiver;
	
	public void unwrap(ArrayList<Object> objects) {
		Log.d(TAG, "Inside Discovery block. Unwrapping.");
		channel = (Channel) objects.get(0);
		isWifiP2pEnabled = (Boolean) objects.get(1);
		manager = (WifiP2pManager) objects.get(2);
	}

	public ArrayList<Object> initDiscoverPeers() {
		Log.d(TAG, "Initiating a discovery. Going into Discover Peers block...");
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(manager);
		return objects;
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
				if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(intent.getAction())) {
					sendToBlock("WIFI_P2P_PEERS_CHANGED_ACTION");
				}
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		
		getContext().registerReceiver(receiver, filter);
	}
	
	private Context getContext() {
		return (Context) getProperty("Android");
	}

	public void requestPeers() {
		manager.requestPeers(channel, new PeerListListener() {
			
			public void onPeersAvailable(WifiP2pDeviceList deviceList) {
				sendToBlock("ON_PEERS_AVAILABLE", deviceList);
			}
		});
	}

	public void unregisterBroadcastReceiver() {
		getContext().unregisterReceiver(receiver);
	}
}

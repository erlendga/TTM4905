package no.ntnu.item.arctis.android.erlendga.wifidirect.discover;

import no.ntnu.item.arctis.android.erlendga.wifidirect.discoverpeers.DiscoverPeersInfo;
import no.ntnu.item.arctis.runtime.Block;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

public class Discover extends Block {

	public boolean isWifiP2pEnabled;
	private Channel channel;
	private WifiP2pManager manager;
	private BroadcastReceiver receiver;
	
	public void unwrap(DiscoverInfo discoverInfo) {
		channel = discoverInfo.channel;
		isWifiP2pEnabled = discoverInfo.wifiP2pStateEnabled;
		manager = discoverInfo.manager;
	}

	public DiscoverPeersInfo initDiscoverPeers() {
		DiscoverPeersInfo discoverPeersInfo = new DiscoverPeersInfo();
		discoverPeersInfo.channel = channel;
		discoverPeersInfo.manager = manager;
		return discoverPeersInfo;
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

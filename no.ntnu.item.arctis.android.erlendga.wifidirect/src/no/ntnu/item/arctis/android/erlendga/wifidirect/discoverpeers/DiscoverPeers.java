package no.ntnu.item.arctis.android.erlendga.wifidirect.discoverpeers;

import java.util.ArrayList;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import no.ntnu.item.arctis.runtime.Block;

public class DiscoverPeers extends Block {

	private static final String TAG = "discoverpeers";
	private Channel channel;
	private WifiP2pManager manager;

	public void discoverPeers(ArrayList<Object> channelAndManagerList) {
		channel = (Channel) channelAndManagerList.get(0);
		manager = (WifiP2pManager) channelAndManagerList.get(1);
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {		
			public void onSuccess() {
				sendToBlock("ON_SUCCESS");
			}
			
			public void onFailure(int reasonCode) {
				sendToBlock("ON_FAILURE", reasonCode);
			}
		});
	}

}

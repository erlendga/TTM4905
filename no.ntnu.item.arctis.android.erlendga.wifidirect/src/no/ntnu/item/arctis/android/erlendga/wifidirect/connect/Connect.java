package no.ntnu.item.arctis.android.erlendga.wifidirect.connect;

import java.util.ArrayList;

import no.ntnu.item.arctis.runtime.Block;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;

public class Connect extends Block {

	private Channel channel;
	private WifiP2pConfig config;
	private WifiP2pManager manager;

	public void connect(ArrayList<Object> mergedList) {
		channel = (Channel) mergedList.get(0);
		config = (WifiP2pConfig) mergedList.get(1);
		manager = (WifiP2pManager) mergedList.get(2);
		manager.connect(channel, config, new ActionListener() {
			
			public void onSuccess() {
				sendToBlock("ON_SUCCESS");
			}
			
			public void onFailure(int reasonCode) {
				sendToBlock("ON_FAILURE", reasonCode);
			}
		});
	}
}

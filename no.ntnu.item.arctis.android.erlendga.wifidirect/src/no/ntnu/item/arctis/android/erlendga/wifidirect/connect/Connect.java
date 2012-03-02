package no.ntnu.item.arctis.android.erlendga.wifidirect.connect;

import java.util.ArrayList;

import no.ntnu.item.arctis.runtime.Block;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

public class Connect extends Block {

	private static final String TAG = "connect";
	private Channel channel;
	private WifiP2pConfig config;
	private WifiP2pManager manager;
	
	// Instance parameter. Edit only in overview page.
	public final int channelElement;
	// Instance parameter. Edit only in overview page.
	public final int configElement;
	// Instance parameter. Edit only in overview page.
	public final int managerElement;
	
	public void connect(ArrayList<Object> mergedList) {
		Log.d(TAG, "Trying to connect");
		channel = (Channel) mergedList.get(channelElement);
		config = (WifiP2pConfig) mergedList.get(configElement);
		manager = (WifiP2pManager) mergedList.get(managerElement);
		manager.connect(channel, config, new ActionListener() {
			
			public void onSuccess() {
				Log.d(TAG, "Connect successful. Terminating block.");
				sendToBlock("ON_SUCCESS");
			}
			
			public void onFailure(int reasonCode) {
				Log.d(TAG, "Connect failed. Terminating block.");
				sendToBlock("ON_FAILURE", reasonCode);
			}
		});
	}

	// Do not edit this constructor.
	public Connect(int channelElement, int configElement, int managerElement) {
	    this.channelElement = channelElement;
	    this.configElement = configElement;
	    this.managerElement = managerElement;
	}
}

package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectbroadcastreceiver;

import no.ntnu.item.arctis.runtime.Block;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

public class WiFiDirectBroadcastReceiver extends Block {

	private IntentFilter filter;
	private Context context;
	private BroadcastReceiver receiver;

	public void registerReceiver() {
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_STATE_CHANGED", intent);
				}
				else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_PEERS_CHANGED");
				}
				else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_CONNECTION_CHANGED", intent);
				}
				else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_THIS_DEVICE_CHANGED", intent);
				}
			}
		};
		
		context = (Context) getProperty("Android");
		
		filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
		context.registerReceiver(receiver, filter);
	}

	public void unregisterReceiver() {
		context.unregisterReceiver(receiver);
	}

}

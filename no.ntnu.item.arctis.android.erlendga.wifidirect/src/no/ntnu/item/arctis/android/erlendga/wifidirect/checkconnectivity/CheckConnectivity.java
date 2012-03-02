package no.ntnu.item.arctis.android.erlendga.wifidirect.checkconnectivity;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import no.ntnu.item.arctis.runtime.Block;

public class CheckConnectivity extends Block {

	private static final String TAG = "checkconnectivity";

	public void checkConnectivity(Intent intent) {
		NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
		WifiP2pInfo wifiP2pInfo = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
		Log.d(TAG, "Checking connectivity...\n" + networkInfo.toString() + "\n" + wifiP2pInfo.toString());
		if (networkInfo.isConnected()) {
			Log.d(TAG, "CONNECTED. Unregistering receiver and stopping timer.");
			sendToBlock("IS_CONNECTED");
		}
		else {
			Log.d(TAG, "NOT CONNECTED. Unregistering receiver.\nReason: " + networkInfo.getReason());
			sendToBlock("IS_NOT_CONNECTED");
		}
	}

}

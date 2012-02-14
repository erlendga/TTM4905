package no.ntnu.item.arctis.android.erlendga.wifidirect.checkconnectivity;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import no.ntnu.item.arctis.runtime.Block;

public class CheckConnectivity extends Block {

	public void checkConnectivity(Intent intent) {
		NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
		
		if (networkInfo.isConnected()) {
			sendToBlock("IS_CONNECTED");
		}
		else {
			sendToBlock("IS_NOT_CONNECTED");
		}
	}

}

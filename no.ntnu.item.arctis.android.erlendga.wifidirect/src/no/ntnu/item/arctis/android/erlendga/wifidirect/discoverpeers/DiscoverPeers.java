package no.ntnu.item.arctis.android.erlendga.wifidirect.discoverpeers;

import no.ntnu.item.arctis.runtime.Block;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.util.Log;

public class DiscoverPeers extends Block {

	private static final String TAG = "discoverpeers";

	public void discoverPeers(DiscoverPeersInfo discoverPeersInfo) {
		discoverPeersInfo.manager.discoverPeers(discoverPeersInfo.channel, new ActionListener() {		
			public void onSuccess() {
				Log.d(TAG, "Discover peers successful. Terminating block.");
				sendToBlock("ON_SUCCESS");
			}
			
			public void onFailure(int reasonCode) {
				Log.d(TAG, "Disocver peers failed. Terminating block.");
				sendToBlock("ON_FAILURE", reasonCode);
			}
		});
	}

}

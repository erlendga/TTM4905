package no.ntnu.item.arctis.android.erlendga.wifidirect.connect;

import no.ntnu.item.arctis.runtime.Block;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.util.Log;

public class Connect extends Block {

	private static final String TAG = "connect";
	
	public void connect(ConnectInfo connectInfo) {
		connectInfo.manager.connect(connectInfo.channel, connectInfo.config, new ActionListener() {
			
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
	public Connect() {
	}
}

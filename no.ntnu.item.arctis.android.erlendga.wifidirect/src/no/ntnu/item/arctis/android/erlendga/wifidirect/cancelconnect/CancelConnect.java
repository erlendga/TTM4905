package no.ntnu.item.arctis.android.erlendga.wifidirect.cancelconnect;

import no.ntnu.item.arctis.runtime.Block;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;

public class CancelConnect extends Block {
	
	public void cancelConnect(CancelConnectInfo cancelConnectInfo) {
		cancelConnectInfo.manager.cancelConnect(cancelConnectInfo.channel, new ActionListener() {
		
	        public void onSuccess() {
	            sendToBlock("ON_SUCCESS");
	        }
	
	        public void onFailure(int reasonCode) {               	
	            sendToBlock("ON_FAILURE", reasonCode);
	        }
	    });
	}

	// Do not edit this constructor.
	public CancelConnect() {
	}
}

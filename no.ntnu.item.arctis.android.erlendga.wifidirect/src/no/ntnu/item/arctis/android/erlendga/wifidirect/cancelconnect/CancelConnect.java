package no.ntnu.item.arctis.android.erlendga.wifidirect.cancelconnect;

import java.util.ArrayList;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import no.ntnu.item.arctis.runtime.Block;

public class CancelConnect extends Block {

	private Channel channel;
	private WifiP2pManager manager;

	public void cancelConnect(ArrayList<Object> mergeList) {
		channel = (Channel) mergeList.get(0);
		manager = (WifiP2pManager) mergeList.get(1);
		manager.cancelConnect(channel, new ActionListener() {
		
	        public void onSuccess() {
	            sendToBlock("ON_SUCCESS");
	        }
	
	        public void onFailure(int reasonCode) {               	
	            sendToBlock("ON_FAILURE", reasonCode);
	        }
	    });
	}
}

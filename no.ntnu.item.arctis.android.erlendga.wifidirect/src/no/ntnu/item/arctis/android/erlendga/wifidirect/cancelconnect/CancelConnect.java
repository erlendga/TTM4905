package no.ntnu.item.arctis.android.erlendga.wifidirect.cancelconnect;

import java.util.ArrayList;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import no.ntnu.item.arctis.runtime.Block;

public class CancelConnect extends Block {

	private Channel channel;
	private WifiP2pManager manager;
	
	// Instance parameter. Edit only in overview page.
	public final int channelElement;
	// Instance parameter. Edit only in overview page.
	public final int managerElement;
	
	public void cancelConnect(ArrayList<Object> mergeList) {
		channel = (Channel) mergeList.get(channelElement);
		manager = (WifiP2pManager) mergeList.get(managerElement);
		manager.cancelConnect(channel, new ActionListener() {
		
	        public void onSuccess() {
	            sendToBlock("ON_SUCCESS");
	        }
	
	        public void onFailure(int reasonCode) {               	
	            sendToBlock("ON_FAILURE", reasonCode);
	        }
	    });
	}
	
	// Do not edit this constructor.
	public CancelConnect(int channelElement, int managerElement) {
	    this.channelElement = channelElement;
	    this.managerElement = managerElement;
	}
}

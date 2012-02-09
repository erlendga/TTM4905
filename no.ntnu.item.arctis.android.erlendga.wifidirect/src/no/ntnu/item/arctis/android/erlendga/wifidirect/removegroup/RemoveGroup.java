package no.ntnu.item.arctis.android.erlendga.wifidirect.removegroup;

import java.util.ArrayList;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import no.ntnu.item.arctis.runtime.Block;

public class RemoveGroup extends Block {

	private Channel channel;
	private WifiP2pManager manager;
	
	// Instance parameter. Edit only in overview page.
	public final int channelElement;
	// Instance parameter. Edit only in overview page.
	public final int managerElement;
	
	public void removeGroup(ArrayList<Object> objects) {
		channel = (Channel) objects.get(channelElement);
		manager = (WifiP2pManager) objects.get(managerElement);
		manager.removeGroup(channel, new ActionListener() {
					
			public void onSuccess() {
				sendToBlock("ON_SUCCESS");
			}
			
			public void onFailure(int reasonCode) {
				sendToBlock("ON_FAILURE", reasonCode);
			}
		});
	}

	// Do not edit this constructor.
	public RemoveGroup(int channelElement, int managerElement) {
	    this.channelElement = channelElement;
	    this.managerElement = managerElement;
	}

}

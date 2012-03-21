package no.ntnu.item.arctis.android.erlendga.wifidirect.removegroup;

import no.ntnu.item.arctis.runtime.Block;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;

public class RemoveGroup extends Block {
	
	public void removeGroup(RemoveGroupInfo removeGroupInfo) {
		removeGroupInfo.manager.removeGroup(removeGroupInfo.channel, new ActionListener() {
					
			public void onSuccess() {
				sendToBlock("ON_SUCCESS");
			}
			
			public void onFailure(int reasonCode) {
				sendToBlock("ON_FAILURE", reasonCode);
			}
		});
	}

	// Do not edit this constructor.
	public RemoveGroup() {
	}

}

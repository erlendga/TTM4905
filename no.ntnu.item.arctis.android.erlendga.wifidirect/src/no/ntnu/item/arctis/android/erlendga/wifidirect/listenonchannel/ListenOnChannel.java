package no.ntnu.item.arctis.android.erlendga.wifidirect.listenonchannel;

import no.ntnu.item.arctis.runtime.Block;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;

public class ListenOnChannel extends Block implements ChannelListener {

	public android.net.wifi.p2p.WifiP2pManager manager;
	private boolean retryChannel = false;
	
	private Context getContext() {
		return (Context) getProperty("Android");
	}

	public void onChannelDisconnected() {
		if (manager != null && !retryChannel) {
            retryChannel = true;
            manager.initialize(getContext(), getContext().getMainLooper(), this);
            sendToBlock("RESET_DATA");
        } else {
        	sendToBlock("CHANNEL_LOST");
        }
	}

}

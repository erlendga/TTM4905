package no.ntnu.item.arctis.android.erlendga.wifidirect.listener;

import java.net.URI;

import android.net.wifi.p2p.WifiP2pConfig;

public interface DeviceActionListener {

    void cancelConnect();
    
    void cancelDiscoverPeers();

	void connect(WifiP2pConfig config);

	void disconnect();

	void groupInfo();

	void back();

	void takePhoto(URI uri);
}

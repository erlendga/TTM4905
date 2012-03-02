package no.ntnu.item.arctis.android.erlendga.wifidirect.listener;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;

public interface DeviceActionListener {

    void cancelConnect();
    
    void cancelDiscoverPeers();

	void onActivityResultDeviceDetailFragment(Intent intent);

	void connect(WifiP2pConfig config);

	void disconnect();

	void groupInfo();

	void back();
}

package no.ntnu.item.arctis.android.erlendga.wifidirect.devicelistfragment;

import android.net.wifi.p2p.WifiP2pDevice;

public interface DeviceListFragmentListener {
	
	public void deviceSelected(WifiP2pDevice device);
	
}

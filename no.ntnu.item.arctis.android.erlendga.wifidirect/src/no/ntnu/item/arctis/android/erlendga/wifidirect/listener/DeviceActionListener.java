package no.ntnu.item.arctis.android.erlendga.wifidirect.listener;

import java.util.List;

import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceListFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.View;

public interface DeviceActionListener {

    void cancelConnect();
    
    void cancelDiscoverPeers();

	void onCreateViewDeviceDetailFragment(View view, Fragment fragment);

	void onActivityResultDeviceDetailFragment(Intent intent);

	void onActivityCreatedDeviceListFragment(List<WifiP2pDevice> peers);

	void onListItemClickDeviceListFragment(WifiP2pDevice device);

	void getViewWiFiPeerListAdapter(List<WifiP2pDevice> items, int position, View convertView);

	void onCreateViewDeviceListFragment(View view);
}

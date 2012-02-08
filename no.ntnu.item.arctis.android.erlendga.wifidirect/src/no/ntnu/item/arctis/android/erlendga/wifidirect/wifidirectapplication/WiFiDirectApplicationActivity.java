package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.devicelistfragment.DeviceListFragment.DeviceActionListener;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bitreactive.library.android.core.activity.ArctisAndroidActivity;

public class WiFiDirectApplicationActivity extends ArctisAndroidActivity implements DeviceActionListener {
	
	private static final String TAG = "wifidirectapplicationactivity";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
	
	public void onResume() {
		super.onResume();
		Log.d(TAG, "On Resume");
		sendSignalToBuildingBlock("ON_RESUME");
	}
	
	public void onPause() {
		super.onPause();
		Log.d(TAG, "On Pause");
		sendSignalToBuildingBlock("ON_PAUSE");
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_items, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	        case R.id.atn_direct_enable:
	        	sendSignalToBuildingBlock("ENABLE_SELECTED");
	            return true;
	
	        case R.id.atn_direct_discover:
	        	sendSignalToBuildingBlock("DISCOVER_SELECTED");
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	public void showDetails(WifiP2pDevice device) {
		sendSignalToBuildingBlockWithObject("SHOW_DETAILS", device);
	}

	public void cancelConnect() {
		sendSignalToBuildingBlock("CANCEL_CONNECT");
	}

	public void connect(WifiP2pConfig config) {
		sendSignalToBuildingBlockWithObject("CONNECT", config);
	}

	public void disconnect() {
		sendSignalToBuildingBlock("DISCONNECT");
	}

	public void cancelDiscoverPeers() {
		sendSignalToBuildingBlock("CANCEL_DISCOVER_PEERS");
	}
}

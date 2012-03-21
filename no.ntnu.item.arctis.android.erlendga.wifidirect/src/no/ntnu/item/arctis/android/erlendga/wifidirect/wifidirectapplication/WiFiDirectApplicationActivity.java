package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.listener.DeviceActionListener;
import android.app.Fragment;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
		sendSignalToBuildingBlockWithObject("ON_CREATE_OPTIONS_MENU", menu);
		return true;
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem progressBar = menu.getItem(1);
		MenuItem discover = menu.getItem(2);
		if (progressBar.isVisible()) {
			progressBar.setVisible(false);
			discover.setVisible(true);
		}
		else {
			progressBar.setVisible(true);
			discover.setVisible(false);
		}
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

	public void cancelConnect() {
		sendSignalToBuildingBlock("CANCEL_CONNECT");
	}

	public void cancelDiscoverPeers() {
		sendSignalToBuildingBlock("CANCEL_DISCOVER_PEERS");
	}

	public void connect(WifiP2pConfig config) {
		sendSignalToBuildingBlockWithObject("CONNECT", config);
	}

	public void disconnect() {
		sendSignalToBuildingBlock("DISCONNECT");
	}

	public void groupInfo() {
		sendSignalToBuildingBlock("GROUP_INFO");
	}

	public void back() {
		sendSignalToBuildingBlock("BACK");
	}

	public void takePhoto(URI uri) {
		sendSignalToBuildingBlockWithObject("TAKE_PHOTO", uri);
	}
}

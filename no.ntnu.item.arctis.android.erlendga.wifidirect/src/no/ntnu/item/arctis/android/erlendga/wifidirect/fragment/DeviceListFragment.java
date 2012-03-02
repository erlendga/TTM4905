package no.ntnu.item.arctis.android.erlendga.wifidirect.fragment;

import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.R;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListFragment extends ListFragment {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ProgressDialog deviceListProgressDialog = null;
    private View deviceListView = null;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	deviceListView = inflater.inflate(R.layout.device_list, null);
    	return deviceListView;
    }
    
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager().findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
    }
    
    public void clearPeers() {
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }
    
    public View getDeviceListView() {
		return deviceListView;
	}
    
    public ProgressDialog getDeviceListProgressDialog() {
    	return deviceListProgressDialog;
    }
    
    public List<WifiP2pDevice> getPeers() {
    	return peers;
    }
    
    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;

        public WiFiPeerListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	View view = convertView;
        	if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.row_devices, null);
			}
        	WifiP2pDevice device = items.get(position); 
        	if (device != null) {
        		TextView top = (TextView) view.findViewById(R.id.device_name);
				TextView bottom = (TextView) view.findViewById(R.id.device_details);
				if (top != null) {
					top.setText(device.deviceName);
				}
				if (bottom != null) {
					bottom.setText(getDeviceStatus(device.status));
				}
			}
        	return view;
        }
    }

	public void notifyDataSetChanged() {
		((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
	}

	public void updateThisDevice(WifiP2pDevice device) {
		TextView view = (TextView) deviceListView.findViewById(R.id.my_name);
        view.setText(device.deviceName);
        view = (TextView) deviceListView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
	}
	
	public void resetThisDevice() {
		TextView view = (TextView) deviceListView.findViewById(R.id.my_name);
        view.setText("");
        view = (TextView) deviceListView.findViewById(R.id.my_status);
        view.setText("");
	}
}
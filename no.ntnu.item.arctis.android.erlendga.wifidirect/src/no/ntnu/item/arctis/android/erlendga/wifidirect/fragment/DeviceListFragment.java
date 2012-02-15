package no.ntnu.item.arctis.android.erlendga.wifidirect.fragment;

import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.listener.DeviceActionListener;
import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DeviceListFragment extends ListFragment {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private View deviceListView = null;

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));
        ((DeviceActionListener) getActivity()).onActivityCreatedDeviceListFragment(peers);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	deviceListView = inflater.inflate(R.layout.device_list, null);
    	return deviceListView;
    }
    
    public void onListItemClick(ListView l, View v, int position, long id) {
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).onListItemClickDeviceListFragment(device);
    }
    
    public View getDeviceListView() {
		return deviceListView;
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
        	((DeviceActionListener) getActivity()).getViewWiFiPeerListAdapter(view, device);       	
        	return view;
        }
    }

	public void notifyDataSetChanged() {
		((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
	}
}
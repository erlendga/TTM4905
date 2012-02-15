package no.ntnu.item.arctis.android.erlendga.wifidirect.fragment;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.listener.DeviceActionListener;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DeviceDetailFragment extends Fragment {

	private View deviceDetailView = null;
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	deviceDetailView = inflater.inflate(R.layout.device_detail, null);
    	((DeviceActionListener) getActivity()).onCreateViewDeviceDetailFragment(deviceDetailView, this);
    	return deviceDetailView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	((DeviceActionListener) getActivity()).onActivityResultDeviceDetailFragment(data);
    }
    
    public View getDeviceDetailView() {
    	return deviceDetailView;
    }
}

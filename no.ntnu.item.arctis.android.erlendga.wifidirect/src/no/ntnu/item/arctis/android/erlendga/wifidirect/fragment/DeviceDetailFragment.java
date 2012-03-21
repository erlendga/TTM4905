package no.ntnu.item.arctis.android.erlendga.wifidirect.fragment;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.listener.DeviceActionListener;
import no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication.WiFiDirectApplication;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeviceDetailFragment extends Fragment {

	protected static final int TAKE_PHOTO_CODE = 20;
	private View deviceDetailView = null;
	private WifiP2pDevice device;
	private ProgressDialog deviceDetailProgressDialog = null;
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	deviceDetailView = inflater.inflate(R.layout.device_detail, null);
    	deviceDetailView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            

			public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (deviceDetailProgressDialog != null && deviceDetailProgressDialog.isShowing()) {
                    deviceDetailProgressDialog.dismiss();
                }
                deviceDetailProgressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "Connecting to :" + device.deviceAddress, true, true, new OnCancelListener() {
					
					public void onCancel(DialogInterface dialog) {
						((DeviceActionListener) getActivity()).cancelConnect();
					}
				});
                ((DeviceActionListener) getActivity()).connect(config);
            }
        });

        deviceDetailView.findViewById(R.id.btn_disconnect).setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                    	((DeviceActionListener) getActivity()).disconnect();
                    }
        });

//        deviceDetailView.findViewById(R.id.btn_start_client).setOnClickListener(new View.OnClickListener() {
//
//                    public void onClick(View v) {
////                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
////                        intent.setType("image/*");
////                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
//                    	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    	startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
//                    }
//        });
        
        deviceDetailView.findViewById(R.id.btn_group_info).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				((DeviceActionListener) getActivity()).groupInfo();
			}
		});
        
        deviceDetailView.findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				((DeviceActionListener) getActivity()).back();
			}
		});
        
    	return deviceDetailView;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case TAKE_PHOTO_CODE:
//				((DeviceActionListener) getActivity()).takePhoto(data);
				break;
			}
		}
    }
    
    public void resetViews() {
    	deviceDetailView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) deviceDetailView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) deviceDetailView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) deviceDetailView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) deviceDetailView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        deviceDetailView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        deviceDetailView.findViewById(R.id.btn_group_info).setVisibility(View.GONE);
        deviceDetailView.findViewById(R.id.btn_disconnect).setVisibility(View.GONE);
        deviceDetailView.setVisibility(View.GONE);
	}
    
    public void showDetails(final WifiP2pDevice device) {
		this.device = device;
		this.getView().setVisibility(View.VISIBLE);
		TextView view = (TextView) deviceDetailView.findViewById(R.id.device_info);
        view.setText(
        		"Name: " + device.deviceName + "\n" +
        		"MAC address: " + device.deviceAddress + "\n" +
        		"Primary device type: " + device.primaryDeviceType + "\n" +
        		"Secondary device type: " + device.secondaryDeviceType + "\n" +
        		"Status: " + getDeviceStatus(device.status) + "\n" +
        		"Is group owner: " + ((device.isGroupOwner() == true) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n" +
        		"Service discovery capable: " + ((device.isServiceDiscoveryCapable() == true) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n" +
        		"Support WPS display configuration: " + ((device.wpsDisplaySupported() == true) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n" +
        		"Support WPS keypad: " + ((device.wpsKeypadSupported() == true) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)) + "\n" +
        		"Support WPS Push button: " + ((device.wpsPbcSupported() == true) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)));
	}

    public ProgressDialog getDeviceDetailProgressDialog() {
    	return deviceDetailProgressDialog;
    }
    
    
    public View getDeviceDetailView() {
    	return deviceDetailView;
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
}

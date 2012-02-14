package no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceDetailFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceListFragment;
import no.ntnu.item.arctis.runtime.Block;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiDirectApplication extends Block {

	private Channel channel;
	private final static String TAG = "wifidirectapplication";
	public android.net.wifi.p2p.WifiP2pManager manager;
	public no.ntnu.item.arctis.android.erlendga.wifidirect.wifidirectapplication.WiFiDirectApplicationActivity activity;
	public boolean isWifiP2pEnabled = false;
	public boolean wifiP2pPeersTimedOut = false;
	public android.content.Intent wifiP2pConnectionChangedIntent;
	public boolean connected;
	private List<WifiP2pDevice> peers;
	private ProgressDialog progressDialog;
	private View deviceListView = null;
	private WifiP2pDevice device;
	private WifiP2pInfo info;
	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	private DeviceListFragment fragmentList;
	private DeviceDetailFragment fragmentDetails;
	private View progressBar;
	private View deviceDetailView = null;
	
	public void initialize() {
		manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(activity, activity.getMainLooper(), null);
		activity.setParentID(blockID);
		progressBar = (View) activity.findViewById(R.id.searchProgressWrapper);
		activity.runOnUiThread(new Runnable() {

			public void run() {
				progressBar.setVisibility(View.INVISIBLE);
				fragmentList = (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
				fragmentDetails = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
			}
		});
	}
	
	public void toast(final String string) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void setWifiP2pEnabled(Intent intent) {
		int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
		if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
			this.isWifiP2pEnabled = true;
		}
		else {
			this.isWifiP2pEnabled = false;
			resetData();
		}
	}
	
	public void channelLostWarning() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
			}
		});
		resetData();
	}

	private void resetData() {
		if (fragmentList != null) {
			peers.clear();
			activity.runOnUiThread(new Runnable() {
				
				public void run() {
					fragmentList.notifyDataSetChanged();
				}
			});	
		}
		if (fragmentDetails != null) {
			resetViews();
		}
	}
	
	private void resetViews() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				deviceDetailView .findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
		        TextView view = (TextView) deviceDetailView.findViewById(R.id.device_address);
		        view.setText(R.string.empty);
		        view = (TextView) deviceDetailView.findViewById(R.id.device_info);
		        view.setText(R.string.empty);
		        view = (TextView) deviceDetailView.findViewById(R.id.group_owner);
		        view.setText(R.string.empty);
		        view = (TextView) deviceDetailView.findViewById(R.id.status_text);
		        view.setText(R.string.empty);
		        deviceDetailView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
		        fragmentDetails.getView().setVisibility(View.GONE);
			}
		});
	}

	public void requestPeers(final WifiP2pDeviceList peerList) {
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        fragmentList.notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(TAG, "No devices found");
            return;
        }
	}

	public void updateThisDevice(Intent intent) {
		final WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
		this.device = device;
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
		        TextView view = (TextView) deviceListView.findViewById(R.id.my_name);
		        view.setText(device.deviceName);
		        view = (TextView) deviceListView.findViewById(R.id.my_status);
		        view.setText(getDeviceStatus(device.status));
			}
		});
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

	public void showDetails(final WifiP2pDevice device) {
		this.device = device;
		fragmentDetails.getView().setVisibility(View.VISIBLE);
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				TextView view = (TextView) deviceListView.findViewById(R.id.device_address);
		        view.setText(device.deviceAddress);
		        view = (TextView) deviceListView.findViewById(R.id.device_info);
		        view.setText(device.toString());
			}
		});
	}
	
	private String getReason(int reasonCode) {
		String reason = null;
		switch (reasonCode) {
			case WifiP2pManager.ERROR:
				reason = "Internal error";
				break;
			case WifiP2pManager.P2P_UNSUPPORTED:
				reason = "P2P is unsupported on the device";
				break;
			case WifiP2pManager.BUSY:
				reason = "The framework is busy and unable to service the request";
				break;
			default:
				reason = "Unknown error";
				break;
		}
		return reason;
	}

	// Do not edit this constructor.
	public WiFiDirectApplication() {
	}

	public void channelLost() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
			}
		});
	}

	public void startWirelessSettings() {
		if (manager != null && channel != null) {
					activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
		}
		else {
			Log.e(TAG, "channel or manager is null");
		}
	}

	public void onInitiateDiscovery() {
		if (progressBar != null && progressBar.isShown()) {
			progressBar.setVisibility(View.INVISIBLE);
		}
		progressBar.setVisibility(View.VISIBLE);
	}

	public void discoverySuccess() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Discovery Initiated", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void p2pOffWarning() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void discoveryFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Discovery Failed : " + getReason(reasonCode), Toast.LENGTH_LONG).show();
			}
		});
	}

	public void connectFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
					
			public void run() {
				Toast.makeText(activity, "Connect failed. Reason: " + getReason(reasonCode), Toast.LENGTH_LONG).show();
			}
		});
	}

	public void cancelConnectSuccess() {
		resetViews();
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Connection aborted", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void cancelConnectFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Connect abort request failed. Reason: " + getReason(reasonCode), Toast.LENGTH_LONG).show();
			}
		});
	}

	public void wifiP2pPeersChangedTimedOut() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Discovery process timed out", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void dismissDiscoveryProgressDialog() {
//		progressDialog.dismiss();
	}

	public ArrayList<Object> initDiscover() {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(isWifiP2pEnabled);
		objects.add(manager);
		return objects;
	}

	public ArrayList<Object> initWifiDirectConnect(WifiP2pConfig config) {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(config);
		objects.add(isWifiP2pEnabled);
		objects.add(manager);
		return objects;
	}

	public void wifiP2pConnectionChangedTimedOut() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Connection process timed out", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void connectSuccess() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Connection Initiated", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void dismissConnectProgressDialog() {
		progressDialog.dismiss();
	}

	public void disconnectFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Disconnection failed. Reason: " + getReason(reasonCode), Toast.LENGTH_LONG).show();
			}
		});
	}

	public void disconnected() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Disconnected", Toast.LENGTH_SHORT).show();
				resetData();
			}
		});
	}

	public void connected(final WifiP2pInfo info) {
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        fragmentDetails.getView().setVisibility(View.VISIBLE);

        activity.runOnUiThread(new Runnable() {
			
			public void run() {
				// The owner IP is now known.
		        TextView view = (TextView) deviceListView.findViewById(R.id.group_owner);
		        Resources resources = fragmentDetails.getResources();
		        view.setText(resources.getString(R.string.group_owner_text) + ((info.isGroupOwner == true) ? resources.getString(R.string.yes) : resources.getString(R.string.no)));

		        // InetAddress from WifiP2pInfo struct.
		        view = (TextView) deviceListView.findViewById(R.id.device_info);
		        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

		        // After the group negotiation, we assign the group owner as the file
		        // server. The file server is single threaded, single connection server
		        // socket.
		        if (info.groupFormed && info.isGroupOwner) {
		            new FileServerAsyncTask(getActivity(), deviceListView.findViewById(R.id.status_text)).execute();
		        } else if (info.groupFormed) {
		            // The other device acts as the client. In this case, we enable the
		            // get file button.
		            deviceListView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
		            ((TextView) deviceListView.findViewById(R.id.status_text)).setText(resources.getString(R.string.client_text));
		        }

		        // hide the connect button
		        deviceListView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
			}
		});
	}
	
	public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;

        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(TAG, "Server: connection done");
                final File f = new File(Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis() + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);
            }

        }

        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }
	
	public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }
    
    public Activity getActivity() {
    	return activity;
	}

	public void initDeviceListView(View view) {
		deviceListView = view;
	}

	public void initDeviceDetailView(ArrayList<Object> objects) {
		View view = (View) objects.get(0);
		final Fragment fragment = (Fragment) objects.get(1);
		
		deviceDetailView  = view;
		deviceDetailView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "Connecting to :" + device.deviceAddress, true, true);
                sendToBlock("CONNECT", config);
            }
        });

        deviceDetailView.findViewById(R.id.btn_disconnect).setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                    	sendToBlock("DISCONNECT");
                    }
                });

        deviceDetailView.findViewById(R.id.btn_start_client).setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        fragment.startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });
	}

	public void initDeviceListFragment(List<WifiP2pDevice> peers) {
		this.peers = peers;
	}
}
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
import no.ntnu.item.arctis.android.erlendga.wifidirect.filetransferservice.FileTransferService;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceDetailFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceListFragment;
import no.ntnu.item.arctis.runtime.Block;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiDirectApplication extends Block {

	private Channel channel;
	private final static String TAG = "wifidirectapplication";
	public WifiP2pManager manager;
	public WiFiDirectApplicationActivity activity;
	public boolean wifiP2pStateEnabled = false;
	public boolean progressBarVisible = false;
	private BroadcastReceiver receiver;
	public android.view.Menu menu;
	private WifiP2pGroup groupInfo;
	public WifiP2pInfo connectionInfo;
	
	public void initialize() {
		manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(activity, activity.getMainLooper(), new ChannelListener() {
			
			public void onChannelDisconnected() {
				Log.d(TAG, "Channel lost for the first time. Trying again.");
				
				activity.runOnUiThread(new Runnable() {
					
					public void run() {
						Toast.makeText(activity, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
					}
				});
				
				resetData();
				
				manager.initialize(activity, activity.getMainLooper(), new ChannelListener() {
					
					public void onChannelDisconnected() {
						Log.d(TAG, "Channel is probably lost permanently.");
						activity.runOnUiThread(new Runnable() {
							
							public void run() {
								Toast.makeText(activity, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
							}
						});
					}
				});
			}
		});
		
		activity.setParentID(blockID);
	}

	private void resetData() {
		if (getDeviceListFragment() != null) {
			getDeviceListFragment().clearPeers();
		}
		if (getDeviceDetailFragment() != null) {
			getDeviceDetailFragment().resetViews();
		}
	}
	
	private DeviceListFragment getDeviceListFragment() {
		return (DeviceListFragment) activity.getFragmentManager().findFragmentById(R.id.frag_list);
	}
	
	

	public void peersAvailable(final WifiP2pDeviceList peerList) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				List<WifiP2pDevice> peers = getDeviceListFragment().getPeers();
				peers.clear();
				peers.addAll(peerList.getDeviceList());
				getDeviceListFragment().notifyDataSetChanged();
		        if (peers.size() == 0) {
		            getDeviceDetailFragment().resetViews();
		        } 
			}
		});
		
	}

	public void updateThisDevice(final WifiP2pDevice device) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (!wifiP2pStateEnabled) {
					getDeviceListFragment().resetThisDevice();
				}
				else getDeviceListFragment().updateThisDevice(device);
			}
		});
	}
	
	
    
    private DeviceDetailFragment getDeviceDetailFragment() {
		return (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
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

	public void startWirelessSettings() {
		if (manager != null && channel != null) {
					activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
		}
		else {
			Log.e(TAG, "channel or manager is null");
		}
	}

	public void discoverySuccess() {
		showProgressBar();
	}

	public void p2pOffWarning() {
		hideProgressBar();
		createAlertDialog("Enable P2P from action bar button above or system settings");
	}

	public void discoveryFailed(final int reasonCode) {
		createAlertDialog("Discover Peers Failed. Reason: " + getReason(reasonCode));
	}

	public void connectFailed(final int reasonCode) {
		createAlertDialog("Connect failed. Reason: " + getReason(reasonCode));
	}
	
	private void createAlertDialog(final String message) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Builder builder = new Builder(activity);
				builder.setMessage(message).setCancelable(false).setPositiveButton("OK", null);
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		});
	}
	
	private void hideProgressBar() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (progressBarVisible) {
					activity.onPrepareOptionsMenu(menu);
					progressBarVisible = false;
				}
			}
		});
	}
	
	private void showProgressBar() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (!progressBarVisible) {
					activity.onPrepareOptionsMenu(menu);
					progressBarVisible = true;
				}
			}
		});
	}

	public void cancelConnectSuccess() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				getDeviceDetailFragment().resetViews();
				Toast.makeText(activity, "Connection aborted", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void cancelConnectFailed(final int reasonCode) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Connect connect request failed. Reason: " + getReason(reasonCode), Toast.LENGTH_LONG).show();
			}
		});
	}

	public ArrayList<Object> initDiscover() {
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(wifiP2pStateEnabled);
		objects.add(manager);
		return objects;
	}

	public ArrayList<Object> initWifiDirectConnect(WifiP2pConfig config) {
		Log.d(TAG, "Connect button is clicked. Starting Wifi Direct Connect block...");
		ArrayList<Object> objects = new ArrayList<Object>();
		objects.add(channel);
		objects.add(config);
		objects.add(wifiP2pStateEnabled);
		objects.add(manager);
		return objects;
	}

	public void connectSuccess() {
		ProgressDialog progressDialog = getDeviceDetailFragment().getDeviceDetailProgressDialog();
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
		hideProgressBar();
	}

	public void removeGroupFailed(final int reasonCode) {
		createAlertDialog("Disconnection failed. Reason: " + getReason(reasonCode));
	}

	public void notConnected() {
		Log.d(TAG, "Not connected");
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Toast.makeText(activity, "Disconnected", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void updateConnectionInfo(final WifiP2pInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        hideProgressBar();
        activity.runOnUiThread(new Runnable() {
			
			public void run() {
				getDeviceDetailFragment().getView().setVisibility(View.VISIBLE);
				
				// show the disconnect button
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_disconnect).setVisibility(View.VISIBLE);
				
				// The owner IP is now known.
		        TextView view = (TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.group_owner);
		        Resources resources = getDeviceDetailFragment().getResources();
//		        view.setText(resources.getString(R.string.group_owner_text) + ((info.isGroupOwner == true) ? resources.getString(R.string.yes) : resources.getString(R.string.no)));

		        // InetAddress from WifiP2pInfo struct.
		        view = (TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.device_info);
		        view.setText("Group Owner IP - " + connectionInfo.groupOwnerAddress.getHostAddress());

		        // After the group negotiation, we assign the group owner as the file server.
		        // The file server is single threaded, single connection server socket.
		        if (connectionInfo.groupFormed && connectionInfo.isGroupOwner) {
		        	getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_group_info).setVisibility(View.VISIBLE);
		            new FileServerAsyncTask(getActivity(), getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.status_text)).execute();
		        } 
		        else if (connectionInfo.groupFormed) {
		            // The other device acts as the client. In this case, we enable the get file button.
		            getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
		            getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_group_info).setVisibility(View.VISIBLE);
		            
		            ((TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.status_text)).setText(resources.getString(R.string.client_text));
		            
		        }

		        // hide the connect button
		        getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_connect).setVisibility(View.GONE);
		        getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_back).setVisibility(View.GONE);
			}
		});
	}
	
	public void updateGroupInfo(WifiP2pGroup groupInfo) {
		this.groupInfo = groupInfo;
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

	public void transfer(final Intent intent) {
        activity.runOnUiThread(new Runnable() {
			
			public void run() {
				Uri uri = intent.getData();
		        TextView statusText = (TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.status_text);
		        statusText.setText("Sending: " + uri);
		        Log.d(TAG, "Intent----------- " + uri);
		        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
		        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
		        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
		        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, connectionInfo.groupOwnerAddress.getHostAddress());
		        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
		        getActivity().startService(serviceIntent);
			}
		});
	}

	public void removeGroupSuccess() {
		Log.d(TAG, "Disconnect success");
	}

	public void registerBroadcastReceiver() {
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				
				if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_STATE_CHANGED_ACTION", intent);
				}
				else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION", intent);
				}
				else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
					sendToBlock("WIFI_P2P_CONNECTION_CHANGED_ACTION", intent);
				}
			}
		};
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		
		getContext().registerReceiver(receiver, filter);
	}
	
	private Context getContext() {
		return (Context) getProperty("Android");
	}

	public boolean isWifiP2pStateEnabled(Intent intent) {
		int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
		if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
			return true;
		}
		else return false;
	}

	public WifiP2pDevice getWifiP2pDevice(Intent intent) {
		return (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
	}

	public void unregisterBroadcastReceiver() {
		getContext().unregisterReceiver(receiver);
	}

	public void checkConnectivity(Intent intent) {
		NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
		WifiP2pInfo wifiP2pInfo = (WifiP2pInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
		Log.d("broadcastreceiver2", "Checking connectivity...\n" + networkInfo.toString() + "\n" + wifiP2pInfo.toString());
		if (networkInfo.isConnected()) {
			manager.requestConnectionInfo(channel, new ConnectionInfoListener() {
				
				public void onConnectionInfoAvailable(WifiP2pInfo connectionInfo) {
					sendToBlock("ON_CONNECTION_INFO_AVAILABLE", connectionInfo);
				}
			});
			manager.requestGroupInfo(channel, new GroupInfoListener() {
				
				public void onGroupInfoAvailable(WifiP2pGroup groupInfo) {
					sendToBlock("ON_GROUP_INFO_AVAILABLE", groupInfo);
				}
			});
		}
		else sendToBlock("NOT_CONNECTED");
	}

	public void showGroupInfo() {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_disconnect).setVisibility(View.GONE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_group_info).setVisibility(View.GONE);
				getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.btn_start_client).setVisibility(View.GONE);
				TextView view = (TextView) getDeviceDetailFragment().getDeviceDetailView().findViewById(R.id.device_info);
				view.setText(
						"\nInterface Name: " + groupInfo.getInterface() + "\n" +
						"SSID: " + groupInfo.getNetworkName() + "\n" +
						"Group Owner IP Address: " + connectionInfo.groupOwnerAddress.getHostAddress() + "\n" +	
						"Is Group Owner: " + ((groupInfo.isGroupOwner() == true) ? getDeviceDetailFragment().getResources().getString(R.string.yes) : getDeviceDetailFragment().getResources().getString(R.string.no)) + "\n");
				
				if (groupInfo.isGroupOwner()) {
					view.append("Clients in Group:\n");
					for (WifiP2pDevice wifiP2pDevice : groupInfo.getClientList()) {
						view.append(wifiP2pDevice.deviceAddress + "\n");
					}
					view.append("\nPassfrase: " + groupInfo.getPassphrase() + "\n");
				}
			}
		});
	}
}
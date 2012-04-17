package no.ntnu.item.arctis.android.erlendga.wifidirect.remotecameraservice;

import java.net.URI;

import com.google.gson.Gson;

import no.ntnu.item.arctis.android.R;
import no.ntnu.item.arctis.android.erlendga.wifidirect.filetransferservice.FileTransferServiceInfo;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.CameraFragment;
import no.ntnu.item.arctis.android.erlendga.wifidirect.fragment.DeviceDetailFragment;
import no.ntnu.item.arctis.examples.realtransmissions.Message;
import no.ntnu.item.arctis.runtime.Block;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class RemoteCameraService extends Block {

	private static final int INITIAL = 0;
	private static final int START_CAMERA = 1;
	private static final int SWITCH_CAMERA = 2;
	private static final int TAKE_PHOTO = 3;
	
	private boolean cameraOn = false;
	public int port = 8988;
	public WifiP2pInfo connectionInfo;
	private Activity activity;
	private String senderIP;
	private String message;
	private DeviceDetailFragment deviceDetailFragment;
	private TextView deviceDetailTextView;
	private Resources deviceDetailFragmentResources;
	private Button cameraButton;
	private Button takePhotoButton;
	private Switch cameraSwitch;
	private View cameraView;
	private CameraFragment cameraFragment;
	private View deviceDetailView;
	public static final String MESSAGE_SUFFIX = "</v:Envelope>";

	public void groupOwner(WifiP2pInfo connectionInfo) {
//		connectionInfo = remCamSerInfo.connectionInfo;
		this.connectionInfo = connectionInfo;
		
		activity.runOnUiThread(new Runnable() {

			public void run() {
				deviceDetailTextView.setText(deviceDetailFragmentResources.getString(R.string.server_text));
			        				        		
				cameraButton.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {									
						if (!cameraOn) {
							cameraOn = true;
							cameraButton.setText("Close Camera");
							takePhotoButton.setVisibility(View.VISIBLE);
							cameraSwitch.setVisibility(View.VISIBLE);
						}
						else {
							cameraOn = false;
							cameraButton.setText("Open Camera");
							takePhotoButton.setVisibility(View.GONE);
							cameraSwitch.setVisibility(View.GONE);
						}
						
						sendToBlock("SEND_MESSAGE", START_CAMERA);
					}
				});
				
				takePhotoButton.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						sendToBlock("SEND_MESSAGE", TAKE_PHOTO);
					}
				});
		
				cameraSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {	
		
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						sendToBlock("SEND_MESSAGE", SWITCH_CAMERA);
					}
				});
			}
		});
	}

	public void groupClient(WifiP2pInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				deviceDetailTextView.setText(deviceDetailFragmentResources.getString(R.string.client_text));
	            sendToBlock("SEND_MESSAGE", INITIAL);
			}
		});
	}

	public Message serialize(int prefix) {
		Message message = new Message();
		message.receiverPort = port;
		Gson gson = new Gson();
		
		String json = null;
		
		switch (prefix) {
			case INITIAL:
				message.receiverIP = connectionInfo.groupOwnerAddress.getHostAddress();		
				json = Integer.toString(INITIAL);
				break;
			case START_CAMERA:
				message.receiverIP = senderIP;				
				json = Integer.toString(START_CAMERA) + gson.toJson(cameraOn);
				break;
			case SWITCH_CAMERA:
				message.receiverIP = senderIP;
				json = Integer.toString(SWITCH_CAMERA);
				break;
			case TAKE_PHOTO:
				message.receiverIP = senderIP;
				json = Integer.toString(TAKE_PHOTO);
				break;
		}
		message.message = json;
		return message;
	}

	public FileTransferServiceInfo transfer(URI uri) {
		FileTransferServiceInfo info = new FileTransferServiceInfo();
		info.URIFilePath = uri.toString();
		info.receiverIP = senderIP;
		return info;
	}

	public void extract(Message message) {
		this.senderIP = message.senderIP;
		if (message.message.contains(MESSAGE_SUFFIX))
			this.message = message.message.substring(0, message.message.length() - MESSAGE_SUFFIX.length());
		else 
			this.message = message.message;
	}

	public void deserialize() {
		Gson gson = new Gson();
		
		if (message.startsWith(Integer.toString(INITIAL))) {
//			clientIpReceived = true;
			sendToBlock("CLIENT_IP_RECEIVED");
			activity.runOnUiThread(new Runnable() {
				
				public void run() {
					cameraButton.setVisibility(View.VISIBLE);
				}
			});	
		}
		else if (message.startsWith(Integer.toString(START_CAMERA))) {
			if (!message.contentEquals(Integer.toString(START_CAMERA))) {
				message = message.substring(Integer.toString(START_CAMERA).length(), message.length());
				boolean cameraOn = gson.fromJson(message, boolean.class);
				sendToBlock("START_CAMERA", cameraOn);
			}
		}
		else if (message.startsWith(Integer.toString(SWITCH_CAMERA)))
			sendToBlock("SWITCH_CAMERA");
		else if (message.startsWith(Integer.toString(TAKE_PHOTO)))
			sendToBlock("TAKE_PHOTO");
	}

	public void initialize(final Activity activity) {
		this.activity = activity;
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				deviceDetailFragment = (DeviceDetailFragment) activity.getFragmentManager().findFragmentById(R.id.frag_detail);
				deviceDetailTextView = ((TextView) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.status_text));
				deviceDetailFragmentResources = deviceDetailFragment.getResources();
				cameraButton = (Button) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.btn_start_client);
				takePhotoButton = (Button) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.btn_take_picture);
				cameraSwitch = (Switch) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.swich_camera_switch);
				cameraFragment = (CameraFragment) activity.getFragmentManager().findFragmentById(R.id.camera);
				cameraView = (View) cameraFragment.getCameraView().findViewById(R.id.camera);
				deviceDetailView = (View) deviceDetailFragment.getDeviceDetailView().findViewById(R.id.frag_detail);
			}
		});
	}

	public void startCamera(final boolean cameraOn) {
		activity.runOnUiThread(new Runnable() {
			
			public void run() {
				if (cameraOn) {
					cameraView.setVisibility(View.VISIBLE);
					deviceDetailView.setVisibility(View.GONE);
				}
				else {
					cameraView.setVisibility(View.GONE);
					deviceDetailView.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	public void switchCamera() {
		activity.runOnUiThread(new Runnable() {
		
			public void run() {
				cameraFragment.switchCamera();
			}
		});
	}

	public void takePhoto() {
		activity.runOnUiThread(new Runnable() {

			public void run() {
				cameraFragment.takePicture();
			}
		});
	}

	public void viewImage(String result) {
		if (result != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + result), "image/*");
			activity.startActivity(intent);
		}
	}
}

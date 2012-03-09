package no.ntnu.item.arctis.android.erlendga.wifidirect.groupclient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.Gson;

import no.ntnu.item.arctis.android.erlendga.wifidirect.groupowner.GroupOwnerInfo;
import no.ntnu.item.arctis.examples.realtransmissions.Message;
import no.ntnu.item.arctis.runtime.Block;

public class GroupClient extends Block {

	public int port;
	private String senderIP;
	private int senderPort;
	private String receiverIP;
	private int receiverPort;
	private String message;
	public static final String MESSAGE_SUFFIX = "</v:Envelope>";
	private static final String TAG = "groupclient";

	// Do not edit this constructor.
	public GroupClient(int port) {
	    this.port = port;
	}
	
	private Context getContext() {
		return (Context) getProperty("Android");
	}

	public Message createMessage(GroupOwnerInfo groupOwnerInfo) {
		Message message = new Message();
		message.receiverIP = groupOwnerInfo.ipAddress;
		message.receiverPort = groupOwnerInfo.port;
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		Gson gson = new Gson();
		String json = gson.toJson(intent);
		
		message.message = json;
		
		return message;
	}

	public void extract(Message message) {
		this.senderIP = message.senderIP;
		this.senderPort = message.senderPort;
		this.receiverIP = message.receiverIP;
		this.receiverPort = message.receiverPort;
		if (message.message.contains(MESSAGE_SUFFIX)) {
			this.message = message.message.substring(0, message.message.length() - MESSAGE_SUFFIX.length());
		}
		else this.message = message.message;
		
		
	}

	public File deserialize() {
		Gson gson = new Gson();
        String json = message;
        File image = gson.fromJson(json, File.class);
        return image;
	}

	public String createFile(File file) {
//		final File f = new File(Environment.getExternalStorageDirectory() + "/" + getContext().getPackageName() + "/wifip2pshared-" + System.currentTimeMillis() + ".jpg");
	
		Uri uri = Uri.fromFile(file);
	    File f = null;
	    try {
	    	f = new File(new URI(uri.toString()));
		    
		    File dirs = new File(f.getParent());
		    if (!dirs.exists())
		        dirs.mkdirs();
			f.createNewFile();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
		}
	    
//      
//      Log.d(TAG, "server: copying files " + f.toString());
//      InputStream inputstream = groupOwnerSoccet.getInputStream();
//      copyFile(inputstream, new FileOutputStream(f));
//
//      serverSocket.close();
      return f.getAbsolutePath();
	}

}

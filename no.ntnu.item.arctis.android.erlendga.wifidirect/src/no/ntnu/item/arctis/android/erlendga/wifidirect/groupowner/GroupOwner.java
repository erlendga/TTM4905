package no.ntnu.item.arctis.android.erlendga.wifidirect.groupowner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.Gson;

import no.ntnu.item.arctis.android.erlendga.wifidirect.groupclient.GroupClientInfo;
import no.ntnu.item.arctis.examples.realtransmissions.Message;
import no.ntnu.item.arctis.runtime.Block;

public class GroupOwner extends Block {

	private static final String TAG = "groupowner";
	public final int port;
	private int senderPort;
	private String message;
	private String receiverIP;
	private int receiverPort;
	private String senderIP;
	private File tempFile;
	public static final String MESSAGE_SUFFIX = "</v:Envelope>";

	// Do not edit this constructor.
	public GroupOwner(int port) {
	    this.port = port;
	}
	
	private Context getContext() {
		return (Context) getProperty("Android");
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
	
	public Intent deserialize() {
		Gson gson = new Gson();
        String json = message;
        Intent intent = gson.fromJson(json, Intent.class);
        return intent;
	}

	public GroupClientInfo createTempFile(Intent intent) {
		final File path = new File(Environment.getExternalStorageDirectory(), getContext().getPackageName());
		if (!path.exists()) {
			path.mkdir();
		}
		
		GroupClientInfo groupClient = new GroupClientInfo();

		try {
			tempFile = File.createTempFile("image", null, path);
			
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
			
			
			groupClient.intent = intent;
			groupClient.tempFile = tempFile;
			groupClient.ipAddress = senderIP;
			
		} catch (IOException ioe) {
			Log.e(TAG, ioe.getMessage());
		}
		return groupClient;
	}

	public Message createMessage() {
		Uri uri = Uri.fromFile(tempFile);
		Message message = null;
		try {
			File image = new File(new URI(uri.toString()));
			
			Gson gson = new Gson();
			String json = gson.toJson(image);
			
			message = new Message();
			message.message = json;
			message.receiverIP = this.senderIP;
			message.receiverPort = this.senderPort;
			
		} catch (URISyntaxException e) {
			Log.e(TAG, e.getMessage());
		}
		return message;
	}

}

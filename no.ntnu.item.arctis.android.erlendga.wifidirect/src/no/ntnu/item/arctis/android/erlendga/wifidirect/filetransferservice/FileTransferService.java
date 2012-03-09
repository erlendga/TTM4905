package no.ntnu.item.arctis.android.erlendga.wifidirect.filetransferservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import no.ntnu.item.arctis.runtime.Block;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class FileTransferService extends Block {

	private static final String TAG = "filetransferservice";
	// Instance parameter. Edit only in overview page.
	public final int socketTimeout;
	// Instance parameter. Edit only in overview page.
	public final java.lang.String filename;
	// Instance parameter. Edit only in overview page.
	public final java.lang.String postfix;
	// Instance parameter. Edit only in overview page.
	public final int port;
	
	private Handler handler = new Handler(Looper.getMainLooper());
	private AsyncTask<Void, Void, String> asyncTask;
	
	public void transfer(FileTransferServiceInfo info) {
		Socket socket = new Socket();
		try {
			socket.bind(null);
			socket.connect((new InetSocketAddress(info.receiverIP, port)), socketTimeout);
			OutputStream stream = socket.getOutputStream();
			ContentResolver cr = getContext().getContentResolver();
			InputStream is = null;
			try {
				is = cr.openInputStream(Uri.parse(info.filePath));
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
				sendToBlock("EXCEPTION");
			}
			boolean success = copyFile(is, stream);
			if (success) {
				sendToBlock("SUCCESS");
			}	
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			sendToBlock("EXCEPTION");
		} finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendToBlock("EXCEPTION");
                    }
                }
            }
        }
	}

	private Context getContext() {
		return (Context) getProperty("Android");
	}
	
	private static boolean copyFile(InputStream inputStream, OutputStream out) {
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

	public void listen() {
		handler.post(new Runnable() {

			public void run() {
				asyncTask = new FileServerAsyncTask(getContext(), port, filename, postfix, new FileTransferServiceListener() {
					
					public void onPostExecute(String result) {
						sendToBlock("RECEIVED", result);
					}

					public void onCancelled() {
						sendToBlock("CANCELLED");
					}
				}).execute();
			}
		});
	}
	
	 public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
		private int port;
		private FileTransferServiceListener listener;
		private String filename;
		private String postfix;
        
        public FileServerAsyncTask(Context context, int port, String filename, String postfix, FileTransferServiceListener listener) {
            this.context = context;
            this.port = port;
            this.listener = listener;
            this.filename = filename;
            this.postfix = postfix;
        }

        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                Socket client = serverSocket.accept();
                final File f = new File(Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/" + filename + System.currentTimeMillis() + "." + postfix);

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

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
        	listener.onPostExecute(result);
        }

        protected void onPreExecute() {
//            statusText.setText("Opening a server socket");
        }
        
        protected void onCancelled() {
        	listener.onCancelled();
        }

    }

	// Do not edit this constructor.
	public FileTransferService(int socketTimeout, java.lang.String filename, java.lang.String postfix, int port) {
	    this.socketTimeout = socketTimeout;
	    this.filename = filename;
	    this.postfix = postfix;
	    this.port = port;
	}

	public void cancel() {
		asyncTask.cancel(false);
	}
}
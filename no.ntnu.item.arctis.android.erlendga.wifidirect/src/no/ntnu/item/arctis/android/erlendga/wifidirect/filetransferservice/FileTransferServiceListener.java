package no.ntnu.item.arctis.android.erlendga.wifidirect.filetransferservice;

public interface FileTransferServiceListener {
	
	public void onPostExecute(String result);

	public void onCancelled();
}

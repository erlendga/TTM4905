package no.ntnu.item.arctis.android.erlendga.wifidirect.listener;

import no.ntnu.item.arctis.android.erlendga.wifidirect.groupclient.GroupClientInfo;

public interface AsyncTaskCompleteListener<T> {
	
	public void onServerAsyncTaskComplete(GroupClientInfo result);

}

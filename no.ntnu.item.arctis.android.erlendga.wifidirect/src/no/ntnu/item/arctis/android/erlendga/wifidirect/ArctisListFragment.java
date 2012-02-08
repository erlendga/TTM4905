package no.ntnu.item.arctis.android.erlendga.wifidirect;

import no.ntnu.item.arctis.runtime.AbstractRuntime;
import no.ntnu.item.arctis.runtime.Block;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;

public class ArctisListFragment extends ListFragment {
	
	private static String LOG_TAG = ArctisListFragment.class.getSimpleName();
	
	public static final String BLOCK_ID_EXTRA_KEY = "BLOCK_ID";
	public static final String SESSION_ID_EXTRA_KEY = "SESSION_ID";
	public static final String LAYOUT_ID_EXTRA_KEY = "LAYOUT_ID";
	
	private String blockID;
	private String sessionID;
	
	public ArctisListFragment() {
		Log.v(LOG_TAG, " +++++ observed CONSTRUCTOR " + this.getClass().getSimpleName() + " #" + this.hashCode());
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		
		if(/*sessionID==null || */ AbstractRuntime.getRuntime()==null || (sessionID!=null && !AbstractRuntime.getRuntime().isSessionActive(sessionID))) {
			//Logger.getLogger().applicationError("onResume, but session " + sessionID + " seems inactive. Finishing activity " + this.getClass().getCanonicalName(), null);
			blockID = null;
			super.onActivityCreated(savedInstanceState);
			super.getActivity().finish();
			return;
		}
		try {
			super.onActivityCreated(savedInstanceState);
			blockID = getActivity().getIntent().getStringExtra(BLOCK_ID_EXTRA_KEY);
			sessionID = getActivity().getIntent().getStringExtra(SESSION_ID_EXTRA_KEY);
			AbstractRuntime.getRuntime().sendToBlock(sessionID, blockID, "onActivityCreated_" + blockID, this);
		} catch (Exception e) {
			Block.logException("Error in onActivityCreated()", e);
		}
	}
}

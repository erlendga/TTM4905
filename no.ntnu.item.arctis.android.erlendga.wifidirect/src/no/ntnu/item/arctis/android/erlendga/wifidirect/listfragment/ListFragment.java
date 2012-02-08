package no.ntnu.item.arctis.android.erlendga.wifidirect.listfragment;

import android.content.Context;
import android.content.Intent;
import no.ntnu.item.arctis.runtime.Block;

public class ListFragment<ListFragmentClass extends android.app.ListFragment> extends Block {

	public static final String KEY_CURRENT_CONTEXT = "CurrentActivity";
	public static final String SESSION_ID_EXTRA_KEY = "SESSION_ID";
	public static final String BLOCK_ID_EXTRA_KEY = "BLOCK_ID";
	
	public ListFragmentClass listFragment;
	// Instance parameter. Edit only in overview page.
	public final java.lang.String listFragmentClass;
	// Do not edit this constructor.
	public ListFragment(java.lang.String listFragmentClass) {
	    this.listFragmentClass = listFragmentClass;
	}
	public void startListFragment() {
		Class listFragmentClassToInstantiate = null;
		try {
			listFragmentClassToInstantiate = Class.forName(listFragmentClass);
		} catch (ClassNotFoundException e) {
			log("Could not get a class for the list fragment class " + listFragmentClass + ". Make sure the instance parameter is set to the fully qualified name of the activity class.", e);
			return;
		}
		
		final Intent intent = new Intent(getContext(), listFragmentClassToInstantiate);
		intent.putExtra(BLOCK_ID_EXTRA_KEY, blockID);
		intent.putExtra(SESSION_ID_EXTRA_KEY, sessionID);
		
		System.err.println(" +++++ starting intent to create list fragment of type " + listFragmentClassToInstantiate.getSimpleName());
		System.err.println("       sessionID: " + sessionID);
		System.err.println("         blockID: " + blockID);
		
		if(getCurrentActivityContext() instanceof android.app.Activity) {
			System.err.println("       --> starting via activity context " + getCurrentActivityContext().getClass().getCanonicalName());
			final android.app.Activity parent = (android.app.Activity) getCurrentActivityContext();
			parent.runOnUiThread(new Runnable() {
				public void run() {
					parent.startActivity(intent);
					System.err.println("       --> started from within runnable "); 
				}
			});
			
			
		} else {
			String wrapping = (String) getProperty("wrapping");
			if(wrapping!=null && wrapping.equalsIgnoreCase("service")) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				System.err.println("       --> setting flag NEW_TASK ");
			}
			System.err.println("       --> starting via service context ");
			getContext().startActivity(intent);
		}
	}
	
	private Context getContext() {
		return (Context) getProperty("Android");
	}
	
	public Context getCurrentActivityContext() {
		if(hasProperty(KEY_CURRENT_CONTEXT)) {
			return (Context) getProperty(KEY_CURRENT_CONTEXT);
		}
		return null;
	}
	public ListFragmentClass foo(ListFragmentClass fragment) {
		return fragment;
	}
}

package edu.cmu.sv.geocamdtn;

import java.util.Set;

import edu.cmu.sv.geocamdtn.lib.Constants;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class GeoCamDTNService extends IntentService {
	
	private static final String TAG = "edu.cmu.sv.geocamdtn.GeoCamDTNService";
	
	public GeoCamDTNService() {
		super("GeoCamDTNService");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle data = intent.getBundleExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
		Set<String> keys = data.keySet();
		String randomKey = (String)keys.toArray()[0];
		String value = data.getStringArray(randomKey)[0];
		String msg = "Handling intent: " + value;
		Log.d(TAG, msg);
		
		// Need to send the toast to the main thread, since this is an intentservice
		// this method is running on a background thread
		
		//Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		//toast.show();
		
	    /*  long endTime = System.currentTimeMillis() + 5*1000;
	      while (System.currentTimeMillis() < endTime) {
	          synchronized (this) {
	              try {
	                  wait(endTime - System.currentTimeMillis());
	              } catch (Exception e) {
	              }
	          }
	      }*/
	}
}

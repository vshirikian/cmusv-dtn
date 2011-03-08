package edu.cmu.sv.geocamdtn;

import edu.cmu.sv.geocamdtn.lib.Constants;
import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

public class GeoCamDTNService extends IntentService {
	
	public GeoCamDTNService() {
		super("GeoCamDTNService");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String[] data = intent.getStringArrayExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
		String msg = "Handling intent: " + data[0];
		System.out.println(msg);
		
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		toast.show();
		
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

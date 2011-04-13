package edu.cmu.sv.geocamdtn;

import edu.cmu.sv.geocamdtn.lib.Constants;
import edu.cmu.sv.geocamdtn.lib.MimeEncoder;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class GeoCamDTNService extends IntentService {
	
	/**
	 * Logging tag for supporting Android logging mechanism
	 */
	private static final String TAG = "edu.cmu.sv.geocamdtn.GeoCamDTNService";
	
	public GeoCamDTNService() {
		super("GeoCamDTNService");
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * The extra data for an incoming intent should contain String arrays
	 * whose keys map to the params for a multi-part MIME message.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		
		
		Bundle data = intent.getBundleExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
		Iterator<String> iter = data.keySet().iterator();
		
		Map<String, String> mimeData = new HashMap<String, String>();
		File file = null;
		Log.i(TAG, "About to mime encode intent bundle");
		while (iter.hasNext()) {
			String key = iter.next();
			if (key.equalsIgnoreCase(Constants.FILE_KEY)) {
				file = (File) data.getSerializable(key);
			} else {
				mimeData.put(key, data.getString(key));
			}
		}
			
		byte[] bundlePayload = MimeEncoder.toMime(mimeData, file);
		
		// Send intent to DTNMediatorService
		Bundle mediatorData = new Bundle();
		mediatorData.putByteArray(Constants.DTN_PAYLOAD_KEY, bundlePayload);
		mediatorData.putString(Constants.DTN_DEST_EID_KEY, Constants.STATIC_GATEWAY_EID);
		mediatorData.putInt(Constants.DTN_EXPIRATION_KEY, Constants.BUNDLE_EXPIRATION);
		
		Intent dtnMediatorIntent = new Intent(Constants.ACTION_SEND_DTN_BUNDLE);
		dtnMediatorIntent.putExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD, mediatorData);
		this.startService(dtnMediatorIntent);
	}
}

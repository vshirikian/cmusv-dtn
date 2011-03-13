package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPIBinder;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_api_status_report_code;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_bundle_payload_location_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_bundle_priority_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundlePayload;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleSpec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNEndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNHandle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;

import edu.cmu.sv.geocamdtn.lib.Constants;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class DTNMediatorService extends Service {
	/**
	 * Logging tag for supporting Android logging mechanism
	 */
	private static final String TAG = "edu.cmu.sv.geocamdtn.DTNMediatorService";
	
	private boolean isBound = false;

	/**
	 * The service connection to communicate with DTNService 
	 */
	private ServiceConnection conn;
	
	/**
	 * DTNAPIBinder object
	 */
	private DTNAPIBinder dtn_api_binder;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Creating mediator");
		isBound = false;
		bindDTNService();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unbindDTNService();
	}

	/**
	 * Unbind the DTNService to free resource consumed by the binding
	 */
	private void unbindDTNService()
	{
		unbindService(conn);
	}
	
	/**
	 * bind the DTNService to use the API later
	 */
	private void bindDTNService() {
		Log.d(TAG, "Attempting to bind dtn service.");
		conn = new ServiceConnection() {
			public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
				Log.i(TAG, "DTN Service is bound");
				dtn_api_binder = (DTNAPIBinder) ibinder;
				isBound = true;
			}

			public void onServiceDisconnected(ComponentName arg0) {
				Log.i(TAG, "DTN Service is Unbound");
				dtn_api_binder = null;
			}

		};

		Intent i = new Intent(this, se.kth.ssvl.tslab.bytewalla.androiddtn.DTNService.class);
		Log.d(TAG, "About to bind service");
		bindService(i, conn, BIND_AUTO_CREATE);
		
	}

	
	@Override
	public IBinder onBind(Intent arg0) {
		// No binding provided
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 
		while (dtn_api_binder == null)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "Waiting for bound.");
		}
		Bundle data = intent.getBundleExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
		try {
			createDTNBundle(data);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DTNOpenFailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DTNAPIFailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void createDTNBundle(Bundle params) throws UnsupportedEncodingException, DTNOpenFailException, DTNAPIFailException 
	{
		String destination = params.getString(Constants.DTN_DEST_EID_KEY); 
		int expiration = params.getInt(Constants.DTN_EXPIRATION_KEY);
		byte[] message = params.getByteArray(Constants.DTN_PAYLOAD_KEY);
		int bundle_opts = Constants.BUNDLE_DOPTS; // TODO - Maybe this should be passed in

		
		// Setting DTNBundle Payload according to the values
		DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_MEM);
		dtn_payload.set_buf(message);
		Log.i(TAG, "We are creating a dtn bundle"); 

		// Start the DTN Communication
		DTNHandle dtn_handle = new DTNHandle();
		dtn_api_status_report_code open_status = dtn_api_binder.dtn_open(dtn_handle);
		if (open_status != dtn_api_status_report_code.DTN_SUCCESS) throw new DTNOpenFailException();
		try {
			DTNBundleSpec spec = new DTNBundleSpec();
			// Destination is static gateway
			spec.set_dest(new DTNEndpointID(destination));
			Log.i(TAG, "Setting destination to " + destination);   
			
			// set the source EID from the bundle Daemon
			spec.set_source(new DTNEndpointID(BundleDaemon.getInstance().local_eid().toString()));
			
			spec.set_expiration(expiration);
			Log.i(TAG, "Set bundle expiration to " + expiration);   

			spec.set_dopts(bundle_opts);
			// Set priority
			spec.set_priority(dtn_bundle_priority_t.COS_NORMAL);
			
			// Data structure to get result from the IBinder
			DTNBundleID dtn_bundle_id = new DTNBundleID();

			Log.i(TAG, "Sending bundle ...");   

			dtn_api_status_report_code api_send_result =  dtn_api_binder.dtn_send(dtn_handle, 
					spec, 
					dtn_payload, 
					dtn_bundle_id);
			
			// If the API fail to execute throw the exception so user interface can catch and notify users
			if (api_send_result != dtn_api_status_report_code.DTN_SUCCESS)
			{
				throw new DTNAPIFailException();
			}		
		}
		finally
		{
			dtn_api_binder.dtn_close(dtn_handle);
		}
	}

	
}

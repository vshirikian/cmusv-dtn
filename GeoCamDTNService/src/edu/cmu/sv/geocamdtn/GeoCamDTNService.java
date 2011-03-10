package edu.cmu.sv.geocamdtn;

import java.io.UnsupportedEncodingException;

import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNService;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPIBinder;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_api_status_report_code;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_bundle_payload_location_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_bundle_priority_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundlePayload;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleSpec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNEndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNHandle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNAPIFailException;
import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNOpenFailException;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import edu.cmu.sv.geocamdtn.lib.Constants;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GeoCamDTNService extends IntentService {
	
	/**
	 * Logging TAG for supporting Android logging mechanism
	 */
	private static String SEND_TAG = "GeoCamDTNSend";
	
	/**
	 * The service connection to communicate with DTNService 
	 */
	private ServiceConnection conn;
	
	/**
	 * DTNAPIBinder object
	 */
	private DTNAPIBinder dtn_api_binder;
	
	public GeoCamDTNService() {
		super("GeoCamDTNService");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		bindDTNService();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unbindDTNService();
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
		conn = new ServiceConnection() {
			public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
				Log.i(SEND_TAG, "DTN Service is bound");
				dtn_api_binder = (DTNAPIBinder) ibinder;
			}

			public void onServiceDisconnected(ComponentName arg0) {
				Log.i(SEND_TAG, "DTN Service is Unbound");
				dtn_api_binder = null;
			}

		};

		Intent i = new Intent(this, DTNService.class);
		bindService(i, conn, BIND_AUTO_CREATE);	
	}
	
	/**
	 * major function for send message by calling dtnsend API
	 * @throws UnsupportedEncodingException
	 * @throws DTNOpenFailException
	 * @throws DTNAPIFailException
	 */
	private void sendMessage(byte[] message) throws UnsupportedEncodingException, DTNOpenFailException, DTNAPIFailException {				
		// Setting DTNBundle Payload according to the values
		DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_MEM);
		dtn_payload.set_buf(message);
		   
		// Start the DTN Communication
		DTNHandle dtn_handle = new DTNHandle();
		dtn_api_status_report_code open_status = dtn_api_binder.dtn_open(dtn_handle);
		if (open_status != dtn_api_status_report_code.DTN_SUCCESS) throw new DTNOpenFailException();
		try {
			DTNBundleSpec spec = new DTNBundleSpec();
			
			// Destination is static gateway
			spec.set_dest(new DTNEndpointID(Constants.STATIC_GATEWAY_EID));
			
			// set the source EID from the bundle Daemon
			spec.set_source(new DTNEndpointID(BundleDaemon.getInstance().local_eid().toString()));
			
			spec.set_expiration(Constants.BUNDLE_EXPIRATION);

			spec.set_dopts(Constants.BUNDLE_DOPTS);
			// Set priority
			spec.set_priority(dtn_bundle_priority_t.COS_NORMAL);
			
			// Data structure to get result from the IBinder
			DTNBundleID dtn_bundle_id = new DTNBundleID();
			
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

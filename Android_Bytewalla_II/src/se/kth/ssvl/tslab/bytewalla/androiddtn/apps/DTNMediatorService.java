package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import java.io.File;
import java.io.UnsupportedEncodingException;
import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNManager;
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
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.Bundle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.util.Log;
import edu.cmu.sv.geocamdtn.lib.Constants;

public class DTNMediatorService extends IntentService {
	
	public DTNMediatorService() {
		super("DTNMediatorService");
		// TODO Auto-generated constructor stub
	}

	/**
	 * Logging tag for supporting Android logging mechanism
	 */
	private static final String TAG = "se.kth.ssvl.tslab.bytewalla.DTNMediatorService";

	/**
	 * The service connection to communicate with DTNService 
	 */
	private ServiceConnection conn;
	
	/**
	 * DTNAPIBinder object
	 */
	private DTNAPIBinder dtn_api_binder;
	
	private ConditionVariable serviceCondition;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Creating mediator");
		serviceCondition = new ConditionVariable();
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
	private void unbindDTNService() {
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
				serviceCondition.open();
			}

			public void onServiceDisconnected(ComponentName arg0) {
				Log.i(TAG, "DTN Service is Unbound");
				dtn_api_binder = null;
			}

		};

		Intent i = new Intent(DTNMediatorService.this, DTNService.class);
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
	public void onHandleIntent(Intent intent) {
		// Receive Android bundle from intent
		String action = intent.getAction();

		// If intent is from BundleDaemon:handle_bundle_received
		if (action.equals(Constants.ACTION_RECEIVE_DTN_BUNDLE)) {
			android.os.Bundle data = intent.getBundleExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
	        // Receive DTN bundle from intent and process
	        Bundle bundle = (Bundle)data.getSerializable(Constants.DTN_BUNDLE_KEY);
	        processReturnReceipt(bundle);
            
		// If intent is from GeoCamDTNProxy
		} else if (action.equals(Constants.ACTION_MEDIATE_DTN_BUNDLE)) {
			File file = (File)intent.getSerializableExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
			try {
				serviceCondition.block();
				createDTNBundleFromFile(file);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException" + e);
				e.printStackTrace();
			} catch (DTNOpenFailException e) {
				Log.e(TAG, "DTNOpenFailException" + e);
				e.printStackTrace();
			} catch (DTNAPIFailException e) {
				Log.e(TAG, "DTNAPIFailException" + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Process received bundle
	 * @param bundle
	 */
	private void processReturnReceipt(Bundle bundle) {
		// Only a return receipt if destined to this device
		if (!bundle.dest().equals(BundleDaemon.getInstance().local_eid())) {
			return;
		}
		
		// Extract payload from DTN bundle
		byte[] payload = new byte[bundle.payload().length()];
		bundle.payload().read_data(0, bundle.payload().length(), payload);
		
		// For now, just notify user received return receipt bundle payload
		DTNManager.getInstance().notify_user("GeoCam Return Receipt", "Payload: " + new String(payload));
		Log.i(TAG, "GEOCAM RETURN RECEIPT from " + bundle.source().uri() + ": " + new String(payload));
	}
	
	/**
	 * Create a DTN bundle from a file and send to DTN agent
	 * @param file
	 */
	private void createDTNBundleFromFile(File file) throws UnsupportedEncodingException, DTNOpenFailException, DTNAPIFailException {
		// Setting DTNBundle Payload according to the values
		DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_FILE);
		dtn_payload.set_file(file);
		Log.d(TAG, "Creating a dtn bundle"); 

		// Start the DTN Communication
		DTNHandle dtn_handle = new DTNHandle();
		dtn_api_status_report_code open_status = dtn_api_binder.dtn_open(dtn_handle);
		if (open_status != dtn_api_status_report_code.DTN_SUCCESS) 
			throw new DTNOpenFailException();
		try {
			DTNBundleSpec spec = new DTNBundleSpec();
			// Destination is static gateway
			spec.set_dest(new DTNEndpointID(Constants.STATIC_GATEWAY_EID));
			Log.d(TAG, "Setting destination to " + Constants.STATIC_GATEWAY_EID);
			// set the source EID from the bundle Daemon
			spec.set_source(new DTNEndpointID(BundleDaemon.getInstance().local_eid().toString()));
			spec.set_expiration(Constants.BUNDLE_EXPIRATION);
			Log.d(TAG, "Set bundle expiration to " + Constants.BUNDLE_EXPIRATION);   
			spec.set_dopts(Constants.BUNDLE_DOPTS);
			spec.set_priority(dtn_bundle_priority_t.COS_NORMAL);
			
			// Data structure to get result from the IBinder
			DTNBundleID dtn_bundle_id = new DTNBundleID();

			Log.d(TAG, "Sending bundle ...");   

			dtn_api_status_report_code api_send_result =  dtn_api_binder.dtn_send(dtn_handle, 
					spec, 
					dtn_payload, 
					dtn_bundle_id);
			
			// delete temporary file (it is not needed anymore since bytewalla has persistent bundle with file as payload)
			file.delete();
			
			// If the API fail to execute throw the exception so user interface
			// can catch and notify users
			if (api_send_result != dtn_api_status_report_code.DTN_SUCCESS) {
				throw new DTNAPIFailException();
			}
		} finally {
			dtn_api_binder.dtn_close(dtn_handle);
		}
	}
}

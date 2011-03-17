package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import java.io.UnsupportedEncodingException;
import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNManager;
import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNService;
import se.kth.ssvl.tslab.bytewalla.androiddtn.R;
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
import edu.cmu.sv.geocamdtn.lib.Constants;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.util.Log;

public class DTNMediatorService extends IntentService {
	
	public DTNMediatorService() {
		super("DTNMediatorService");
		// TODO Auto-generated constructor stub
	}

	/**
	 * Logging tag for supporting Android logging mechanism
	 */
	private static final String TAG = "edu.cmu.sv.geocamdtn.DTNMediatorService";

	/**
	 * The service connection to communicate with DTNService 
	 */
	private ServiceConnection conn;
	
	/**
	 * DTNAPIBinder object
	 */
	private DTNAPIBinder dtn_api_binder;
	
	private ConditionVariable serviceCondition;

	// for return receipt notifications
	private NotificationManager	notification_manager;
	private static int NOTIFICATION_APPLICATION_ID = 0;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Creating mediator");
		serviceCondition = new ConditionVariable();
		notification_manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
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

	/**
	 * (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public void onHandleIntent(Intent intent) {
		// If intent is from BundleDaemon:handle_bundle_received
		if (intent.getAction().equals(Constants.ACTION_RECEIVE_DTN_BUNDLE)) {
			// Receive DTN bundle from intent
			Bundle bundle = (Bundle)intent.getSerializableExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
			try {
				// Acquire lock working with bundle
				if (bundle.get_lock().isLocked()) {
					bundle.get_lock().wait();
				} else { 
					bundle.get_lock().tryLock();
				}

				// Only process bundles as return receipts if their destination is your EID
				if (!bundle.dest().equals(BundleDaemon.getInstance().local_eid())) {
					return;
				}
				
				byte[] payload = new byte[bundle.payload().length()];
				bundle.payload().read_data(0, bundle.payload().length(), payload);
				// For now, just notify user received bundle payload to screen
				notify_user("Return Receipt", "Payload: " + new String(payload));
				Log.i(TAG, "RETURN RECEIPT from " + bundle.source().uri() + ": " + new String(payload));				
				// Release bundle lock
				bundle.get_lock().unlock();
			} catch (InterruptedException e) {
				Log.i(TAG, "Mediator failed to successfully receive DTN bundle: " + e);
				e.printStackTrace();
			}

		// If intent is from GeoCamDTNService
		} else if (intent.getAction().equals(Constants.ACTION_MEDIATE_DTN_BUNDLE)) {
			// Receive Android bundle from intent
			android.os.Bundle data = intent.getBundleExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
			try {			
				serviceCondition.block();
				createDTNBundle(data);
			} catch (UnsupportedEncodingException e) {
				Log.i(TAG, "Mediator failed to successfully send DTN bundle: " + e);
				e.printStackTrace();
			} catch (DTNOpenFailException e) {
				Log.i(TAG, "Mediator failed to successfully send DTN bundle: " + e);
				e.printStackTrace();
			} catch (DTNAPIFailException e) {
				Log.i(TAG, "Mediator failed to successfully send DTN bundle: " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Android notification to users 
	 * @param text the main text to notify
	 * @param description the description will be shown in detailed description UI
	 */
	private void notify_user(String text, String description) {
		Intent intent = new Intent(this, DTNManager.class);
		
		Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
        
		notification.setLatestEventInfo(DTNMediatorService.this, text, description, 
				PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
	    
		notification_manager.notify(NOTIFICATION_APPLICATION_ID++, notification);
	}
	
	/**
	 * Create DTN bundle and send to DTNService
	 * @param params android.os.Bundle of parameters needed to make a bundle
	 */
	private void createDTNBundle(android.os.Bundle params) throws UnsupportedEncodingException, DTNOpenFailException, DTNAPIFailException 
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
			if (api_send_result != dtn_api_status_report_code.DTN_SUCCESS) {
				throw new DTNAPIFailException();
			}
		} finally {
			dtn_api_binder.dtn_close(dtn_handle);
		}
	}
}

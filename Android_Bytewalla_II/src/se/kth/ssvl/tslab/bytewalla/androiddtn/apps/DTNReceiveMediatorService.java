/**
 * 
 */
package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.cmu.sv.geocamdtn.lib.Constants;

import se.kth.ssvl.tslab.bytewalla.androiddtn.DTNService;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPIBinder;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_api_status_report_code;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_bundle_payload_location_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.DTNAPICode.dtn_reg_flags_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundlePayload;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNBundleSpec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNEndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNHandle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.applib.types.DTNRegistrationInfo;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.List;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

/**
 * Service that sits and listens for 1. eid registrations from other apps 2. DTN
 * messages for registered eids Will throw intent with the payload from DTN.
 * 
 * We will use a Looper thread to handle eid registrations. A secondary thread
 * will be used to poll dtn for new bundles
 * 
 * @author hbarnor
 * 
 */
public class DTNReceiveMediatorService extends Service {

	/**
	 * The String TAG for supporting Android Logging mechanism
	 */
	private final static String TAG = "DTNReceiveMediatorService";

	/**
	 * The DTNAPIBinder for calling API in DTNService
	 */
	private DTNAPIBinder mDTNAPIBinder;

	/**
	 * The DTNAPIService connection
	 */
	private ServiceConnection mConnection;

	private Thread mReceiveThread;
	private ConditionVariable mBindingCondition;
	//private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	//private final Lock r = rwl.readLock();
	//private final Lock w = rwl.writeLock();

	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private List<DTNRegHandlePair> mRegistrationIds;

	/**
	 * 
	 */
	public DTNReceiveMediatorService() {
		mRegistrationIds = new List<DTNRegHandlePair>();
		mBindingCondition = new ConditionVariable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		super.onCreate();
		Log.d(TAG, "Spawning thread to receive bundles.");
		mReceiveThread = new Thread(new BundleReceiverRunnable());
		mReceiveThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
		mReceiveThread.start();

		// This thread handles eid registration
		HandlerThread eidRegThread = new HandlerThread(
				"EidRegistrationArguments", Process.THREAD_PRIORITY_BACKGROUND);
		eidRegThread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = eidRegThread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// Binding not supported
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Recieved an intent");
		if (intent.getAction().compareTo(
				Constants.ACTION_REGISTER_WITH_RECEIVE_SERVICE) == 0) {
			// send the bundle along to the handler
			Message msg = mServiceHandler.obtainMessage();
			msg.setData(intent.getBundleExtra(Constants.DTN_DEST_EID_KEY));
			mServiceHandler.sendMessage(msg);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * Helper function to receive_messages
	 * 
	 * @return the number of messages receive
	 * @throws DTNAPIFailException
	 */
	private int receiveBundles() throws DTNAPIFailException {
		int count = 0;
		// From here on there at least one registration bound to this handle
		// Iterate over registration_ids to get
		// DTNHandle handle = new DTNHandle();
		synchronized (mRegistrationIds) {
			Iterator<DTNRegHandlePair> iter = mRegistrationIds.iterator();
			while (iter.hasNext()) {
				DTNRegHandlePair pair = null;
				pair = iter.next();
				if (null != pair) {
					DTNHandle handle = pair.handle();
					int regid = pair.regid();
					// Bind the handle to registration if it's not already bind
					// mDTNAPIBinder.dtn_bind(handle, regid.intValue());

					// create an empty spec and payload to retrieve value from
					// an
					// API
					DTNBundleSpec spec = new DTNBundleSpec();
					DTNBundlePayload dtn_payload = new DTNBundlePayload(
							dtn_bundle_payload_location_t.DTN_PAYLOAD_FILE);

					// Block Receiving call from API
					dtn_api_status_report_code receive_result = null;
					try {
						do {
							receive_result = mDTNAPIBinder.dtn_recv(handle,
									regid, spec, dtn_payload, 1);

							if (receive_result == dtn_api_status_report_code.DTN_SUCCESS) {
								// TODO
								Log
										.d(TAG, "Bundle received for regid "
												+ regid);
								broadCastBundle(spec, dtn_payload);
							} else {
								break;
							}
							count++;
						} while (receive_result == dtn_api_status_report_code.DTN_SUCCESS);
					} catch (InterruptedException e) {
						// If we got more than one result try the next step
						e.printStackTrace();
						// continue;
					}
				}
			}
		}
		return count;

	}
	
	/**
	 * Send the bundle as an intent with payload data
	 * @param spec
	 * @param payload
	 */
	private void broadCastBundle(DTNBundleSpec spec, DTNBundlePayload payload) {
		Log.d(TAG, "Broadcasting dtn bundle to listening parties.");
		Bundle dtnData = new Bundle();
		dtnSpecIntoBundle(dtnData, spec);
		if (payload.location() == dtn_bundle_payload_location_t.DTN_PAYLOAD_MEM) {
			try {
				String payloadString = new String(payload.buf(), "US-ASCII");
				dtnData.putInt(Constants.DTN_PAYLOAD_TYPE, Constants.DTN_MEM_PAYLOAD);
				dtnData.putString(Constants.DTN_PAYLOAD_KEY, payloadString);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else if (payload.location() == dtn_bundle_payload_location_t.DTN_PAYLOAD_FILE) {
			// lets read the first MB of the file and send it along for now
			FileInputStream fin;
			int size = (int) payload.file().length();
			if (size > 1024 ) {
				size = 1024;
			}
			// read max of only 1024 bytes
			byte fileContent[] = new byte[size];
			try {
				fin = new FileInputStream(payload.file());
				fin.read(fileContent, 0, size);
				fin.close();
			}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

			dtnData.putString(Constants.DTN_PAYLOAD_KEY, new String(fileContent));
			dtnData.putInt(Constants.DTN_PAYLOAD_TYPE, Constants.DTN_FILE_PAYLOAD);
		}
		Intent bundleIntent = new Intent(Constants.ACTION_RECEIVE_DTN_BUNDLE);
		bundleIntent.putExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD, dtnData);
		this.getApplicationContext().sendBroadcast(bundleIntent);
		// TODO: Send a broadcast instead
		// startActivity(bundleIntent);
		// startService(bundleIntent);
	}

	
	/**
	 * 
	 * Serialize the spec into extras on the bunlde
	 * 
	 * @param bundle
	 * @param spec
	 */
	private void dtnSpecIntoBundle(Bundle bundle, DTNBundleSpec spec) {
		// TODO Auto-generated method stub
		bundle.putString(Constants.DTN_DEST_EID_KEY, spec.dest().toString());
		bundle.putInt(Constants.DTN_EXPIRATION_KEY, spec.expiration());
		bundle.putString(Constants.DTN_SRC_EID_KEY, spec.source().toString());
		// TODO: What else ? 
	}

	// Handler that receives messages from the thread
	// This one will specifically receive eid registration
	// And will add them to a list of registered eids
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			// Extract EID and add to list
			// Get value from user interfaces
			Bundle bundle = msg.peekData();
			String eid = bundle.getString(Constants.DTN_DEST_EID_KEY);
			List<Integer> registrationIds = new List<Integer>() ;
			// find registrations first with dtn_find_registration
			DTNEndpointID dest_eid = new DTNEndpointID(eid);
			DTNHandle handle = new DTNHandle();
			dtn_api_status_report_code find_result = mDTNAPIBinder
					.dtn_find_registrations(handle, dest_eid, registrationIds);

			if (find_result != dtn_api_status_report_code.DTN_SUCCESS) {
				// There are no existing registration for this Endpoint ID
				// Create the registration for this and put the regid in
				// registration_ids
				Log.d(TAG, "Registration: " + eid
						+ " not found. Creating new registration ...");
				DTNRegistrationInfo reginfo = new DTNRegistrationInfo(dest_eid,
						dtn_reg_flags_t.DTN_REG_DEFER.getCode(), 3600, false);
				int[] newregid = new int[1];
				try {
					mDTNAPIBinder.dtn_register(handle, reginfo, newregid);
				} catch (DTNAPIFailException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "Registration failed.");
					e.printStackTrace();
				}
				// bind the handle to the eid ans store that also 				
				mDTNAPIBinder.dtn_bind(handle, newregid[0]);
				// the regid to the list
				synchronized (mRegistrationIds) {
					mRegistrationIds.add(new DTNRegHandlePair(newregid[0], handle));
				}
			} else { 
				// we found some existing registrations
				// so lets add some handlers and track them
				Iterator<Integer> iter = registrationIds.iterator();
				while (iter.hasNext()) {
					Integer regId = iter.next();
					handle = new DTNHandle();
					mDTNAPIBinder.dtn_bind(handle, regId.intValue());
					synchronized (mRegistrationIds) {
						mRegistrationIds.add(new DTNRegHandlePair(regId.intValue(), handle));
					}
				}
				
			}

			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			// TODO: What to do about this
			// stopSelf(msg.arg1);
		}
	}

	// Runnable that polls DTN for bundles
	private final class BundleReceiverRunnable implements Runnable {

		/**
		 * Bind DTNService by using ServiceConnection
		 */
		private void bindDTNService() {
			mConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName arg0,
						IBinder ibinder) {
					Log.i(TAG, "DTN Service is bound");
					mDTNAPIBinder = (DTNAPIBinder) ibinder;
					mBindingCondition.open();
				}

				public void onServiceDisconnected(ComponentName arg0) {
					Log.i(TAG, "DTN Service is Unbound");
					mDTNAPIBinder = null;
				}
			};

			Intent i = new Intent(DTNReceiveMediatorService.this,
					DTNService.class);
			bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		}

		public void run() {
			// bind to the dtn service
			bindDTNService();
			// lets wait for binding to complete
			mBindingCondition.block();
			// we now have a binder, so lets start receiving messages
			// assumption is that dtn_receive blocks
			Log.d(TAG, "Done binding starting bundle reception.");
			int numMessages;
			while (true) {

				try {
					numMessages = receiveBundles();
					if (numMessages > 0) {
						Log.d(TAG, "Recieved " + numMessages + " bundles.");
					}
				} catch (DTNAPIFailException e) {
					e.printStackTrace();
				}
			}

		}

	}
	
	class DTNRegHandlePair {
		private int regid;
		private DTNHandle handle;
		
		public DTNRegHandlePair(int regid, DTNHandle handle) {
			this.regid = regid;
			this.handle = handle;
		}
		
		public DTNRegHandlePair(int regid) {
			this.regid = regid;
			this.handle = new DTNHandle();
			mDTNAPIBinder.dtn_bind(handle, regid);
		}
		
		public DTNHandle handle() {
			return this.handle;
		}
		
		public int regid() {
			return regid;
		}
	}
}

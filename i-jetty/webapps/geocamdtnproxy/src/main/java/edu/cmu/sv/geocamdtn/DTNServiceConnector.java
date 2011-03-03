package edu.cmu.sv.geocamdtn;

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
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.naming.EndpointID;

import android.content.Context;
import android.content.Intent;

import org.apache.commons.fileupload.FileItem;

public class DTNServiceConnector {
	public static final String DEST_EID = "dtn://sg";
	
	private Context context;
	private DTNAPIBinder dtn_api_binder;
	private ServiceConnection conn;
	
	public DTNServiceConnector(Context context) {
		// context = (Context)getServletContext().getAttribute("org.mortbay.ijetty.context");
		this.context = context;
	}
	
	public void bindDTNService() {
		conn = new ServiceConnection() {
			public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
				dtn_api_binder = (DTNAPIBinder) ibinder;
			}
			
			public void onServiceDisconnected(ComponentName arg0) {
				dtn_api_binder = null;
			}
		};
		
		Intent i = new Intent(context, DTNService.class);
		context.bindService(i, conn, BIND_AUTO_CREATE);
	}
	
	public void unbindDTNService() {
		context.unbindService(conn);
	}
	
	public void sendBundle(byte[] message_byte_array) {
		// ---- Start copying from DTNSend ----
		// Setting DTNBundle Payload according to the values
		DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_MEM);
		dtn_payload.set_buf(message_byte_array);
		
		// Start the DTN Communication
		DTNHandle dtn_handle = new DTNHandle();
		dtn_api_status_report_code open_status = dtn_api_binder.dtn_open(dtn_handle);
		if (open_status != dtn_api_status_report_code.DTN_SUCCESS) throw new DTNOpenFailException();
		try
		{
			DTNBundleSpec spec = new DTNBundleSpec();
			
			// set destination from the user input
			spec.set_dest(new DTNEndpointID(DEST_EID));
			
			// set the source EID from the bundle Daemon
			spec.set_source(new DTNEndpointID(BundleDaemon.getInstance().local_eid().toString()));
			
			// Set expiration in seconds, default to 1 hour
			spec.set_expiration(EXPIRATION_TIME);
			// no option processing for now
			spec.set_dopts(DELIVERY_OPTIONS);
			// Set priority
			spec.set_priority(PRIORITY);
			
			// Data structure to get result from the IBinder
			DTNBundleID dtn_bundle_id = new DTNBundleID();
			
			dtn_api_status_report_code api_send_result =  dtn_api_binder_.dtn_send(dtn_handle, 
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
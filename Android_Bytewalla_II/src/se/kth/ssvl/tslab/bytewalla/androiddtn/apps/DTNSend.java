/*
 *	  This file is part of the Bytewalla Project
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/092106/".
 *    
 *    Copyright 2009 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 */
package se.kth.ssvl.tslab.bytewalla.androiddtn.apps;

import java.io.UnsupportedEncodingException;
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
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.naming.EndpointID;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

/**
 * The DTNSend Activity. This Activity allows user to send Text message from DTN
 * @author Rerngvit Yanggratoke (rerngvit@kth.se)
 */
public class DTNSend extends Activity  
{
	public static final String USER_PREFERENCE = "USER_PREFERENCES";
	
	public static final String PREF_DEST_ENDPOINT = "PREF_DEST_ENDPOINT";
	public static final String PREF_DEST_MESSAGE = "PREF_DEST_MESSAGE";
	
	SharedPreferences prefs;

	/**
	 * Logging TAG for supporting Android logging mechanism
	 */
	private static String TAG = "DTNSend";
	
	/**
	 * SendButton reference object
	 */
	private Button SendButton;
	
	/**
	 * CloseButton reference object
	 */
	private Button closeButton;
	
	/**
	 * Destination EndpointID reference object
	 */
	//private EditText DestEIDEditText;
	private AutoCompleteTextView  DestEIDEditText;
	
	/**
	 * Message Textview reference object
	 */
	private TextView MessageTextView;
	
	/**
	 * DTNAPIBinder object
	 */
	private DTNAPIBinder dtn_api_binder_;

	
	//  Default DTN send parameters 
	/**
	 * Default expiration time in seconds, set to 1 hour
	 */
	private static final int EXPIRATION_TIME = 1*60*60;
	
	/**
	 * Set delivery options to don't flag at all
	 */
	private static final int DELIVERY_OPTIONS = 0;
	
	/**
	 * Set priority to normal sending
	 */
	private static final dtn_bundle_priority_t PRIORITY = dtn_bundle_priority_t.COS_NORMAL;
	 
	/**
	 * The service connection to communicate with DTNService 
	 */
	private ServiceConnection conn_;
	
	/**
	 * The onCreate function overridden from Android Activity. 
	 * This basically will associate the display view to dtnsend layout and initialize the class
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtnapps_dtnsend);
		init();
	}
	
	/**
	 * The onDestroy function override form Android Activity. This will free the DTNAPIBinder resource
	 * by unbinding the service
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		unbindDTNService();
	}
		
	/**
	 * Internal Invalid EndpointIDException for this DTNSend Exception.
	 * This is used to pass exception and notify users about invalid input Destination EID
	 */
	private static class  InvalidEndpointIDException extends Exception
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6431328157822474346L;
		
	}
	
	/**
	 * Initialization function for this class. This will init userinterface then bind the DTNService
	 */
	private void init()
	{
		initUI();
		bindDTNService();
	}
	
	private static final String[] eids = new String[] {
        "dtn://nexus.bytewalla.com/test", "dtn://tattoo.bytewalla.com/test", "dtn://android.bytewalla.com/test", "dtn://city.bytewalla.com/test", "dtn://village.bytewalla.com/test", 
    };

	
	/**
	 * Initialization for the user interface by referencing with the actual runtime object and
	 * then set appropriate text or events to the runtime object
	 */
	private void initUI()
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, eids);

		    //DestEIDEditText = (EditText) this.findViewById(R.id.DTNApps_DTNSend_DestEIDEditText);
		DestEIDEditText = (AutoCompleteTextView) this.findViewById(R.id.DTNApps_DTNSend_DestEIDEditText);
			MessageTextView = (TextView) this.findViewById(R.id.DTNApps_DTNSend_MessageTextView);
			
			DestEIDEditText.setAdapter(adapter);

			
			SendButton = (Button)this.findViewById(R.id.DTNApps_DTNSend_SendButton);
			
			  prefs = getSharedPreferences(USER_PREFERENCE, Activity.MODE_PRIVATE);
			  updateUIFromPreferences();
			  
			  
			SendButton.setOnClickListener(new OnClickListener() {
			
				
				public void onClick(View v) {
					try {
						// Validate the user input first whether the EID is valid EID
						checkInputEID();
						
					
						
						// If the validator pass, send the message
						sendMessage();
						
						new AlertDialog.Builder(DTNSend.this).setMessage(
								"Sent DTN message to DTN Service successfully ")
								.setPositiveButton("OK", null).show();
						
						savePreferences();
						
						
					} catch (InvalidEndpointIDException e) {
						new AlertDialog.Builder(DTNSend.this).setMessage(
								"Dest EID is invalid. Please input valid EID for example dtn://endpoint.com")
								.setPositiveButton("OK", null).show();
						
						
						
					} catch (Exception e) {
						new AlertDialog.Builder(DTNSend.this).setMessage(
						"Internal error with " + e.getMessage())
						.setPositiveButton("OK", null).show();
						
					}
					
					
				}
			});
		  	
			closeButton = (Button)this.findViewById(R.id.DTNApps_DTNSend_CloseButton);
			closeButton.setOnClickListener(new OnClickListener() {
				

				public void onClick(View v) {
					   //Toast.makeText(DTNManager.me, "hola", Toast.LENGTH_SHORT).show();  
					savePreferences();
					DTNSend.this.finish();
					
				}
			});
			
	}
	
	private void savePreferences() {
		String dest_EID_text=DestEIDEditText.getText().toString();
			String dest_message_text=(String) MessageTextView.getText().toString();
			
			Editor editor = prefs.edit();
			  editor.putString(PREF_DEST_ENDPOINT, dest_EID_text);
			  editor.putString(PREF_DEST_MESSAGE, dest_message_text);
			  
			  editor.commit();
				
	}
	private void updateUIFromPreferences() 
	{
		String dest_EID_text=prefs.getString(PREF_DEST_ENDPOINT, "dtn://node.bytewalla.com/test");
		String dest_message_text=prefs.getString(PREF_DEST_MESSAGE, "testing");
		 
		DestEIDEditText.setText(dest_EID_text);
		MessageTextView.setText(dest_message_text);
		  
		}

	/**
	 * Unbind the DTNService to free resource consumed by the binding
	 */
	private void unbindDTNService()
	{
		
		unbindService(conn_);
	}
	
	/**
	 * bind the DTNService to use the API later
	 */
	private void bindDTNService()
	{
		
		
		conn_ = new ServiceConnection() {

			


			public void onServiceConnected(ComponentName arg0, IBinder ibinder) {
				Log.i(TAG, "DTN Service is bound");
				dtn_api_binder_ = (DTNAPIBinder) ibinder;
			}



			public void onServiceDisconnected(ComponentName arg0) {
				Log.i(TAG, "DTN Service is Unbound");
				dtn_api_binder_ = null;
			}

			};

			Intent i = new Intent(DTNSend.this, DTNService.class);
			bindService(i, conn_, BIND_AUTO_CREATE);	
	}
	
	/**
	 * major function for send message by calling dtnsend API
	 * @throws UnsupportedEncodingException
	 * @throws DTNOpenFailException
	 * @throws DTNAPIFailException
	 */
	private void sendMessage() throws UnsupportedEncodingException, DTNOpenFailException, DTNAPIFailException
	{
		// Getting values from user interface
		String message = MessageTextView.getText().toString();
		byte[] message_byte_array = message.getBytes("US-ASCII");
		
		
		
		//****************************sacar
		//byte[] message_byte_array1=new byte[1024*1024];
		//Random randomGenerator = new Random();

        //for (int i=0; i<message_byte_array1.length;i++)
        //{
        //	message_byte_array1[(int) i]=(byte) randomGenerator.nextInt(100);

        //}
		//******************************end
		
		
		
		String dest_eid = DestEIDEditText.getText().toString();
		
		// Setting DTNBundle Payload according to the values
		DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_MEM);
		dtn_payload.set_buf(message_byte_array);
		
	//	DTNBundlePayload dtn_payload = new DTNBundlePayload(dtn_bundle_payload_location_t.DTN_PAYLOAD_FILE);
	//	dtn_payload.set_file(new File("/sdcard/testbw.zip"));
	
		   
		// Start the DTN Communication
		DTNHandle dtn_handle = new DTNHandle();
		dtn_api_status_report_code open_status = dtn_api_binder_.dtn_open(dtn_handle);
		if (open_status != dtn_api_status_report_code.DTN_SUCCESS) throw new DTNOpenFailException();
		try
		{
		DTNBundleSpec spec = new DTNBundleSpec();
		
		// set destination from the user input
		spec.set_dest(new DTNEndpointID(dest_eid));
		
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
			dtn_api_binder_.dtn_close(dtn_handle);
		}
	
		
		
	}
	
	/**
	 * validate the input EID and throw exception if it's valid
	 * @throws InvalidEndpointIDException
	 */
	private void checkInputEID() throws InvalidEndpointIDException
	{
		String dest_eid =   DestEIDEditText.getText().toString();
		if (!EndpointID.is_valid_URI(dest_eid)) throw new InvalidEndpointIDException();
		
	}
		
}
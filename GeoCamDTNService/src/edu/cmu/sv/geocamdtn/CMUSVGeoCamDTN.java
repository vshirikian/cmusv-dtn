/**
 * 
 */
package edu.cmu.sv.geocamdtn;

import edu.cmu.sv.geocamdtn.R;
import edu.cmu.sv.geocamdtn.lib.Constants;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author hbarnor
 * 
 */
public class CMUSVGeoCamDTN extends Activity {

	/**
	 * The Destination EndpointID EditText reference object
	 */
	private EditText eidEditText;

	/**
	 * The registerButton reference object
	 */
	private Button registerButton;

	/**
	 * The registerButton reference object
	 */
	private Button unRegisterButton;

	private TextView resultTextView;

	/**
	 * 
	 */
	public CMUSVGeoCamDTN() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initUserInterface();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Intent triggerIntent = getIntent();
		if (triggerIntent.hasExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD)) {
			processBundle(triggerIntent);
		}
	}

	private void processBundle(Intent intent) {
		String msg = intent.getStringExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
		if (null != msg) 
		{
			StringBuffer buf = new StringBuffer();
			buf.append(String.format("\nReturn receipt: %s", msg));
			resultTextView.append(buf.toString());
		}
	}

	/**
	 * Intiailize user interface by adding events and setting appropriate text
	 */
	private void initUserInterface() {

		registerButton = (Button) this.findViewById(R.id.RegisterEidButton);
		unRegisterButton = (Button) this.findViewById(R.id.UnregisterEidButton);
		unRegisterButton.setEnabled(false);
		eidEditText = (EditText) this.findViewById(R.id.DestEIDEditText);
		resultTextView = (TextView) this.findViewById(R.id.ResultTextView);
		registerButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// Generate a registration intent and send it
				String destEid = eidEditText.getText().toString();
				Intent registration = new Intent(
						Constants.ACTION_REGISTER_WITH_RECEIVE_SERVICE);
				Bundle destEidBundle = new Bundle();
				destEidBundle.putString(Constants.DTN_DEST_EID_KEY, destEid);
				registration
						.putExtra(Constants.DTN_DEST_EID_KEY, destEidBundle);
				startService(registration);
			}

		});

	}

}

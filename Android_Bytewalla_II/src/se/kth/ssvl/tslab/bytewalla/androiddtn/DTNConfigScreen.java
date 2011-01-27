package se.kth.ssvl.tslab.bytewalla.androiddtn;

import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNApps;
import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNReceive;
import se.kth.ssvl.tslab.bytewalla.androiddtn.apps.DTNSend;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DTNConfigScreen extends Activity 
{
	/**
	 * String TAG to support Android Logging system
	 */
	private final static String TAG = "DTNConfigScreen";
	
	
	/**
	 * xxxButton reference object
	 */
	private Button DTNConfigEditorButton;
	
	/**
	 * DTNReceiveOpenBuntton reference object
	 */
	private Button DTNSetSecurityPolicyButton;
	
	/**
	 * CloseButton reference object
	 */
	private Button CloseButton;

	private static final int SHOW_SECURITYPOLICY = 1;
	
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtnconfigscreen);
		init();
	}
	
	
	/**
	 * Initialiazing function for DTNApps. Bind the object to runtime button, add event listeners
	 */
		private void init()
		{ 
			DTNConfigEditorButton    = (Button) this.findViewById(R.id.DTNConfigScreen_DTNConfigEditorOpenButton);
			DTNSetSecurityPolicyButton = (Button) this.findViewById(R.id.DTNConfigScreen_DTNSetSecurityPolicyOpenButton);
			CloseButton       = (Button) this.findViewById(R.id.DTNConfigScreen_CloseButton);
			
		
			
			CloseButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					DTNConfigScreen.this.finish();
					
				}
			});
		
			DTNConfigEditorButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(DTNConfigScreen.this, DTNConfigEditor.class);
					startActivity(i);

					
				}
			});
			
			DTNSetSecurityPolicyButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(DTNConfigScreen.this, DTNSetSecurityPolicy.class);
					startActivityForResult(i, SHOW_SECURITYPOLICY);
				}
			});
	
	
	
		}
		
		@Override 
		public void onActivityResult(int requestCode, 
		                             int resultCode, 
		                             Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			switch(requestCode) 
			{
				case (SHOW_SECURITYPOLICY) : 
				{
					if (resultCode == Activity.RESULT_OK) 
					{
						new AlertDialog.Builder(DTNConfigScreen.this).setMessage(
								data.getStringExtra("SELECTED_PISTOL"))
								.setPositiveButton("OK", null).show();
	
					}
					break;
				}
			}
	}
}

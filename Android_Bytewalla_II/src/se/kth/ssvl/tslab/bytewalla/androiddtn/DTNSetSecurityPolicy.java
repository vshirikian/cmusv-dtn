package se.kth.ssvl.tslab.bytewalla.androiddtn;





import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security.SPD;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security.SPD.spd_policy_t;
import android.app.Activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;

public class DTNSetSecurityPolicy extends Activity 
{
	/**
	 * String TAG to support Android Logging system
	 */
	private final static String TAG = "SetDTNSecurityPolicy";
	
	CheckBox checkbox_useBAB_IN;
	CheckBox checkbox_useCB_IN;
	CheckBox checkbox_usePSB_IN;
	
	CheckBox checkbox_useBAB_OUT;
	CheckBox checkbox_useCB_OUT;
	CheckBox checkbox_usePSB_OUT;
	
	
	/**
	 * CloseButton reference object
	 */
	private Button CloseButton;

	public static final String USER_PREFERENCE = "USER_PREFERENCES";
	
	public static final String IN_POLICY = "IN_POLICY";
	public static final String OUT_POLICY = "OUT_POLICY";
	
	public static final String useBAB_IN = "useBAB_IN";
	public static final String useCB_IN = "useCB_IN";
	public static final String usePSB_IN = "usePSB_IN";
	
	public static final String useBAB_OUT = "useBAB_OUT";
	public static final String useCB_OUT = "useCB_OUT";
	public static final String usePSB_OUT = "usePSB_OUT";

	SharedPreferences prefs;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dtnsetsecuritypolicy);
		init();
		
		prefs = getSharedPreferences(USER_PREFERENCE, Activity.MODE_PRIVATE);
		updateUIFromPreferences();
		
		Button okButton = (Button) findViewById(R.id.okButton);
		  okButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View view) 
		    {
		    	savePreferences();

		    	SPD.spd_direction_t direction;		   
		    	direction = SPD.spd_direction_t.SPD_DIR_IN;     	
		    	boolean useBAB_IN_checked = checkbox_useBAB_IN.isChecked();
		    	boolean useCB_IN_checked = checkbox_useCB_IN.isChecked();
		    	boolean usePSB_IN_checked = checkbox_usePSB_IN.isChecked();

		    	int policyCode=0;
		    	if (useBAB_IN_checked)
		    		policyCode=policyCode|SPD.spd_policy_t.SPD_USE_BAB.getCode();
		    	if (useCB_IN_checked)
		    		policyCode=policyCode|SPD.spd_policy_t.SPD_USE_CB.getCode();
		    	if (usePSB_IN_checked)
		    		policyCode=policyCode|SPD.spd_policy_t.SPD_USE_PSB.getCode();
		    	String res=SPD.getInstance().set_global_policy(direction, SPD.spd_policy_t.get(policyCode));
		    	direction = SPD.spd_direction_t.SPD_DIR_OUT;
		    	boolean useBAB_OUT_checked = checkbox_useBAB_OUT.isChecked();
		    	boolean useCB_OUT_checked = checkbox_useCB_OUT.isChecked();
		    	boolean usePSB_OUT_checked = checkbox_usePSB_OUT.isChecked();
		    	policyCode=0;
		    	if (useBAB_OUT_checked)
		    		policyCode=policyCode|SPD.spd_policy_t.SPD_USE_BAB.getCode();
		    	if (useCB_OUT_checked)
		    		policyCode=policyCode|SPD.spd_policy_t.SPD_USE_CB.getCode();
		    	if (usePSB_OUT_checked)
		    		policyCode=policyCode|SPD.spd_policy_t.SPD_USE_PSB.getCode();
		    	String res1=SPD.getInstance().set_global_policy(direction, SPD.spd_policy_t.get(policyCode));

		    	String return_message="The policy has been updated.\nPolicy IN: "+res+"\nPolicy OUT: "+res1;
		    	Uri data=null;
		    	Intent result = new Intent(null,data);
		    	result.putExtra("SELECTED_PISTOL", return_message);
		    	DTNSetSecurityPolicy.this.setResult(RESULT_OK, result);
		    	finish();
		    }
		  });
		  Button cancelButton = (Button) findViewById(R.id.cancelButton);
		  cancelButton.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View view) 
		    {
		    	DTNSetSecurityPolicy.this.setResult(RESULT_CANCELED);
		    	finish();
		    }
		  });
		  
		  
	}
	
	
	/**
	 * Initialiazing function for DTNApps. Bind the object to runtime button, add event listeners
	 */
		private void init()
		{ 
			//CloseButton       = (Button) this.findViewById(R.id.);
			checkbox_useBAB_IN = (CheckBox)findViewById(R.id.checkbox_useBAB_IN);
			checkbox_useCB_IN = (CheckBox)findViewById(R.id.checkbox_useCB_IN);
			checkbox_usePSB_IN = (CheckBox)findViewById(R.id.checkbox_usePSB_IN);
			
			checkbox_useBAB_OUT = (CheckBox)findViewById(R.id.checkbox_useBAB_OUT);
			checkbox_useCB_OUT = (CheckBox)findViewById(R.id.checkbox_useCB_OUT);
			checkbox_usePSB_OUT = (CheckBox)findViewById(R.id.checkbox_usePSB_OUT);
			
			
		}	
		
		private void updateUIFromPreferences() {
			  boolean useBAB_IN_checked = prefs.getBoolean(useBAB_IN, false);
			  boolean useCB_IN_checked = prefs.getBoolean(useCB_IN, false);
			  boolean usePSB_IN_checked = prefs.getBoolean(usePSB_IN, false);
			  
			  boolean useBAB_OUT_checked = prefs.getBoolean(useBAB_OUT, false);
			  boolean useCB_OUT_checked = prefs.getBoolean(useCB_OUT, false);
			  boolean usePSB_OUT_checked = prefs.getBoolean(usePSB_OUT, false);
			  
			 
			  checkbox_useBAB_IN.setChecked(useBAB_IN_checked);
			  checkbox_useCB_IN.setChecked(useCB_IN_checked);
			  checkbox_usePSB_IN.setChecked(usePSB_IN_checked);
			  
			  checkbox_useBAB_OUT.setChecked(useBAB_OUT_checked);
			  checkbox_useCB_OUT.setChecked(useCB_OUT_checked);
			  checkbox_usePSB_OUT.setChecked(usePSB_OUT_checked);
			  
			}
		
		
		private void savePreferences() 
		{
			
			
			
				  
				  boolean useBAB_IN_checked = checkbox_useBAB_IN.isChecked();
				  boolean useCB_IN_checked = checkbox_useCB_IN.isChecked();
				  boolean usePSB_IN_checked = checkbox_usePSB_IN.isChecked();
				  
				  boolean useBAB_OUT_checked = checkbox_useBAB_OUT.isChecked();
				  boolean useCB_OUT_checked = checkbox_useCB_OUT.isChecked();
				  boolean usePSB_OUT_checked = checkbox_usePSB_OUT.isChecked();
				  
				  Editor editor = prefs.edit();
				  editor.putBoolean(useBAB_IN, useBAB_IN_checked);
				  editor.putBoolean(useCB_IN, useCB_IN_checked);
				  editor.putBoolean(usePSB_IN, usePSB_IN_checked);
				  
				  editor.putBoolean(useBAB_OUT, useBAB_OUT_checked);
				  editor.putBoolean(useCB_OUT, useCB_OUT_checked);
				  editor.putBoolean(usePSB_OUT, usePSB_OUT_checked);
				  
				  
				  editor.commit();
		}
		
		
}

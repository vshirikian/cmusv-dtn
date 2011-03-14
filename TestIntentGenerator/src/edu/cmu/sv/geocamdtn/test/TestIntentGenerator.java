package edu.cmu.sv.geocamdtn.test;

import edu.cmu.sv.geocamdtn.lib.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class TestIntentGenerator extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void sendIntent(View view) {
		Bundle data = new Bundle();

		data.putStringArray("test", new String[] {"data1", "data2"});

		Intent geoCamDTNIntent = new Intent(Constants.ACTION_CREATE_DTN_BUNDLE);
		geoCamDTNIntent.putExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD, data);
		Log.i("TestIntentGenerator", "Sending intent");
		this.startService(geoCamDTNIntent);
    }
}
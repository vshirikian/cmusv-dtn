package edu.cmu.sv.geocamdtn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.cmu.sv.geocamdtn.lib.Constants;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class CMUSVBroadcastReceiver extends BroadcastReceiver {

	int index;
	public CMUSVBroadcastReceiver() {
		index = 0;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle dtnBundle = intent.getBundleExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD);
			StringBuffer buf = new StringBuffer();
			String payloadType = null;
			String msg = null;
			if (dtnBundle.getInt(Constants.DTN_PAYLOAD_TYPE) == Constants.DTN_MEM_PAYLOAD) {
				payloadType = "String in memory";
				msg = dtnBundle.getString(Constants.DTN_PAYLOAD_KEY);
			} else { 
				// it is a file lets read it in 
				payloadType = "String in a file";
				msg = dtnBundle.getString(Constants.DTN_PAYLOAD_KEY);				
			}

			buf.append(String.format(
							"Receive from %s , payload type = %s \n", dtnBundle
									.getString(Constants.DTN_SRC_EID_KEY),
							payloadType));
			buf.append(String.format("\n\t\t %s", msg));
		String ticker = String.format("DTNBundle from %s", dtnBundle.getString(Constants.DTN_SRC_EID_KEY));
		postNotification(context, ticker, buf.toString());
	}
	
	private void postNotification(Context context, String tickerText, String msg) {
		int icon =  R.drawable.icon;        // icon from resources
		// CharSequence tickerText = "Hello";              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		CharSequence contentTitle = "Geocam Lens notification";  // expanded message title
		CharSequence contentText = msg;      // expanded message text

		Intent notificationIntent = new Intent(context, CMUSVGeoCamDTN.class);
		notificationIntent.putExtra(Constants.IKEY_DTN_BUNDLE_PAYLOAD, msg);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		NotificationManager notifier = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifier.notify(index++, notification);
	}

}
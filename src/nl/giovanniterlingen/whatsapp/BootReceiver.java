package nl.giovanniterlingen.whatsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {

		Intent service = new Intent(context, MessageService.class);
		context.startService(service);

	}
}

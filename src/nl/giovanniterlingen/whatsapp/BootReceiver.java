package nl.giovanniterlingen.whatsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class BootReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {

		Intent service = new Intent(context, MessageService.class);
		context.startService(service);

	}
}

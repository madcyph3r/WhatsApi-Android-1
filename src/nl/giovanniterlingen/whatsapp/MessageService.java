package nl.giovanniterlingen.whatsapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class MessageService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
		.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(MessageService.this);
		WhatsApi wa = null;

		try {
			wa = new WhatsApi(MessageService.this, preferences.getString(
					"number", ""), "WhatsApi", preferences.getString(
					"username", ""));

			wa.connect();
			wa.loginWithPassword(preferences.getString("pw", ""));

			//Error here
			MessageProcessor mp = new MessageProcessing();
			wa.setNewMessageBind(mp);
			//What to do?

		} catch (Exception e) {
			Toast.makeText(MessageService.this,
					"Caught exception: " + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
			wa.disconnect();
		}
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
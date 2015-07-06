package nl.giovanniterlingen.whatsapp;

import android.support.v7.app.ActionBarActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Conversations extends ActionBarActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		setContentView(R.layout.conversations);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		WhatsApi wa = null;
		try {
			wa = new WhatsApi(Conversations.this, preferences.getString(
					"number", ""), "WhatsApi", preferences.getString(
					"username", ""));
			wa.connect(); // Maybe I was forgotten this one
			wa.loginWithPassword(preferences.getString("pw", ""));

			sendTextMessage(wa);

		} catch (Exception e) {
			Toast.makeText(this, "Caught exception: " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private static void sendTextMessage(WhatsApi wa) throws WhatsAppException {
		String to = "31612345678";
		String message = "Test message from Giovanni's WhatsApp client";
		wa.sendMessage(to, message);
	}
}
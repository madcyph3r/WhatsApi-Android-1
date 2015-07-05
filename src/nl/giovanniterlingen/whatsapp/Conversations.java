package nl.giovanniterlingen.whatsapp;

import android.support.v7.app.ActionBarActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Conversations extends ActionBarActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversations);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		WhatsApi wa = null;
		try {
			wa = new WhatsApi(Conversations.this, preferences.getString(
					"number", ""), "WhatsApi", preferences.getString(
					"username", ""));
			wa.loginWithPassword(preferences.getString("pw", ""));

		} catch (Exception e) {
			Toast.makeText(this, "Caught exception: " + e.getMessage(),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			if (wa != null) {
				wa.disconnect();
			}
		}
	}
}
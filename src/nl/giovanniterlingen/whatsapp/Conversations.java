package nl.giovanniterlingen.whatsapp;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;

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

		try {
			WhatsApi wa = new WhatsApi(Conversations.this,
					preferences.getString("number", ""), "WhatsApi",
					preferences.getString("username", ""));
			wa.loginWithPassword(preferences.getString("pw", ""));
		} catch (NoSuchAlgorithmException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		} catch (WhatsAppException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		}

	}
}
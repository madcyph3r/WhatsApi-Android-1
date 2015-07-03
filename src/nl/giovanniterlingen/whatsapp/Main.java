/**
 * This file should search for an existing password and try to login when the
 * app starts, however we have to work with shared preferences, I will add them soon.
 */

package nl.giovanniterlingen.whatsapp;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class Main extends Activity {

	protected void onCreate(Bundle savedInstanceState) {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String pw = preferences.getString("pw", "");
		if (pw.length() == 0) {

			Intent intent = new Intent(this, RegisterActivity.class);
			startActivity(intent);
			super.onCreate(savedInstanceState);
			finish();
			return;

		} else {

			String number = preferences.getString("number", "");
			String username = preferences.getString("username", "");

			WhatsApi wa = null;

			try {
				wa = new WhatsApi(this, number, "WhatsApi", username);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WhatsAppException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				wa.loginWithPassword(pw);

				Intent intent = new Intent(this, Conversations.class);
				startActivity(intent);
				finish();
				return;

			} catch (WhatsAppException e) {

				Intent intent = new Intent(this, RegisterActivity.class);
				startActivity(intent);
				finish();
				return;
			}
		}

	}

}
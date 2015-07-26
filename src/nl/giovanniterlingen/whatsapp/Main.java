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

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class Main extends Activity {

	protected void onCreate(Bundle savedInstanceState) {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String password = preferences.getString("pw", "");
		if (password.length() == 0) {

			Intent intent = new Intent(this, RegisterActivity.class);
			startActivity(intent);
			super.onCreate(savedInstanceState);
			finish();
			return;

		} else {

			Intent service = new Intent(this, MessageService.class);
			this.startService(service);
			
			Intent intent = new Intent(this, ConversationsList.class);
			startActivity(intent);
			super.onCreate(savedInstanceState);
			finish();
			return;
		}

	}

}
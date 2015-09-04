package nl.giovanniterlingen.whatsapp;

import java.io.UnsupportedEncodingException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class RegisterActivity extends AppCompatActivity {

	ImageButton mButton;
	ImageButton rButton;
	EditText mEdit;
	EditText mUser;
	EditText mVerify;
	EditText mCountryCode;
	private WhatsApi wa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registeractivity);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		mButton = (ImageButton) findViewById(R.id.verify_button);
		rButton = (ImageButton) findViewById(R.id.register_button);
		mEdit = (EditText) findViewById(R.id.number_text);
		mUser = (EditText) findViewById(R.id.user_text);
		mVerify = (EditText) findViewById(R.id.verficationcode_text);
		mCountryCode = (EditText) findViewById(R.id.country_code);

		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					wa = new WhatsApi(RegisterActivity.this, mCountryCode
							.getText().toString() + mEdit.getText().toString(),
							"WhatsApi", mUser.getText().toString());
					sendRequest(wa);
				} catch (Exception e) {
					Toast.makeText(RegisterActivity.this,
							"Caught exception: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}

		});

		rButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					wa = new WhatsApi(RegisterActivity.this, mCountryCode
							.getText().toString() + mEdit.getText().toString(),
							"WhatsApi", mUser.getText().toString());
					sendRegister(wa);
				} catch (Exception e) {
					Toast.makeText(RegisterActivity.this, e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			}
		});

	}

	private void sendRequest(WhatsApi wa) throws WhatsAppException,
			JSONException, UnsupportedEncodingException {
		JSONObject resp = wa.codeRequest("sms", null, null);

		if (resp.toString().contains("existing")) {

			String password = resp.getString("pw");
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("pw", password);
			editor.putString("number", mCountryCode.getText().toString()
					+ mEdit.getText().toString());
			editor.putString("cc", mCountryCode.getText().toString());
			editor.putString("username", mUser.getText().toString());

			editor.apply();

			startApp();

		} else {

			Toast.makeText(this, "Registration sent: " + resp.toString(2),
					Toast.LENGTH_SHORT).show();

		}
	}

	private void sendRegister(WhatsApi wa) throws JSONException,
			WhatsAppException {
		String code = mVerify.getText().toString();
		if (code == null || code.length() == 0) {
			Toast.makeText(this, "No verification code was entered",
					Toast.LENGTH_SHORT).show();
			return;
		}
		JSONObject res = wa.codeRegister(code);
		Toast.makeText(this, "Registration was succesfull!", Toast.LENGTH_SHORT)
				.show();

		String password = res.getString("pw"); // second try!

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("pw", password);
		editor.putString("number", mCountryCode.getText().toString()
				+ mEdit.getText().toString());
		editor.putString("cc", mCountryCode.getText().toString());
		editor.putString("username", mUser.getText().toString());

		editor.apply();

		startApp();

	}

	private void startApp() throws WhatsAppException {

		Intent service = new Intent(this, MessageService.class);
		this.startService(service);
		
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SYNC_CONTACTS);
		sendBroadcast(i);

		Intent intent = new Intent(this, Main.class);
		startActivity(intent);
		finish();

	}
}
package nl.giovanniterlingen.whatsapp;

import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends ActionBarActivity {

	Button mButton;
	Button rButton;
	EditText mEdit;
	EditText mUser;
	EditText mVerify;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registeractivity);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		mButton = (Button) findViewById(R.id.verify_button);
		rButton = (Button) findViewById(R.id.register_button);
		mEdit = (EditText) findViewById(R.id.number_text);
		mUser = (EditText) findViewById(R.id.user_text);
		mVerify = (EditText) findViewById(R.id.verficationcode_text);

		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				WhatsApi wa = null;
				try {
					wa = new WhatsApi(RegisterActivity.this, mEdit.getText()
							.toString(), "WhatsApi", mUser.getText().toString());
					sendRequest(wa);
					return;
				} catch (Exception e) {
					Toast.makeText(RegisterActivity.this,
							"Caught exception: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
					return;
				}
			}

		});

		rButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				WhatsApi wa = null;

				try {
					wa = new WhatsApi(RegisterActivity.this, mEdit.getText()
							.toString(), "WhatsApi", mUser.getText().toString());
					sendRegister(wa);
					return;
				} catch (Exception e) {
					Toast.makeText(RegisterActivity.this, e.getMessage(),
							Toast.LENGTH_SHORT).show();
					return;
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
			editor.putString("number", mEdit.getText().toString());
			editor.putString("username", mUser.getText().toString());

			editor.apply();
			
			Intent intent = new Intent(this, Main.class);
			startActivity(intent);
			finish();
			
		return;
		}
		else {
			
		Toast.makeText(this, "Registration sent: " + resp.toString(2),
				Toast.LENGTH_SHORT).show();
		
		return;
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
		editor.putString("number", mEdit.getText().toString());
		editor.putString("username", mUser.getText().toString());

		editor.apply();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setText(password);

		Toast.makeText(this, "Password copied to clipboard!",
				Toast.LENGTH_SHORT).show();

		Intent intent = new Intent(this, Main.class);
		startActivity(intent);
		finish();
		return;

	}
}
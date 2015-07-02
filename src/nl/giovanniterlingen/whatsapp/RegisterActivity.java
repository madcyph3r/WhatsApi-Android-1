package nl.giovanniterlingen.whatsapp;

import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.content.ClipboardManager;
import android.content.Context;
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
	
	
	
	@Override
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
							.toString(), "WhatsApi", mUser.getText()
							.toString());
					sendRequest(wa);
				} catch (Exception e) {
					Toast.makeText(RegisterActivity.this,
							"Caught exception: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}

		});
		
		
		rButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				
			WhatsApi wa = null;
				
			
			try {
				wa = new WhatsApi(RegisterActivity.this, mEdit.getText()
						.toString(), "WhatsApi", mUser.getText()
						.toString());
				sendRegister(wa);
			} catch (Exception e) {
				Toast.makeText(RegisterActivity.this,
						"Caught exception: " + e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
				
			}
			});
		
	}

	private void sendRequest(WhatsApi wa) throws WhatsAppException,
			JSONException, UnsupportedEncodingException {
		JSONObject resp = wa.codeRequest("sms", null, null);
		Toast.makeText(RegisterActivity.this,
				"Registration sent: " + resp.toString(2), Toast.LENGTH_SHORT)
				.show();
	}
	
	
	
	private void sendRegister(WhatsApi wa) throws JSONException, WhatsAppException {
		String code = mVerify.getText()
				.toString();
		if(code == null || code.length() == 0) {
			Toast.makeText(RegisterActivity.this,
					"No verification code was entered", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		JSONObject res = wa.codeRegister(code);
		Toast.makeText(RegisterActivity.this,
				"Registration was succesfull!", Toast.LENGTH_SHORT)
				.show();
		
		JSONArray array = new JSONArray(res);
		JSONObject jObj = array.getJSONObject(0);
		String password = jObj.getString("pw");
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("pw", password);
		editor.putString("number", mEdit.getText()
				.toString());
		editor.putString("username", mUser.getText()
				.toString());
		
		editor.apply();
		    
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setText(password);
		
		Toast.makeText(RegisterActivity.this,		
				"Password has been copied to your clipboard", Toast.LENGTH_SHORT)
				.show();
				
	}
}
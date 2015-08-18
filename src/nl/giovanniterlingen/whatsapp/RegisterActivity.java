package nl.giovanniterlingen.whatsapp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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
	private WhatsApi wa;

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

		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					wa = new WhatsApi(RegisterActivity.this, mEdit.getText()
							.toString(), "WhatsApi", mUser.getText().toString());
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
							.toString(), "WhatsApi", mUser.getText().toString());
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
			editor.putString("number", mEdit.getText().toString());
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
		editor.putString("number", mEdit.getText().toString());
		editor.putString("username", mUser.getText().toString());

		editor.apply();

		startApp();

	}

	private void startApp() throws WhatsAppException {

		ContentResolver cr = RegisterActivity.this.getContentResolver();
		Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cursor.moveToFirst()) {
			ArrayList<String> alContacts = new ArrayList<String>();
			do {
				String id = cursor.getString(cursor
						.getColumnIndex(ContactsContract.Contacts._ID));

				if (Integer
						.parseInt(cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor mCursor = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);
					while (mCursor.moveToNext()) {
						String contactNumber = mCursor
								.getString(mCursor
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						alContacts.add(contactNumber);
						break;
					}
					mCursor.close();
					wa.sendSync(alContacts, null, SyncType.DELTA_BACKGROUND, 0,
							true);
				}

			} while (cursor.moveToNext());
		}

		Intent intent = new Intent(this, Main.class);
		startActivity(intent);
		finish();

	}
}
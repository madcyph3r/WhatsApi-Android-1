package nl.giovanniterlingen.whatsapp;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Conversations extends ActionBarActivity {

	Button sButton;
	Button cButton;
	EditText nEdit;
	EditText mEdit;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversations);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		sButton = (Button) findViewById(R.id.send_button);
		cButton = (Button) findViewById(R.id.contact_button);
		mEdit = (EditText) findViewById(R.id.message_text);
		nEdit = (EditText) findViewById(R.id.contact_text);

		Intent intent = getIntent();
		if (intent.hasExtra("numberpass")) {
			String number = intent.getExtras().getString("numberpass");
			nEdit.setText(number);
		}

		cButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Intent intent = new Intent(Conversations.this, Contacts.class);
				startActivity(intent);

			}
		});

		sButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(Conversations.this);
				WhatsApi wa = null;

				try {
					wa = new WhatsApi(Conversations.this, preferences
							.getString("number", ""), "WhatsApi", preferences
							.getString("username", ""));
					wa.connect();
					wa.loginWithPassword(preferences.getString("pw", ""));
					sendTextMessage(wa);
					return;

				} catch (Exception e) {
					Toast.makeText(Conversations.this,
							"Caught exception: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					return;
				}
			}

		});
	}

	private void sendTextMessage(WhatsApi wa) throws WhatsAppException {
		String to = nEdit.getText().toString();
		String str = to.replaceAll("\\D+", "");
		String message = mEdit.getText().toString();
		wa.sendMessage(str, message);
		return;
	}
}
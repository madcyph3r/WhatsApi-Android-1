package nl.giovanniterlingen.whatsapp;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	Button mButton;
	EditText mEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		mButton = (Button) findViewById(R.id.verify_button);
		mEdit = (EditText) findViewById(R.id.number_text);

		mButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				WhatsApi wa = null;
				try {
					wa = new WhatsApi(MainActivity.this, mEdit.getText()
							.toString(), "mytestapplication", "mytestaccount");
					sendRequest(wa);
				} catch (Exception e) {
					Toast.makeText(MainActivity.this,
							"Caught exception: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	}

	private void sendRequest(WhatsApi wa) throws WhatsAppException,
			JSONException, UnsupportedEncodingException {
		JSONObject resp = wa.codeRequest("sms", null, null);
		Toast.makeText(MainActivity.this,
				"Registration sent: " + resp.toString(2), Toast.LENGTH_SHORT)
				.show();
	}
}
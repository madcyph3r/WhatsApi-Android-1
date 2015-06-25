package nl.giovanniterlingen.whatsapp;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		WhatsApi wa = null;
		try {
			wa = new WhatsApi(MainActivity.this, "358401122333", "358401122333", "358401122333");
			sendRequest(wa);
		} catch (Exception e) {
			System.out.println("Caught exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void sendRequest(WhatsApi wa) throws WhatsAppException,
			JSONException, UnsupportedEncodingException {
		JSONObject resp = wa.codeRequest("sms", null, null);
		System.out.println("Registration sent: " + resp.toString(2));
	}
}
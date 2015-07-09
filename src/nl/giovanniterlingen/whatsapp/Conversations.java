package nl.giovanniterlingen.whatsapp;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Conversations extends ActionBarActivity {

	Button sButton;
	Button cButton;
	EditText nEdit;
	EditText mEdit;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversations);

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
				finish();

			}
		});

		sButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String to = nEdit.getText().toString();
				String str = to.replaceAll("\\D+", "");
				String message = mEdit.getText().toString();
				Intent i = new Intent();
				i.setAction(MessageService.ACTION_SEND_MSG);
				i.putExtra("to", str);
				i.putExtra("msg", message);
				sendBroadcast(i);
			}
		});
	}
}

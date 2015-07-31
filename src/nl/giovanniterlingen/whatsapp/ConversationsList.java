package nl.giovanniterlingen.whatsapp;

import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ConversationsList extends ActionBarActivity {

	Button cButton;
	private SQLiteDatabase newDB;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversationslist);

		cButton = (Button) findViewById(R.id.contacts_button);

		cButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Intent intent = new Intent(ConversationsList.this,
						Contacts.class);
				startActivity(intent);
				finish();

			}
		});

		DatabaseHelper dbHelper = new DatabaseHelper(
				this.getApplicationContext());

		newDB = dbHelper.getWritableDatabase();

		List<String> all = dbHelper.getContacts(newDB);

		ListView lv = (ListView) findViewById(R.id.conversationslist);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				ConversationsList.this, android.R.layout.simple_list_item_1,
				all);

		lv.setAdapter(adapter);

		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				String number = (String) parent.getItemAtPosition(position);
				Intent i = new Intent(ConversationsList.this,
						Conversations.class);
				i.putExtra("numberpass", number);
				startActivity(i);

			}

		};
		
		lv.setOnItemClickListener(listener);
		
		lv.setItemsCanFocus(true);
		
	}

}
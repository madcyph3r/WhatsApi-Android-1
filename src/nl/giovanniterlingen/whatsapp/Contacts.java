package nl.giovanniterlingen.whatsapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class Contacts extends AppCompatActivity {

	private SQLiteDatabase newDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		ContactsDatabaseHelper dbHelper = new ContactsDatabaseHelper(
				this.getApplicationContext());
		newDB = dbHelper.getWritableDatabase();

		final ContactsAdapter adapter = new ContactsAdapter(Contacts.this,
				ContactsDatabaseHelper.getContacts(newDB), 0);

		ListView lv = (ListView) findViewById(R.id.contactslistview);

		lv.setAdapter(adapter);

		OnItemClickListener listener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				Cursor cur = (Cursor) adapter.getItem(position);
				cur.moveToPosition(position);
				String contact = cur.getString(cur
						.getColumnIndexOrThrow("number"));
				String group = contact.replaceAll("@g.us", "");
				String trim = group.replaceAll("@s.whatsapp.net", "");

				Intent i = new Intent(Contacts.this, Conversations.class);
				i.putExtra("numberpass", trim);
				startActivity(i);
			}

		};

		lv.setOnItemClickListener(listener);

		lv.setItemsCanFocus(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; goto parent activity.
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_ONLINE);
		sendBroadcast(i);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_OFFLINE);
		sendBroadcast(i);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

}
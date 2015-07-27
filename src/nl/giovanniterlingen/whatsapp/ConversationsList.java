package nl.giovanniterlingen.whatsapp;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ConversationsList extends ListActivity {

	private SQLiteDatabase newDB;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DatabaseHelper dbHelper = new DatabaseHelper(
				this.getApplicationContext());

		newDB = dbHelper.getWritableDatabase();

		List<String> all = dbHelper.getContacts(newDB);
		if (all.size() > 0) // check if list contains items.
		{

			setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, all));

		} else {
			Toast.makeText(ConversationsList.this, "No items to display", 1000)
					.show();
		}

	}
	public void onListItemClick(ListView l, View v, int position, long id) {

		String number = (String) getListAdapter().getItem(position);
		Intent i = new Intent(this, Conversations.class);
		i.putExtra("numberpass", number);
		startActivity(i);

		
	}

}
package nl.giovanniterlingen.whatsapp;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

@SuppressWarnings("deprecation")
public class Contacts extends ListActivity implements OnItemClickListener {

	/**
	 * A quick try for obtaining a contacts list
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		String[] projection = new String[] { People._ID, People.NAME };
		Cursor cursor = managedQuery(People.CONTENT_URI, projection, null,
				null, People.NAME + " ASC");

		ListAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.two_line_list_item, cursor,
				new String[] { People.NAME }, new int[] { android.R.id.text1 });
		setListAdapter(adapter);

		getListView().setOnItemClickListener(this);

	}

	public void onItemClick(AdapterView<?> adapterView, View view,
			int position, long id) {

		// TODO add intent

	}

}
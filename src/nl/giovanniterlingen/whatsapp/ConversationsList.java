package nl.giovanniterlingen.whatsapp;

import java.util.List;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ConversationsList extends AppCompatActivity {

	FloatingActionButton cButton;
	private DrawerLayout mDrawerLayout;
	private LinearLayout searchContainer;
	private EditText toolbarSearchView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversationslist);

		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		searchContainer = (LinearLayout) findViewById(R.id.search_container);
		searchContainer.setVisibility(View.GONE);
		toolbarSearchView = (EditText) findViewById(R.id.search_view);
		ImageView searchClearButton = (ImageView) findViewById(R.id.search_clear);

		toolbarSearchView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		searchClearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toolbarSearchView.setText("");
			}
		});

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		if (navigationView != null) {
			setupDrawerContent(navigationView);
		}

		Cursor UsernameCursor = getApplication().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
		UsernameCursor.moveToFirst();
		TextView mUsername = (TextView) findViewById(R.id.username);
		mUsername.setText(UsernameCursor.getString(UsernameCursor.getColumnIndex("display_name")));
		UsernameCursor.close();

		cButton = (FloatingActionButton) findViewById(R.id.contacts_button);

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

		SQLiteDatabase newDB = dbHelper.getWritableDatabase();

		List<String> all = dbHelper.getContacts(newDB);

		ListView lv = (ListView) findViewById(R.id.conversationslist);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.search){
			if (searchContainer.isShown()){
				searchContainer.setVisibility(View.GONE);
			} else {
				searchContainer.setVisibility(View.VISIBLE);
			}
		}

		switch (item.getItemId()) {
			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupDrawerContent(NavigationView navigationView) {
		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {
						menuItem.setChecked(true);
						mDrawerLayout.closeDrawers();
						int id = menuItem.getItemId();

						if (id == R.id.action_info) {
							Intent intent = new Intent(ConversationsList.this, InfoActivity.class);
							startActivity(intent);
							finish();
							return true;
						}
						if (id == R.id.change_status) {
							Intent intent = new Intent(ConversationsList.this, StatusActivity.class);
							startActivity(intent);
							finish();
							return true;
						}
						return true;
					}
				});
	}
	
	protected void onResume() {
		super.onResume();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_ONLINE);
		sendBroadcast(i);
	}

	protected void onPause() {
		super.onPause();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_OFFLINE);
		sendBroadcast(i);
	}

}
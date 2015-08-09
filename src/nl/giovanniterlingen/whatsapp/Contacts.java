package nl.giovanniterlingen.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class Contacts extends FragmentActivity implements
		OnContactSelectedListener {
	public static final String SELECTED_CONTACT_ID = "contact_id";
	public static final String KEY_PHONE_NUMBER = "phone_number";
	public static final String KEY_CONTACT_NAME = "contact_name";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

		FragmentManager fragmentManager = this.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		ContactsListFragment fragment = new ContactsListFragment();

		fragmentTransaction.add(R.id.fragment_container, fragment);
		fragmentTransaction.commit();
	}

	@Override
	public void onContactNameSelected(long contactId) {
		// TODO AUTO GENERATED
	}

	@Override
	public void onContactNumberSelected(String contactNumber, String contactName) {
		String number = contactNumber.replaceAll("\\D+", "");
		Intent i = new Intent(this, Conversations.class);
		i.putExtra("numberpass", number);
		startActivity(i);

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
	
	public void onBackPressed() {
		Intent intent = new Intent(this, Main.class);
		startActivity(intent);
		finish();
		return;
	}

}
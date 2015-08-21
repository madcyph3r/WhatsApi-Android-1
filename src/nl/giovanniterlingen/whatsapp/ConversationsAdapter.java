package nl.giovanniterlingen.whatsapp;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ConversationsAdapter extends CursorAdapter {

	public ConversationsAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, 0);
	}

	// The newView method is used to inflate a new view and return it,
	// you don't bind any data to the view at this point.
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.conversations_row,
				parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Find fields to populate in inflated template
		TextView name = (TextView) view.findViewById(R.id.display_name);
		TextView message = (TextView) view.findViewById(R.id.display_message);
		TextView date = (TextView) view.findViewById(R.id.display_date);
		// Extract properties from cursor
		String to = cursor.getString(cursor.getColumnIndexOrThrow("to"));
		String from = cursor.getString(cursor.getColumnIndexOrThrow("from"));
		String txt = cursor.getString(cursor.getColumnIndexOrThrow("message"));
		String time = cursor.getString(cursor.getColumnIndexOrThrow("t"));
		// Parse time
		long datevalue = Long.valueOf(time) * 1000;
		Date dateformat = new java.util.Date(datevalue);
		String convert = new SimpleDateFormat("HH:mm").format(dateformat);

		// Populate fields with extracted properties
		if (to.equals("me")) {

			String contactname = ContactsHelper.getContactName(context, from);

			if (contactname != null) {
				name.setText(contactname);
			} else {
				name.setText(from);
			}
		}
		if (from.equals("me")) {

			String contactname = ContactsHelper.getContactName(context, to);

			if (contactname != null) {
				name.setText(contactname);
			} else {
				name.setText(to);
			}
		}
		message.setText(txt);
		date.setText(convert);

	}
}

package nl.giovanniterlingen.whatsapp;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
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
public class ChatAdapter extends CursorAdapter {

	public ChatAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, 0);
	}

	// The newView method is used to inflate a new view and return it,
	// you don't bind any data to the view at this point.
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.chat_item, parent,
				false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Find fields to populate in inflated template
		TextView left = (TextView) view.findViewById(R.id.lefttext);
		TextView right = (TextView) view.findViewById(R.id.righttext);
		LinearLayout rightBubble = (LinearLayout) view
				.findViewById(R.id.right_bubble);
		LinearLayout leftBubble = (LinearLayout) view
				.findViewById(R.id.left_bubble);
		TextView leftDate = (TextView) view.findViewById(R.id.leftdate);
		TextView rightDate = (TextView) view.findViewById(R.id.rightdate);
		// Extract properties from cursor
		String from = cursor.getString(cursor.getColumnIndexOrThrow("from"));
		String txt = cursor.getString(cursor.getColumnIndexOrThrow("message"));
		String date = cursor.getString(cursor.getColumnIndexOrThrow("t"));
		String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
		// Parse time
		long datevalue = Long.valueOf(date) * 1000;
		Date dateformat = new java.util.Date(datevalue);
		String convert = new SimpleDateFormat("HH:mm").format(dateformat);

		// Populate fields with extracted properties
		if (from.equals("me")) {

			right.setText(txt);
			left.setText("");
			rightBubble.setBackgroundResource(R.drawable.balloon_outgoing_normal);
			leftBubble.setBackgroundDrawable(null);
			rightDate.setText(convert);
			leftDate.setVisibility(View.GONE);

		}

		else {

			left.setText(txt);
			right.setText("");
			leftBubble.setBackgroundResource(R.drawable.balloon_incoming_normal);
			rightBubble.setBackgroundDrawable(null);
			leftDate.setText(convert);
			rightDate.setVisibility(View.GONE);
			
			// send that I have read the message
			Intent i = new Intent();
			i.setAction(MessageService.ACTION_SEND_READ);
			i.putExtra("to", from);
			i.putExtra("id", id);
			context.sendBroadcast(i);

		}
	}

}

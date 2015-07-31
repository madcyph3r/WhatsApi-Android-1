package nl.giovanniterlingen.whatsapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import nl.giovanniterlingen.whatsapp.DatabaseContract.DbEntries;
import nl.giovanniterlingen.whatsapp.message.Message;
import nl.giovanniterlingen.whatsapp.message.TextMessage;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class MessageProcessing implements MessageProcessor {

	private Context context;

	public MessageProcessing(Context context) {
		this.context = context;
	}

	public void processMessage(ProtocolNode message, String textmessage) {
		String from = message.getAttribute("from");
		String extract = from.replaceAll("^[^-]*-", "");
		String group = extract.replaceAll("@g.us", "");
		String trim = group.replaceAll("@s.whatsapp.net", "");
		if (message.getAttribute("type").equals("text")) {
			String participant = message.getAttribute("participant");
			String id = message.getAttribute("id");
			String t = message.getAttribute("t");
			if (participant != null && !participant.isEmpty()) {
				// Group message
				
				DatabaseHelper mDbHelper = new DatabaseHelper(context);

				SQLiteDatabase db = mDbHelper.getWritableDatabase();

				String query = "INSERT INTO " + DbEntries.TABLE_NAME + " ("
						+ DbEntries.COLUMN_NAME_FROM + ", "
						+ DbEntries.COLUMN_NAME_TO + ", "
						+ DbEntries.COLUMN_NAME_MESSAGE + ", "
						+ DbEntries.COLUMN_NAME_ID + ", "
						+ DbEntries.COLUMN_NAME_TIME + ") VALUES ('" + trim + "', " + "'me'"
						+ ", '" + textmessage + "', '" + id + "', '" + t + "')";

				db.execSQL(query);
				
				db.close();				
			} else {
				// Private message
				
				DatabaseHelper mDbHelper = new DatabaseHelper(context);

				SQLiteDatabase db = mDbHelper.getWritableDatabase();

				String query = "INSERT INTO " + DbEntries.TABLE_NAME + " ("
						+ DbEntries.COLUMN_NAME_FROM + ", "
						+ DbEntries.COLUMN_NAME_TO + ", "
						+ DbEntries.COLUMN_NAME_MESSAGE + ", "
						+ DbEntries.COLUMN_NAME_ID + ", "
						+ DbEntries.COLUMN_NAME_TIME + ") VALUES ('" + trim + "', " + "'me'"
						+ ", '" + textmessage + "', '" + id + "', '" + t + "')";

				
				db.execSQL(query);
				
				db.close();
			}
		}
	}

	public void processMessage(Message message) {
		// TODO add all supported message types
		switch (message.getType()) {
		case TEXT:
			final TextMessage msg = (TextMessage) message;
			
			String textmessage = msg.getText();
			
			if (msg.getGroupId() != null && !msg.getGroupId().isEmpty()) {
				// Group message
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								context,
								msg.getFrom() + "(" + msg.getGroupId() + "): "
										+ msg.getText(), Toast.LENGTH_LONG)
								.show();
					}
				});
				processMessage(message.getProtocolNode(), textmessage);
			} else {
				// Private message
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context,
								msg.getFrom() + " : " + msg.getText(),
								Toast.LENGTH_LONG).show();
					}
				});
				processMessage(message.getProtocolNode(), textmessage);
			}
			break;
		}
	}

}
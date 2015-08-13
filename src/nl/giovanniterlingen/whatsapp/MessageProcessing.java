package nl.giovanniterlingen.whatsapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
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
						+ DbEntries.COLUMN_NAME_TIME + ") VALUES ("
						+ DatabaseUtils.sqlEscapeString(trim) + ", " + "'me'"
						+ ", " + DatabaseUtils.sqlEscapeString(textmessage)
						+ ", " + DatabaseUtils.sqlEscapeString(id) + ", "
						+ DatabaseUtils.sqlEscapeString(t) + ")";

				db.execSQL(query);

				db.close();
				
				Intent intent = new Intent(Conversations.SET_NOTIFY);
				context.sendBroadcast(intent);
			} else {
				// Private message

				DatabaseHelper mDbHelper = new DatabaseHelper(context);

				SQLiteDatabase db = mDbHelper.getWritableDatabase();

				String query = "INSERT INTO " + DbEntries.TABLE_NAME + " ("
						+ DbEntries.COLUMN_NAME_FROM + ", "
						+ DbEntries.COLUMN_NAME_TO + ", "
						+ DbEntries.COLUMN_NAME_MESSAGE + ", "
						+ DbEntries.COLUMN_NAME_ID + ", "
						+ DbEntries.COLUMN_NAME_TIME + ") VALUES ("
						+ DatabaseUtils.sqlEscapeString(trim) + ", " + "'me'"
						+ ", " + DatabaseUtils.sqlEscapeString(textmessage)
						+ ", " + DatabaseUtils.sqlEscapeString(id) + ", "
						+ DatabaseUtils.sqlEscapeString(t) + ")";

				db.execSQL(query);

				db.close();
				
				Intent intent = new Intent(Conversations.SET_NOTIFY);
				context.sendBroadcast(intent);
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

						NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
								context)
								.setSmallIcon(R.drawable.notifybar)
								.setContentTitle(
										"Groupmessage from "
												+ ContactsHelper
														.getContactName(
																context,
																msg.getFrom()))
								.setContentText(msg.getText())
								.setPriority(Notification.PRIORITY_HIGH)
								.setDefaults(Notification.DEFAULT_VIBRATE)
								.setAutoCancel(true);

						String number = msg.getFrom();
						Intent resultIntent = new Intent(context,
								Conversations.class);
						resultIntent.putExtra("numberpass", number);

						TaskStackBuilder stackBuilder = TaskStackBuilder
								.create(context);

						stackBuilder.addParentStack(Main.class);

						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent = stackBuilder
								.getPendingIntent(0,
										PendingIntent.FLAG_UPDATE_CURRENT);
						mBuilder.setContentIntent(resultPendingIntent);
						NotificationManager mNotificationManager = (NotificationManager) context
								.getSystemService(Context.NOTIFICATION_SERVICE);

						mNotificationManager.notify(0, mBuilder.build());

					}
				});
				processMessage(message.getProtocolNode(), textmessage);
			} else {
				// Private message
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {

						NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
								context)
								.setSmallIcon(R.drawable.notifybar)
								.setContentTitle(
										"Message from "
												+ ContactsHelper
														.getContactName(
																context,
																msg.getFrom()))
								.setContentText(msg.getText())
								.setPriority(Notification.PRIORITY_HIGH)
								.setDefaults(Notification.DEFAULT_VIBRATE)
								.setAutoCancel(true);

						String number = msg.getFrom();
						Intent resultIntent = new Intent(context,
								Conversations.class);
						resultIntent.putExtra("numberpass", number);

						TaskStackBuilder stackBuilder = TaskStackBuilder
								.create(context);

						stackBuilder.addParentStack(Main.class);

						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent = stackBuilder
								.getPendingIntent(0,
										PendingIntent.FLAG_UPDATE_CURRENT);
						mBuilder.setContentIntent(resultPendingIntent);
						NotificationManager mNotificationManager = (NotificationManager) context
								.getSystemService(Context.NOTIFICATION_SERVICE);

						mNotificationManager.notify(0, mBuilder.build());

					}
				});
				processMessage(message.getProtocolNode(), textmessage);
			}
			break;
		}
	}

}
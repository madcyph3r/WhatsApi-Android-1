package nl.giovanniterlingen.whatsapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class MessageService extends Service {

	public static final String ACTION_SEND_MSG = "send_msg";
	public static final String ACTION_START_COMPOSING = "start_composing";
	public static final String ACTION_STOP_COMPOSING = "stop_composing";
	public static final String ACTION_SHOW_ONLINE = "show_online";
	public static final String ACTION_SHOW_OFFLINE = "show_offline";
	public static final String ACTION_SET_STATUS = "set_status";
	private WhatsApi wa;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == ACTION_SEND_MSG) {
				try {
					wa.sendMessage(intent.getStringExtra("to"),
							intent.getStringExtra("msg"));
				} catch (WhatsAppException e) {
					Toast.makeText(MessageService.this,
							"Caught exception: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}

			if (intent.getAction() == ACTION_START_COMPOSING) {
				try {
					wa.sendMessageComposing(intent.getStringExtra("to"));
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_STOP_COMPOSING) {
				try {
					wa.sendMessagePaused(intent.getStringExtra("to"));
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_SHOW_ONLINE) {
				try {
					wa.sendActiveStatus();
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_SHOW_OFFLINE) {
				try {
					wa.sendOfflineStatus();
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_SET_STATUS) {
				try {
					wa.sendStatusUpdate(intent.getStringExtra("status"));
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
		}
	};

	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SEND_MSG);
		filter.addAction(ACTION_START_COMPOSING);
		filter.addAction(ACTION_STOP_COMPOSING);
		filter.addAction(ACTION_SHOW_ONLINE);
		filter.addAction(ACTION_SHOW_OFFLINE);
		filter.addAction(ACTION_SET_STATUS);
		registerReceiver(broadcastReceiver, filter);
		startService();
		return START_STICKY;
	}

	private void startService() {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(MessageService.this);
		try {
			wa = new WhatsApi(MessageService.this, preferences.getString(
					"number", ""), "WhatsApi", preferences.getString(
					"username", ""));

			MessageProcessor mp = new MessageProcessing(MessageService.this);
			wa.setNewMessageBind(mp);

			wa.connect();
			wa.loginWithPassword(preferences.getString("pw", ""));
			wa.sendOfflineStatus();
			return;

		} catch (Exception e) {
			Toast.makeText(MessageService.this,
					"Caught exception: " + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
			wa.disconnect();
			return;
		}
	}

	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
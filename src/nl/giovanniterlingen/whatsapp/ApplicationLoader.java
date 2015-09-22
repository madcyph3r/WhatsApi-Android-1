package nl.giovanniterlingen.whatsapp;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ApplicationLoader extends Application {

	public static volatile Context applicationContext;
	public static volatile Handler applicationHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		applicationContext = getApplicationContext();
		applicationHandler = new Handler(applicationContext.getMainLooper());

		startPushService();
	}

	public static void startPushService() {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(applicationContext);
		String password = preferences.getString("pw", "");
		if (password.length() != 0) {

			applicationContext.startService(new Intent(applicationContext,
					MessageService.class));

			if (android.os.Build.VERSION.SDK_INT >= 19) {
				// Calendar cal = Calendar.getInstance();
				// PendingIntent pintent =
				// PendingIntent.getService(applicationContext, 0, new
				// Intent(applicationContext, NotificationsService.class), 0);
				// AlarmManager alarm = (AlarmManager)
				// applicationContext.getSystemService(Context.ALARM_SERVICE);
				// alarm.setRepeating(AlarmManager.RTC_WAKEUP,
				// cal.getTimeInMillis(), 30000, pintent);

				PendingIntent pintent = PendingIntent.getService(
						applicationContext, 0, new Intent(applicationContext,
								MessageService.class), 0);
				AlarmManager alarm = (AlarmManager) applicationContext
						.getSystemService(Context.ALARM_SERVICE);
				alarm.cancel(pintent);
			}
		}
	}

	public static void runOnUIThread(Runnable runnable) {
		runOnUIThread(runnable, 0);
	}

	public static void runOnUIThread(Runnable runnable, long delay) {
		if (delay == 0) {
			applicationHandler.post(runnable);
		} else {
			applicationHandler.postDelayed(runnable, delay);
		}
	}
}
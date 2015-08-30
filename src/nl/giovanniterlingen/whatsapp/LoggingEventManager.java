package nl.giovanniterlingen.whatsapp;

import java.util.Map;

import nl.giovanniterlingen.whatsapp.events.Event;
import android.util.Log;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class LoggingEventManager extends AbstractEventManager {

	@Override
	public void fireEvent(String event, Map<String, Object> eventData) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("Event " + event + ": ");
		for (String key : eventData.keySet()) {
			if (!first) {
				sb.append(",");
			} else {
				first = false;
			}
			sb.append(key);
			sb.append("=");
			sb.append(eventData.get(key));
		}
		Log.i("INFO", sb.toString());
	}

	@Override
	public void fireEvent(Event event) {
		Log.i("INFO", event.toString());
	}

}

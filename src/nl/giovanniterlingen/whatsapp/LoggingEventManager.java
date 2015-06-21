package nl.giovanniterlingen.whatsapp;

import java.util.Map;

import android.util.Log;

import nl.giovanniterlingen.whatsapp.events.Event;

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

	public void fireEvent(Event event) {
		Log.i("INFO", event.toString());
	}

}

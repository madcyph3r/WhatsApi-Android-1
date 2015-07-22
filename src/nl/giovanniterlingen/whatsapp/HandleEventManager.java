package nl.giovanniterlingen.whatsapp;

import java.util.Map;

import nl.giovanniterlingen.whatsapp.AbstractEventManager;
import nl.giovanniterlingen.whatsapp.events.Event;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class HandleEventManager extends AbstractEventManager {
	@Override
	public void fireEvent(String event, Map<String, Object> eventData) {
		if(event.equals(AbstractEventManager.EVENT_UNKNOWN)) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("Event "+event+": ");
		for(String key: eventData.keySet()) {
			if(!first) {
				sb.append(",");
			} else {
				first = false;
			}
			sb.append(key);
			sb.append("=");
			sb.append(eventData.get(key));
		}
		System.out.println(sb.toString());
	}

	public void fireEvent(Event event) {
		System.out.println(event.toString());
		
	}

}
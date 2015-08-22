package nl.giovanniterlingen.whatsapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import nl.giovanniterlingen.whatsapp.AbstractEventManager;
import nl.giovanniterlingen.whatsapp.events.Event;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class EventProcessor extends AbstractEventManager {

	private Context context;

	public EventProcessor(Context context) {
		this.context = context;
	}

	@Override
	public void fireEvent(String event, Map<String, Object> eventData) {
		if (event.equals(AbstractEventManager.EVENT_UNKNOWN)) {
			return;
		}
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
		System.out.println(sb.toString());

		if (event.equals(AbstractEventManager.EVENT_GET_PROFILE_PICTURE)) {

			if (eventData.get(TYPE).equals("preview")) {

				String filename = "preview_" + eventData.get(FROM) + ".jpg";
				String file = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ File.separator
						+ "WhatsApi"
						+ File.separator + filename;

				FileOutputStream out;
				try {
					out = new FileOutputStream(file);
					if (out != null) {
						byte[] content = serialize(eventData.get(DATA));
						byte[] filteredByteArray = Arrays.copyOfRange(content, 27, content.length - 0);
						out.write(filteredByteArray);
						out.close();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			else {

				String filename = eventData.get(FROM) + ".jpg";
				String file = Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ File.separator
						+ "WhatsApi"
						+ File.separator + filename;
				FileOutputStream out;
				try {
					out = new FileOutputStream(file);
					if (out != null) {
						byte[] content = serialize(eventData.get(DATA));
						byte[] filteredByteArray = Arrays.copyOfRange(content, 27, content.length - 0);
						out.write(filteredByteArray);
						out.close();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (event.equals(AbstractEventManager.EVENT_GET_LAST_SEEN)) {

			Intent i = new Intent();
			i.setAction(Conversations.SET_LAST_SEEN);
			i.putExtra("sec", eventData.get(SECONDS).toString());
			context.sendBroadcast(i);
			
		}
	}

	public void fireEvent(Event event) {
		System.out.println(event.toString());

	}

	public static byte[] serialize(Object obj) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
	}

}
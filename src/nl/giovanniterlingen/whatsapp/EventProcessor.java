package nl.giovanniterlingen.whatsapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
import nl.giovanniterlingen.whatsapp.AbstractEventManager;
import nl.giovanniterlingen.whatsapp.events.Event;

public class EventProcessor extends AbstractEventManager {

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
						byte[] content = toByteArray(eventData.get(DATA));
						out.write(content);
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
						byte[] content = toByteArray(eventData.get(DATA));
						out.write(content);
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
	}

	public void fireEvent(Event event) {
		System.out.println(event.toString());

	}

	public static byte[] toByteArray(Object obj) throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray();
		} finally {
			if (oos != null) {
				oos.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
		return bytes;
	}

}
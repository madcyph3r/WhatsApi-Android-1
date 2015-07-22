package nl.giovanniterlingen.whatsapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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

	public void processMessage(ProtocolNode message) {
		String from = message.getAttribute("from");
		if (message.getAttribute("type").equals("text")) {
			ProtocolNode body = message.getChild("body");
			String hex = new String(body.getData());
			String participant = message.getAttribute("participant");
			if (participant != null && !participant.isEmpty()) {
				// Group message
				System.out.println(participant + "(" + from + ") ::: " + hex);
			} else {
				// Private message
				System.out.println(from + " ::: " + hex);
			}
		}
	}

	public void processMessage(Message message) {
		// TODO add all supported message types
		switch (message.getType()) {
		case TEXT:
			final TextMessage msg = (TextMessage) message;
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
			}
			break;
		default:
			processMessage(message.getProtocolNode());
		}
	}

}
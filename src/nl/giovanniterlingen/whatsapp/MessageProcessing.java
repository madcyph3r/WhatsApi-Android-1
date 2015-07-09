package nl.giovanniterlingen.whatsapp;

import nl.giovanniterlingen.whatsapp.MessageProcessor;
import nl.giovanniterlingen.whatsapp.ProtocolNode;
import nl.giovanniterlingen.whatsapp.message.Message;
import nl.giovanniterlingen.whatsapp.message.TextMessage;

public class MessageProcessing implements MessageProcessor {

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
				System.out.println(msg.getFrom() + "(" + msg.getGroupId()
						+ "): " + msg.getText());
			} else {
				// Private message
				System.out.println(msg.getFrom() + " : " + msg.getText());
			}
			break;
		default:
			processMessage(message.getProtocolNode());
		}
	}

}
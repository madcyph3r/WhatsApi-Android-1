package nl.giovanniterlingen.whatsapp;

import nl.giovanniterlingen.whatsapp.message.Message;

public interface MessageProcessor {
	public void processMessage(Message message);
}

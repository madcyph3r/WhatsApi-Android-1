package nl.giovanniterlingen.whatsapp;

import nl.giovanniterlingen.whatsapp.message.Message;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public interface MessageProcessor {
	public void processMessage(Message message);
}

package nl.giovanniterlingen.whatsapp.message;

import java.util.Date;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public interface Message {
	public String getFrom();
	public MessageType getType();
	public Date getDate();
	public ProtocolNode getProtocolNode();
}

package nl.giovanniterlingen.whatsapp.message;

import java.util.Date;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

public interface Message {
	public String getFrom();
	public MessageType getType();
	public Date getDate();
	public ProtocolNode getProtocolNode();
}

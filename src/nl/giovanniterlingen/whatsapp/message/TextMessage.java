package nl.giovanniterlingen.whatsapp.message;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class TextMessage extends BasicMessage {
	private String text;
	
	public TextMessage(ProtocolNode node, String from, String group) {
		super(MessageType.TEXT,node, from, group);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		if(getGroupId() != null) {
			return getDate()+" :: "+getFrom()+"("+getGroupId()+"): "+text;
		}
		return getDate()+" :: "+getFrom()+": "+text;
	}
}

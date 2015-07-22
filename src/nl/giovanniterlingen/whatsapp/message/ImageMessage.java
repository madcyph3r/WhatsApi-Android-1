package nl.giovanniterlingen.whatsapp.message;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ImageMessage extends MediaMessage {

	public ImageMessage(ProtocolNode node, String from,
			String groupId) {
		super(MessageType.IMAGE, node, from, groupId);
	}

}

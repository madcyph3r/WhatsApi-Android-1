package nl.giovanniterlingen.whatsapp.message;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class VideoMessage extends MediaMessage {

	public VideoMessage(ProtocolNode node, String from,
			String groupId) {
		super(MessageType.VIDEO, node, from, groupId);
	}

}

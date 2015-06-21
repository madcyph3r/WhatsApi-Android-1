package nl.giovanniterlingen.whatsapp.message;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

public class ImageMessage extends MediaMessage {

	public ImageMessage(ProtocolNode node, String from,
			String groupId) {
		super(MessageType.IMAGE, node, from, groupId);
	}

}

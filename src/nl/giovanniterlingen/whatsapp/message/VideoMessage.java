package nl.giovanniterlingen.whatsapp.message;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

public class VideoMessage extends MediaMessage {

	public VideoMessage(ProtocolNode node, String from,
			String groupId) {
		super(MessageType.VIDEO, node, from, groupId);
	}

}

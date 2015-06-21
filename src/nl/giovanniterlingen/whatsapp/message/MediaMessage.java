package nl.giovanniterlingen.whatsapp.message;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

public abstract class MediaMessage extends BasicMessage {

	private String caption = null;
	private byte[] preview = null;
	private String content = null;
	
	public MediaMessage(MessageType type, ProtocolNode node, String from,
			String groupId) {
		super(type, node, from, groupId);
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public byte[] getPreview() {
		return preview;
	}

	public void setPreview(byte[] preview) {
		this.preview = preview;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	

}

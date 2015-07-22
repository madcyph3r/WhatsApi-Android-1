package nl.giovanniterlingen.whatsapp.message;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class AudioMessage extends MediaMessage {
	
	private String seconds = "0";
	private String size = "0";
	private String file = null;
	private String origin = null;
	private String ip = null;
	private String mimetype = null;
	private String fileHash = null;
	private String duration = "0";
	private String acodec = null;
	private String asampfreq = null;
	private String abitrate = null;

	public AudioMessage(ProtocolNode node, String from,
			String groupId) {
		super(MessageType.AUDIO, node, from, groupId);
	}

	public String getSeconds() {
		return seconds;
	}

	public void setSeconds(String seconds) {
		this.seconds = seconds;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getFileHash() {
		return fileHash;
	}

	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getAcodec() {
		return acodec;
	}

	public void setAcodec(String acodec) {
		this.acodec = acodec;
	}

	public String getAsampfreq() {
		return asampfreq;
	}

	public void setAsampfreq(String asampfreq) {
		this.asampfreq = asampfreq;
	}

	public String getAbitrate() {
		return abitrate;
	}

	public void setAbitrate(String abitrate) {
		this.abitrate = abitrate;
	}

}

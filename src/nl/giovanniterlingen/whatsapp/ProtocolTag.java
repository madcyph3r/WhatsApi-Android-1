package nl.giovanniterlingen.whatsapp;

public enum ProtocolTag {
	START("start"),CHALLENGE("challenge"),
	ACK("ack"),RECEIPT("receipt"),
	SUCCESS("success"), FAILURE("failure"), MESSAGE("message"), PRESENCE("presence"), 
	IB("ib"), IQ("iq"),PING("ping"),QUERY("query"), 
	DIRTY("dirty"), OFFLINE("offline"),
	NOTIFICATION("notification"),CHATSTATE("chatstate"),
	UNKNOWN("unknown"), STREAM_ERROR("stream:error"), SYNC("sync"), GROUP("group");
	
	private String tag;

	private ProtocolTag(String tag) {
		this.tag = tag;
	}
	@Override
	public String toString() {
		return tag;
	}
	public static ProtocolTag fromString(String text) {
	    if (text != null) {
	        for (ProtocolTag b : ProtocolTag.values()) {
	          if (text.equalsIgnoreCase(b.toString())) {
	            return b;
	          }
	        }
	      }
	      return null;
	}
	
}

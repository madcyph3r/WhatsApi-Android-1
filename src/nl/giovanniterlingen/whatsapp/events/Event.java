package nl.giovanniterlingen.whatsapp.events;

import java.util.Collections;
import java.util.List;

import nl.giovanniterlingen.whatsapp.ProtocolNode;

public class Event {
	private final EventType type;
	private final String phoneNumber; 
	private List<ProtocolNode> data;
	private String groupId = null;
	private String from;
	private Object eventSpecificData;
	
	public Event(EventType type, String phoneNumber) {
		this.type = type;
		this.phoneNumber = phoneNumber;
	}
	
	public EventType getType() {
		return type;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public List<ProtocolNode> getData() {
		if(data == null) {
			return Collections.emptyList();
		}
		return data;
	}

	public void setData(List<ProtocolNode> data) {
		this.data = data;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		sb.append("Event ");
		sb.append(type);
		sb.append(" to ");
		sb.append(phoneNumber);
		if(groupId != null) {
			sb.append("("+groupId+")");
		}
		sb.append(": ");
		if(data != null) {
			for(ProtocolNode node : data) {
				if(!first) {
					sb.append(",");
				} else {
					first = false;
				}

				sb.append(node.toString());
			}
		}
		if(eventSpecificData != null) {
			sb.append(eventSpecificData.toString());
		}
		return sb.toString();
	}

	public Object getEventSpecificData() {
		return eventSpecificData;
	}

	public void setEventSpecificData(Object eventSpecificData) {
		this.eventSpecificData = eventSpecificData;
	}
}

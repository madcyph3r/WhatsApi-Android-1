package nl.giovanniterlingen.whatsapp;

import java.util.List;
import java.util.Map;

public class SyncResult {

	private String index;
	private String sid;
	private Map<String, String> existingUsers;
	private List<String> failedNumbers;

	public SyncResult(String index, String attribute,
			Map<String, String> existingUsers, List<String> failedNumbers) {
		this.index = index;
		this.sid = attribute;
		this.existingUsers = existingUsers;
		this.failedNumbers = failedNumbers;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public Map<String, String> getExistingUsers() {
		return existingUsers;
	}

	public void setExistingUsers(Map<String, String> existingUsers) {
		this.existingUsers = existingUsers;
	}

	public List<String> getFailedNumbers() {
		return failedNumbers;
	}

	public void setFailedNumbers(List<String> failedNumbers) {
		this.failedNumbers = failedNumbers;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SID: ");
		sb.append(sid);
		sb.append(";");
		sb.append("Index: ");
		sb.append(index);
		sb.append(";");
		if(existingUsers != null) {
			boolean first = true;
			sb.append("Existing users: (");
			for(String key : existingUsers.keySet()) {
				if(!first) {
					sb.append(",");
				} else {
					first = false;
				}
				sb.append(key);
				sb.append("=");
				sb.append(existingUsers.get(key));
			}
			sb.append(");");
		}
		if(failedNumbers != null) {
			boolean first = true;
			sb.append("Not existing users: (");
			for(String key : failedNumbers) {
				if(!first) {
					sb.append(",");
				} else {
					first = false;
				}
				sb.append(key);
			}
			sb.append(");");
		}
		return sb.toString();
	}
}

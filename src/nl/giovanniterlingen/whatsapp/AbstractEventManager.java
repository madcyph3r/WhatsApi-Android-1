package nl.giovanniterlingen.whatsapp;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public abstract class AbstractEventManager implements EventManager {

	public static final String EVENT_MESSAGE_RECEIVED_SERVER = "message_received_server";
	public static final String EVENT_MESSAGE_PAUSED = "message_paused";
	public static final String EVENT_MESSAGE_RECEIVED_CLIENT = "message_received_client";
	public static final String EVENT_MESSAGE_COMPOSING = "message_composing";
	public static final String EVENT_GET_PROFILE_PICTURE = "get_profile_picture";
	// Events
	public static final String EVENT_UNKNOWN = "UNKNOWN";
	public static final String EVENT_GROUPS_PARTICIPANTS_ADD = "GROUPS_PARTICIPANTS_ADD";
	public static final String EVENT_GROUPS_PARTICIPANTS_REMOVE = "GROUPS_PARTICIPANTS_REMOVE";
	public static final String EVENT_PONG = "PONG";
	public static final String EVENT_PRESENCE = "PRESENCE";

	// Event fields
	public static final String PHONE_NUMBER = "phoneNumber";
	public static final String MSG_ID = "msgId";
	public static final String GROUP_ID = "groupId";
	public static final String JID = "jid";
	public static final String JID2 = "jid2";
	public static final String FROM = "from";
	public static final String TYPE = "type";
	public static final String RESULT = "result";
	public static final String TIME = "time";
	public static final String DATA = "data";

	public abstract void fireEvent(String event, Map<String,Object> eventData);

	/* (non-Javadoc)
	 * @see nl.giovanniterlingen.whatsapp.EventManager#fireSendPong(java.lang.String, java.lang.String)
	 */
	public void fireSendPong(String phone, String msgid) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(MSG_ID, msgid);
		fireEvent(EVENT_PONG, eventData);
	}


	/* (non-Javadoc)
	 * @see nl.giovanniterlingen.whatsapp.EventManager#fireGroupsParticipantsAdd(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void fireGroupsParticipantsAdd(String phone, String groupId,
			String parseJID) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(GROUP_ID, groupId);
		eventData.put(JID, parseJID);
		fireEvent(EVENT_GROUPS_PARTICIPANTS_ADD, eventData);
	}

	/* (non-Javadoc)
	 * @see nl.giovanniterlingen.whatsapp.EventManager#fireGroupsParticipantsRemove(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void fireGroupsParticipantsRemove(String phone,
			String groupId, String parseJID, String parseJID2) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(GROUP_ID, groupId);
		eventData.put(JID, parseJID);
		eventData.put(JID2, parseJID2);
		fireEvent(EVENT_GROUPS_PARTICIPANTS_REMOVE, eventData);
	}

	/* (non-Javadoc)
	 * @see nl.giovanniterlingen.whatsapp.EventManager#firePresence(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void firePresence(String phone, String from,
			String type) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(FROM, from);
		eventData.put(TYPE, type);
		fireEvent(EVENT_PRESENCE, eventData);
	}

	public void fireClose(String phone, String error) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("close", eventData);
	}

	public void fireCodeRegister(String phone, String login, String pw,
			String type, String expiration, String kind, String price,
			String cost, String currency, String price_expiration) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("code_register", eventData);
	}

	public void fireCodeRegisterFailed(String phone, String status,
			String reason, String retry_after) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("code_register_failed", eventData);
	}

	public void fireCodeRequest(String phone, String method, String length) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("code_request", eventData);
	}

	public void fireCodeRequestFailed(String phone, String method,
			String reason, String value) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("code_request_failed", eventData);
	}

	public void fireCodeRequestFailedTooRecent(String phone, String method,
			String reason, String retry_after) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("code_request_failed_too_recent", eventData);
	}

	public void fireConnect(String phone, String socket) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		fireEvent("connect", eventData);
		
	}

	public void fireConnectError(String phone, String socket) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("connect_error", eventData);
	}

	public void fireCredentialsBad(String phone, String status, String reason) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("credentials_bad", eventData);
	}

	public void fireCredentialsGood(String phone, String login, String pw,
			String type, String expiration, String kind, String price,
			String cost, String currency, String price_expiration) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("credentials_good", eventData);
	}

	public void fireDisconnect(String phone, Socket socket) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("disconnect", eventData);
	}

	public void fireDissectPhone(String phone, String country, String cc,
			String mcc, String lc, String lg) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("dissect_phone", eventData);
	}

	public void fireDissectPhoneFailed(String phone) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("dissect_phone_failed", eventData);
	}

	public void fireGetAudio(String phone, String from, String msgid,
			String type, String time, String name, String size, String url,
			String file, String mimetype, String filehash, String duration,
			String acodec) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_audio", eventData);
	}

	public void fireGetError(String phone, String id, String error) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_error", eventData);
	}

	public void fireGetGroupsInfo(String phone, Map<String,Object> groupList) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_groups_info", eventData);
	}

	public void fireGetGroupsSubject(String phone, String[] reset_from,
			String time, String[] reset_author, String[] reset_author2,
			String name, byte[] bs) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_groups_subject", eventData);
	}

	public void fireGetImage(String phone, String from, String msgid,
			String type, String time, String name, String size, String url,
			String file, String mimetype, String filehash, String width,
			String height, byte[] bs) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_image", eventData);
	}

	public void fireGetLocation(String phone, String from, String msgid,
			String type, String time, String name, String place_name,
			String longitude, String latitude, String url, byte[] bs) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_locations", eventData);
	}

	public void fireGetMessage(String phone, String from, String msgid,
			String type, String time, String name, byte[] bs) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_message", eventData);
	}

	public void fireGetGroupMessage(String phone, String from, String author,
			String msgid, String type, String time, String name, byte[] bs) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_group_message", eventData);
	}

	public void fireGetGroupParticipants(String phone, String groupId,
			Map<String,Object> groupList) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_group_participants", eventData);
	}

	public void fireGetPrivacyBlockedList(String phone, List<ProtocolNode> list) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireGetProfilePicture(String phone, String from, String type,
			byte[] bs) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(FROM, from);
		eventData.put(TYPE, type);
		eventData.put(DATA, bs);
		
		fireEvent(EVENT_GET_PROFILE_PICTURE, eventData);
	}

	public void fireGetRequestLastSeen(String phone, String from, String msgid,
			String sec) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireGetServerProperties(String phone, String version,
			Map<String,String> props) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireGetStatus(String phone, String from, String type,
			String id, String t, byte[] bs) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("get_status", eventData);
	}

	public void fireGetvCard(String phone, String from, String msgid,
			String type, String time, String name, String contact, byte[] bs) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireGetVideo(String phone, String from, String msgid,
			String type, String time, String name, String url, String file,
			String size, String mimetype, String filehash, String duration,
			String vcodec, String acodec, byte[] bs) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireGroupsChatCreate(String phone, String gId) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireGroupsChatEnd(String phone, String gId) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireLogin(String phone) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("login", eventData);
	}

	public void fireLoginFailed(String phone, String tag) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("login_failed", eventData);
	}

	public void fireMediaMessageSent(String phone, String to, String id,
			String filetype, String url, String filename, String filesize,
			byte[] icon) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireMediaUploadFailed(String phone, String id,
			ProtocolNode node, Map<String, Object> messageNode, String reason) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireMessageComposing(String phone, String from, String msgid,
			String type, String time) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(FROM, from);
		eventData.put(MSG_ID, msgid);
		eventData.put(TYPE, type);
		eventData.put(TIME, time);
		
		fireEvent(EVENT_MESSAGE_COMPOSING, eventData);
	}

	public void fireMessagePaused(String phone, String from, String msgid,
			String type, String time) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(FROM, from);
		eventData.put(MSG_ID, msgid);
		eventData.put(TYPE, type);
		eventData.put(TIME, time);
		
		fireEvent(EVENT_MESSAGE_PAUSED, eventData);
	}

	public void fireMessageReceivedClient(String phone, String from,
			String msgid, String type, String time) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(FROM, from);
		eventData.put(MSG_ID, msgid);
		eventData.put(TYPE, type);
		eventData.put(TIME, time);
		
		fireEvent(EVENT_MESSAGE_RECEIVED_CLIENT, eventData);
	}

	public void fireMessageReceivedServer(String phone, String from,
			String msgid, String type, String time) {
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		eventData.put(FROM, from);
		eventData.put(MSG_ID, msgid);
		eventData.put(TYPE, type);
		eventData.put(TIME, time);
		
		fireEvent(EVENT_MESSAGE_RECEIVED_SERVER, eventData);
	}

	public void firePing(String phone, String msgid) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("ping", eventData);
	}

	public void fireProfilePictureChanged(String phone, String from, String id,
			String t) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireProfilePictureDeleted(String phone, String from, String id,
			String t) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireSendMessage(String phone, String targets, String id,
			ProtocolNode node) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("send_message", eventData);
	}

	public void fireSendMessageReceived(String phone, String from, String type) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent("send_message_received", eventData);
	}

	public void fireSendPresence(String phone, String type, String name) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireSendStatusUpdate(String phone, String msg) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireUploadFile(String phone, String name, String url) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireUploadFileFailed(String phone, String name) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(PHONE_NUMBER, phone);
		
		fireEvent(EVENT_UNKNOWN, eventData);
	}

	public void fireGetSyncResult(String result) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(RESULT, result);
		fireEvent(EVENT_UNKNOWN, eventData);
		
	}

	public void fireGetReceipt(String from, String id, String offline,
			String retry) {
		// TODO Auto-generated method stub
		Map<String,Object> eventData = new HashMap<String,Object>();
		eventData.put(FROM, from);
		fireEvent(EVENT_UNKNOWN, eventData);
		
	}
}

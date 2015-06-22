package nl.giovanniterlingen.whatsapp;

import nl.giovanniterlingen.whatsapp.events.Event;
import nl.giovanniterlingen.whatsapp.events.EventType;
import nl.giovanniterlingen.whatsapp.message.*;
import nl.giovanniterlingen.whatsapp.tools.BinHex;
import nl.giovanniterlingen.whatsapp.tools.CharsetUtils;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * Java adaptation of PHP WhatsAPI by venomous0x
 * {@link https://github.com/venomous0x/WhatsAPI}
 *
 * @author Kim Lindberg (kim@sumppen.net)
 */
public class WhatsApi {

    private static final String RELEASE_TOKEN_CONST = "PdA2DJyKoUrwLw1Bg6EIhzh502dF9noR9uFCllGk";
    private static final String RELEASE_TIME = "1419900749520";
    private final int PORT = 443;                                      // The port of the WhatsApp server.
    private final int TIMEOUT_SEC = 2;                                  // The timeout for the connection with the WhatsApp servers.
    private final String WHATSAPP_CHECK_HOST = "v.whatsapp.net/v2/exist";  // The check credentials host.
    public static final String WHATSAPP_GROUP_SERVER = "g.us";                   // The Group server hostname
    private final String WHATSAPP_HOST = "c.whatsapp.net";                 // The hostname of the WhatsApp server.
    private final String WHATSAPP_REGISTER_HOST = "v.whatsapp.net/v2/register"; // The register code host.
    private final String WHATSAPP_REQUEST_HOST = "v.whatsapp.net/v2/code";      // The request code host.
    public static final String WHATSAPP_SERVER = "s.whatsapp.net";               // The hostname used to login/send messages.
    private final String WHATSAPP_DEVICE = "S40";                      // The device name.
    private final String WHATSAPP_VER = "2.12.81";                // The WhatsApp version.
    private final String WHATSAPP_USER_AGENT = "WhatsApp/2.12.81 S40Version/14.26 Device/Nokia302";// User agent used in request/registration code.
    private final String WHATSAPP_VER_CHECKER = "https://coderus.openrepos.net/whitesoft/whatsapp_version"; // Check WhatsApp version

    private String identity;
    private final String name;
    private final String phoneNumber;
    private LoginStatus loginStatus;
    private Socket socket;
    private String password;

    private BinTreeNodeWriter writer;
    private byte[] challengeData;
    private BinTreeNodeReader reader;
    private KeyStream inputKey;
    private KeyStream outputKey;
    private List<String> serverReceivedId = new LinkedList<String>();
    private List<ProtocolNode> messageQueue = new LinkedList<ProtocolNode>();
    private String lastId;
    private List<ProtocolNode> outQueue = new LinkedList<ProtocolNode>();
    private EventManager eventManager = new LoggingEventManager();
    private int messageCounter = 0;
    private final List<Country> countries;
    private Map<String, Map<String, Object>> mediaQueue = new HashMap<String, Map<String, Object>>();
    private MediaInfo mediaFile;
    private JSONObject mediaInfo;
    private MessageProcessor processor = null;
    private MessagePoller poller;
    private String lastSendMsgId;
    private Proxy proxy;
    protected Context context;

    public WhatsApi(String username, String identity, String nickname) throws NoSuchAlgorithmException, WhatsAppException, IOException {
        writer = new BinTreeNodeWriter();
        reader = new BinTreeNodeReader();
        this.name = nickname;
        this.phoneNumber = username;
        try {
            if (!checkIdentity(identity)) {
                this.identity = buildIdentity(identity);
            } else {
                this.identity = identity;
            }
        } catch (UnsupportedEncodingException e) {
            throw new WhatsAppException(e);
        }
        this.loginStatus = LoginStatus.DISCONNECTED_STATUS;
        countries = readCountries();
    }

    /**
     * Add message to the outgoing queue.
     */
    public void addMsgOutQueue(ProtocolNode node) {
        outQueue.add(node);
    }

    /**
     * Register account on WhatsApp using the provided code.
     *
     * @return object
     * An object with server response.
     * - status: Account status.
     * - login: Phone number with country code.
     * - pw: Account password.
     * - type: Type of account.
     * - expiration: Expiration date in UNIX TimeStamp.
     * - kind: Kind of account.
     * - price: Formatted price of account.
     * - cost: Decimal amount of account.
     * - currency: Currency price of account.
     * - price_expiration: Price expiration in UNIX TimeStamp.
     * @throws WhatsAppException
     * @throws JSONException
     * @throws Exception
     */
    public JSONObject codeRegister(String code) throws WhatsAppException, JSONException {
        Map<String, String> phone;
        if ((phone = dissectPhone()) == null) {
            throw new WhatsAppException("The prived phone number is not valid.");
        }
        String countryCode = null;
        String langCode = null;
        if (countryCode == null) {
            if (phone.get("ISO3166") != null) {
                countryCode = phone.get("ISO3166");
            } else {
                countryCode = "US";
            }
        }
        if (langCode == null) {
            if (phone.get("ISO639") != null) {
                langCode = phone.get("ISO639");
            } else {
                langCode = "en";
            }
        }

        // Build the url.
        String host = "https://" + WHATSAPP_REGISTER_HOST;
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("cc", phone.get("cc"));
        query.put("in", phone.get("phone"));
        query.put("lg", langCode);
        query.put("lc", countryCode);
        query.put("id", (identity == null ? "" : identity));
        query.put("code", code);
        //		query.put("c", "cookie");

        JSONObject response = getResponse(host, query);
        Log.d("DEBUG", response.toString(1));
        if (!response.getString("status").equals("ok")) {
            eventManager.fireCodeRegisterFailed(phoneNumber, response.getString("status"), response.getString("reason"), "");//response.getString("retry_after"));
            throw new WhatsAppException("An error occurred registering the registration code from WhatsApp.");
        } else {
            eventManager.fireCodeRegister(phoneNumber, response.getString("login"), response.getString("pw"), response.getString("type"), response.getString("expiration"),
                    response.getString("kind"), response.getString("price"), response.getString("cost"), response.getString("currency"), response.getString("price_expiration"));
        }

        return response;
    }

    /**
     * Request a registration code from WhatsApp.
     *
     * @return {@link JSONObject}
     * An object with server response.
     * - status: Status of the request (sent/fail).
     * - length: Registration code lenght.
     * - method: Used method.
     * - reason: Reason of the status (e.g. too_recent/missing_param/bad_param).
     * - param: The missing_param/bad_param.
     * - retry_after: Waiting time before requesting a new code.
     * @throws JSONException
     * @throws WhatsAppException
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    public JSONObject codeRequest(String method, String countryCode, String langCode) throws WhatsAppException, JSONException, UnsupportedEncodingException {
        if (method == null) {
            method = "sms";
        }
        Map<String, String> phone;
        if ((phone = dissectPhone()) == null) {
            throw new WhatsAppException("The prived phone number is not valid.");
        }

        if (countryCode == null) {
            if (phone.get("ISO3166") != null) {
                countryCode = phone.get("ISO3166");
            } else {
                countryCode = "US";
            }
        }
        if (langCode == null) {
            if (phone.get("ISO639") != null) {
                langCode = phone.get("ISO639");
            } else {
                langCode = "en";
            }
        }

        String token;
        try {
            token = generateRequestToken(phone.get("country"), phone.get("phone"));
        } catch (NoSuchAlgorithmException e) {
            throw new WhatsAppException(e);
        } catch (IOException e) {
            throw new WhatsAppException(e);
        }
        // Build the url.
        String host = "https://" + WHATSAPP_REQUEST_HOST;
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("cc", phone.get("cc"));
        query.put("in", phone.get("phone"));
        //		query.put("to",phoneNumber);
        query.put("lg", langCode);
        query.put("lc", countryCode);
        query.put("method", method);
        //		query.put("mcc",phone.get("mcc"));
        //		query.put("mnc","001");
        query.put("sim_mcc", phone.get("mcc"));
        query.put("sim_mnc", "000");
        query.put("token", URLEncoder.encode(token, "iso-8859-1"));
        query.put("id", (identity == null ? "" : identity));

        JSONObject response = getResponse(host, query);
        Log.d("DEBUG", response.toString(1));
        if (!response.getString("status").equals("ok")) {
            if (response.getString("status").equals("sent")) {
                eventManager.fireCodeRequest(phoneNumber, method, response.getString("length"));
            } else {
                if (!response.isNull("reason") && response.getString("reason").equals("too_recent")) {
                    String retry_after = (response.has("retry_after") ? response.getString("retry_after") : null);
                    eventManager.fireCodeRequestFailedTooRecent(phoneNumber, method, response.getString("reason"), retry_after);
                    throw new WhatsAppException("Code already sent. Retry after " + retry_after + " seconds");
                } else {
                    eventManager.fireCodeRequestFailed(phoneNumber, method, response.getString("reason"), (response.has("param") ? response.getString("param") : null));
                    throw new WhatsAppException("There was a problem trying to request the code. Status=" + response.getString("status"));
                }
            }
        } else {
            eventManager.fireCodeRegister(phoneNumber, response.getString("login"), response.getString("pw"), response.getString("type"), response.getString("expiration"),
                    response.getString("kind"), response.getString("price"), response.getString("cost"), response.getString("currency"), response.getString("price_expiration"));
        }
        return response;
    }

    protected String generateRequestToken(String country, String phone) throws IOException, NoSuchAlgorithmException {
        return WhatsMediaUploader.md5(RELEASE_TOKEN_CONST + RELEASE_TIME + phone);
    }

    private byte[] hash(String algo, byte[] dataBytes) throws NoSuchAlgorithmException {
        MessageDigest md;

        md = MessageDigest.getInstance(algo);

        md.update(dataBytes, 0, dataBytes.length);
        byte[] mdbytes = md.digest();
        return mdbytes;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public Proxy getProxy() {
        return proxy;
    }

    /**
     * Connect (create a socket) to the WhatsApp network.
     */
    public boolean connect() throws UnknownHostException, IOException {
        if (proxy == null) {
            socket = new Socket(WHATSAPP_HOST, PORT);
        } else {
            socket = new Socket(proxy);
            SocketAddress socketAddress = new InetSocketAddress(WHATSAPP_HOST, PORT);
            socket.connect(socketAddress);
        }
        if (socket.isConnected()) {
            socket.setSoTimeout(TIMEOUT_SEC * 1000);
            return true;
        } else {
            Log.w("WARNING", "Failed to connect to WhatsApp server");
            return false;
        }
    }

    /**
     * Disconnect from the WhatsApp network.
     */
    public void disconnect() {
        if (poller != null) {
            poller.setRunning(false);
        }
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("ERROR", "Exception while disconnecting", e);
            }
        }
        eventManager.fireDisconnect(
                phoneNumber,
                socket
        );
    }

    /**
     * Drain the message queue for application processing.
     *
     * @return List<ProtocolNode>
     * Return the message queue list.
     */
    public List<ProtocolNode> getMessages() {
        List<ProtocolNode> ret = messageQueue;
        messageQueue = new LinkedList<ProtocolNode>();

        return ret;
    }

    /**
     * Log into the Whatsapp server.
     * <p>
     * ###Warning### using this method will generate a new password
     * from the WhatsApp servers each time.
     * <p>
     * If you know your password and wish to use it without generating
     * a new password - use the loginWithPassword() method instead.
     *
     * @throws WhatsAppException
     */
    public void login(boolean profileSubscribe) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");

    }

    /**
     * Login to the Whatsapp server with your password
     * <p>
     * If you already know your password you can log into the Whatsapp server
     * using this method.
     */
    public void loginWithPassword(String password) throws WhatsAppException {
        this.password = password;
        login();
    }

    private void login() throws WhatsAppException {
        try {
            doLogin();
            if (loginStatus != LoginStatus.CONNECTED_STATUS) {
                throw new WhatsAppException("Failed to log in");
            }
        } catch (Exception e) {
            throw new WhatsAppException(e);
        }
    }

    public boolean isConnected() {
        return (loginStatus == LoginStatus.CONNECTED_STATUS) && socket != null && socket.isConnected();
    }

    public void reconnect() throws WhatsAppException {
        if (password == null) {
            throw new WhatsAppException("No password exists");
        }
        login();
    }

    /**
     * Send the active status. User will show up as "Online" (as long as socket is connected).
     *
     * @throws WhatsAppException
     */
    public void sendActiveStatus() throws WhatsAppException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("type", "active");
        ProtocolNode messageNode = new ProtocolNode("presence", map, null, null);
        sendNode(messageNode);
    }

    public void sendBroadcastAudio(List<String> targets, String path) throws WhatsAppException {
        sendBroadcastAudio(targets, path, false);
    }

    /**
     * Send a Broadcast Message with audio.
     * <p>
     * The recipient MUST have your number (synced) and in their contact list
     * otherwise the message will not deliver to that person.
     * <p>
     * Approx 20 (unverified) is the maximum number of targets
     *
     * @throws WhatsAppException
     */
    public void sendBroadcastAudio(List<String> targets, String path, boolean storeURLmedia) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    public void sendBroadcastImage(List<String> targets, String path) throws WhatsAppException {
        sendBroadcastImage(targets, path, false);
    }

    /**
     * Send a Broadcast Message with an image.
     * <p>
     * The recipient MUST have your number (synced) and in their contact list
     * otherwise the message will not deliver to that person.
     * <p>
     * Approx 20 (unverified) is the maximum number of targets
     *
     * @throws WhatsAppException
     */
    public void sendBroadcastImage(List<String> targets, String path, boolean storeURLmedia) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Send a Broadcast Message with location data.
     * <p>
     * The recipient MUST have your number (synced) and in their contact list
     * otherwise the message will not deliver to that person.
     * <p>
     * If no name is supplied , receiver will see large sized google map
     * thumbnail of entered Lat/Long but NO name/url for location.
     * <p>
     * With name supplied, a combined map thumbnail/name box is displayed
     * <p>
     * Approx 20 (unverified) is the maximum number of targets
     *
     * @throws WhatsAppException
     */

    public void sendBroadcastLocation(List<String> targets, float lng, float lat, String name, String url) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Send a Broadcast Message
     * <p>
     * The recipient MUST have your number (synced) and in their contact list
     * otherwise the message will not deliver to that person.
     * <p>
     * Approx 20 (unverified) is the maximum number of targets
     *
     * @throws WhatsAppException
     */
    public void sendBroadcastMessage(List<String> targets, String message) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    public void sendBroadcastVideo(List<String> targets, String path) throws WhatsAppException {
        sendBroadcastVideo(targets, path, false);
    }

    /**
     * Send a Broadcast Message with a video.
     * <p>
     * The recipient MUST have your number (synced) and in their contact list
     * otherwise the message will not deliver to that person.
     * <p>
     * Approx 20 (unverified) is the maximum number of targets
     *
     * @throws WhatsAppException
     */
    public void sendBroadcastVideo(List<String> targets, String path, boolean storeURLmedia) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    public void sendClientConfig() throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    public void sendGetClientConfig() throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Send a request to return a list of groups user is currently participating
     * in.
     * <p>
     * To capture this list you will need to bind the "onGetGroups" event.
     *
     * @throws WhatsAppException
     */
    public void getGroups() throws WhatsAppException {
        String type = "participating";
        String msgID = createMsgId("getgroups");
        ProtocolNode child = new ProtocolNode(type, null, null, null);
        Map<String, String> attr = new HashMap<String, String>();
        attr.put("id", msgID);
        attr.put("type", "get");
        attr.put("xmlns", "w:g2");
        attr.put("to", WHATSAPP_GROUP_SERVER);

        List<ProtocolNode> children = new LinkedList<ProtocolNode>();
        children.add(child);
        ProtocolNode node = new ProtocolNode("iq", attr, children, null);

        sendNode(node);
        try {
            waitForServer(msgID);
        } catch (Exception e) {
            throw new WhatsAppException("Error getting groups", e);
        }
    }

    /**
     * Send a request to get information about a specific group
     *
     * @throws WhatsAppException
     */
    public void sendGetGroupsInfo(String gjid) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Send a request to return a list of groups user has started
     * in.
     * <p>
     * To capture this list you will need to bind the "onGetGroups" event.
     *
     * @throws WhatsAppException
     */
    public void sendGetGroupsOwning() throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Send a request to get a list of people you have currently blocked
     *
     * @throws WhatsAppException
     */
    public void sendGetPrivacyBlockedList() throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    public void sendGetProfilePicture(String number) throws WhatsAppException {
        sendGetProfilePicture(number, false);
    }

    /**
     * Get profile picture of specified user
     *
     * @throws WhatsAppException
     */
    public void sendGetProfilePicture(String number, boolean large) throws WhatsAppException {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("type", large ? "image" : "preview");
        ProtocolNode picture = new ProtocolNode("picture", map, null, null);

        map = new LinkedHashMap<String, String>();
        map.put("id", createMsgId("getpicture"));
        map.put("type", "get");
        map.put("xmlns", "w:profile:picture");
        map.put("to", getJID(number));

        List<ProtocolNode> lista = new LinkedList<ProtocolNode>();
        lista.add(picture);

        ProtocolNode node = new ProtocolNode("iq", map, lista, null);
        try {
            sendNode(node);
            waitForServer(map.get("id"));
        } catch (Exception e) {
            throw new WhatsAppException("Failed to get profile picture", e);
        }
    }

    /**
     * Request to retrieve the last online time of specific user.
     */
    public void sendGetRequestLastSeen(String to) {
        //TODO implement this
    }

    /**
     * Send a request to get the current server properties
     *
     * @throws WhatsAppException
     */
    public void sendGetServerProperties() throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Create a group chat.
     *
     * @return String
     * The group ID.
     * @throws WhatsAppException
     */
    public String sendGroupsChatCreate(String subject, List<String> participants) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * End or delete a group chat
     *
     * @throws WhatsAppException
     */
    public void sendGroupsChatEnd(String gjid) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Leave a group chat
     *
     * @throws WhatsAppException
     */
    public void sendGroupsLeave(List<String> gjids) throws WhatsAppException {
        String msgId = createMsgId("leavegroups");

        List<ProtocolNode> nodes = new LinkedList<ProtocolNode>();
        for (String gjid : gjids) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("id", getJID(gjid));
            ProtocolNode node = new ProtocolNode("group",
                    params, null, null);
            nodes.add(node);
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "delete");
        ProtocolNode leave = new ProtocolNode("leave",
                params, nodes, null);

        params = new HashMap<String, String>();
        params.put("id", msgId);
        params.put("to", WHATSAPP_GROUP_SERVER);
        params.put("type", "set");
        params.put("xmlns", "w:g2");
        List<ProtocolNode> list = new LinkedList<ProtocolNode>();
        list.add(leave);
        ProtocolNode node = new ProtocolNode("iq",
                params, list, null);

        sendNode(node);
        try {
            waitForServer(msgId);
        } catch (Exception e) {
            throw new WhatsAppException("Failure while waiting for response", e);
        }
    }

    /**
     * Add participant(s) to a group.
     *
     * @throws WhatsAppException
     */
    public void sendGroupsParticipantsAdd(String groupId, List<String> participants) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Remove participant(s) from a group.
     *
     * @throws WhatsAppException
     */
    public void sendGroupsParticipantsRemove(String groupId, List<String> participants) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Send audio to the user/group.     *
     *
     * @return JSONObject json object with media information, or null if sending failed
     * @throws WhatsAppException
     */
    public JSONObject sendMessageAudio(String to, File filepath) throws WhatsAppException {
        return sendMessageAudio(to, filepath, false);
    }

    /**
     * Send audio to the user/group.     *
     *
     * @return JSONObject json object with media information, or null if sending failed
     * @throws WhatsAppException
     */
    public JSONObject sendMessageAudio(String to, File file, boolean storeURLmedia) throws WhatsAppException {
        String[] allowedExtensions = {"3gp", "caf", "wav", "mp3", "mp4", "wma", "ogg", "aif", "aac", "m4a"};
        int size = 10 * 1024 * 1024; // Easy way to set maximum file size for this media type.
        try {
            // This list should be done better or at least cached!
            List<String> list = new ArrayList<String>();
            for (String ext : allowedExtensions) {
                list.add(ext);
            }
            MediaInfo info = new MediaInfo();
            info.setMediaFile(file);
            return sendCheckAndSendMedia(info, size, to, "audio", list, null);
        } catch (Exception e) {
            Log.w("WARNING", "Exception sending audio", e);
            throw new WhatsAppException(e);
        }
    }

    /**
     * Checks that the media file to send is of allowable filetype and within size limits.
     *
     * @return JSONObject json with media details, or null if failed
     * @throws IOException
     * @throws InvalidTokenException
     * @throws InvalidMessageException
     * @throws IncompleteMessageException
     * @throws WhatsAppException
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     * @throws DecodeException
     * @throws InvalidKeyException
     */
    private JSONObject sendCheckAndSendMedia(MediaInfo info, int maxSize, String to,
                                             String type, List<String> allowedExtensions, String caption) throws WhatsAppException, IncompleteMessageException, InvalidMessageException, InvalidTokenException, IOException, JSONException, NoSuchAlgorithmException, InvalidKeyException, DecodeException {
        File file = info.getMediaFile();
        if (file.length() <= maxSize && file.isFile() && file.length() > 0) {
            String fileName = file.getName();
            int lastIndexOf = fileName.lastIndexOf('.') + 1;
            String extension = fileName.substring(lastIndexOf);
            if (allowedExtensions.contains(extension)) {
                mediaInfo = null;
                String b64hash = base64_encode(hash_file("sha256", file, true));
                //request upload
                sendRequestFileUpload(b64hash, type, info, to, caption);

                if (mediaInfo == null) {
                    lastSendMsgId = null;
                }

                return mediaInfo;
            } else {
                //Not allowed file type.
                return new JSONObject("{\"error\":\"Invalid media type " + extension + "\"}");
            }
        } else {
            //Didn't get media file details.
            return null;
        }
    }

    private void sendRequestFileUpload(String b64hash, String type, MediaInfo file,
                                       String to, String caption) throws WhatsAppException, IncompleteMessageException, InvalidMessageException, InvalidTokenException, IOException, JSONException, NoSuchAlgorithmException, InvalidKeyException, DecodeException {
        mediaFile = file;
        Map<String, String> hash = new HashMap<String, String>();
        hash.put("hash", b64hash);
        hash.put("type", type);
        hash.put("size", Long.toString(file.getMediaFile().length()));
        ProtocolNode mediaNode = new ProtocolNode("media", hash, null, null);
        hash = new HashMap<String, String>();
        String id = createMsgId("upload");
        hash.put("id", id);
        hash.put("to", WHATSAPP_SERVER);
        hash.put("type", "set");
        hash.put("xmlns", "w:m");
        ArrayList<ProtocolNode> list = new ArrayList<ProtocolNode>();
        list.add(mediaNode);
        ProtocolNode node = new ProtocolNode("iq", hash, list, null);

		/*
         * TODO support for multiple recipients
		 *  if (!is_array($to)) {
		 *    $to = $this->getJID($to);
		 *	}
		 *
		 */
        String messageId = createMsgId("message");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("messageNode", node);
        map.put("file", file);
        map.put("to", to);
        map.put("message_id", messageId);
        map.put("caption", caption);
        mediaQueue.put(id, map);
        sendNode(node);
        waitForServer(id);
    }

    private String base64_encode(byte[] data) {
        byte[] enc = Base64.encode(data, 0); /** I could be wrong here */
        return new String(enc);
    }

    private byte[] hash_file(String string, File file, boolean b) throws NoSuchAlgorithmException, IOException {
        MessageDigest md;

        md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);

        try {
            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            ;
            byte[] mdbytes = md.digest();
            return mdbytes;
        } finally {
            fis.close();
        }
    }

    /**
     * Send the composing message status. When typing a message.
     *
     * @throws WhatsAppException
     */
    public void sendMessageComposing(String to) throws WhatsAppException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("to", getJID(to));
        ArrayList<ProtocolNode> list = new ArrayList<ProtocolNode>();
        ProtocolNode status = new ProtocolNode("composing", null, null, null);
        list.add(status);
        ProtocolNode messageNode = new ProtocolNode("chatstate", map, list, null);
        sendNode(messageNode);
    }


    /**
     * Send an image file to group/user
     *
     * @return JSONObject
     * @throws WhatsAppException
     */
    public JSONObject sendMessageImage(String to, File image, File preview) throws WhatsAppException {
        return sendMessageImage(to, image, preview, "");
    }

    /**
     * Send an image file to group/user
     *
     * @return JSONObject
     * @throws WhatsAppException
     */
    public JSONObject sendMessageImage(String to, File image, File preview, String caption) throws WhatsAppException {

        String[] allowedExtensions = {"jpg", "jpeg", "gif", "png"};
        int size = 5 * 1024 * 1024; // Easy way to set maximum file size for this media type.
        try {
            // This list should be done better or at least cached!
            List<String> list = new ArrayList<String>();
            for (String ext : allowedExtensions) {
                list.add(ext);
            }
            MediaInfo info = new MediaInfo();
            info.setMediaFile(image);
            info.setPreviewFile(preview);
            info.setCaption(caption);
            return sendCheckAndSendMedia(info, size, to, "image", list, caption);
        } catch (Exception e) {
            Log.w("WARNING", "Exception sending audio", e);
            throw new WhatsAppException(e);
        }
    }

    /**
     * Send a location to the user/group.
     * <p>
     * If no name is supplied , receiver will see large sized google map
     * thumbnail of entered Lat/Long but NO name/url for location.
     * <p>
     * With name supplied, a combined map thumbnail/name box is displayed
     *
     * @throws WhatsAppException
     */
    public void sendMessageLocation(List<String> to, float lng, float lat, String name, String url) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Send the 'paused composing message' status.
     *
     * @throws WhatsAppException
     */
    public void sendMessagePaused(String to) throws WhatsAppException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("to", getJID(to));
        ArrayList<ProtocolNode> list = new ArrayList<ProtocolNode>();
        ProtocolNode status = new ProtocolNode("paused", null, null, null);
        list.add(status);
        ProtocolNode messageNode = new ProtocolNode("chatstate", map, list, null);
        sendNode(messageNode);
    }

    /**
     * Send a video to the user/group.
     *
     * @return boolean
     * @throws WhatsAppException
     */
    public JSONObject sendMessageVideo(String to, File media, File preview, String caption) throws WhatsAppException {
        String[] allowedExtensions = {"3gp", "mp4", "mov", "avi"};
        int size = 20 * 1024 * 1024; // Easy way to set maximum file size for this media type.
        try {
            // This list should be done better or at least cached!
            List<String> list = new ArrayList<String>();
            for (String ext : allowedExtensions) {
                list.add(ext);
            }
            MediaInfo info = new MediaInfo();
            info.setMediaFile(media);
            info.setPreviewFile(preview);
            info.setCaption(caption);
            return sendCheckAndSendMedia(info, size, to, "video", list, caption);
        } catch (Exception e) {
            Log.w("WARNING", "Exception sending video", e);
            throw new WhatsAppException(e);
        }
    }

    /**
     * Send the offline status. User will show up as "Offline".
     *
     * @throws WhatsAppException
     */
    public void sendOfflineStatus() throws WhatsAppException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("type", "unavailable");
        ProtocolNode messageNode = new ProtocolNode("presence", map, null, null);
        sendNode(messageNode);
    }

    /**
     * Send available presence status.
     */
    public void sendPresence() throws IOException, WhatsAppException {
        sendPresence("available");
    }

    public void sendPing() throws WhatsAppException {
        Log.d("DEBUG", "Sending ping");

        String msgId = createMsgId("ping");
        ProtocolNode pingNode = new ProtocolNode("ping", null, null, null);

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", msgId);
        params.put("xmlns", "w:p");
        params.put("type", "get");
        params.put("to", WHATSAPP_SERVER);

        ProtocolNode node = new ProtocolNode("iq", params, Arrays.asList(pingNode), null);
        sendNode(node);

    }

    public void sendMessageRead(String to, String id) throws WhatsAppException {

        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "read");
        params.put("to", getJID(to));
        params.put("id", id);

        ProtocolNode messageNode = new ProtocolNode("receipt", params, null, null);

        this.sendNode(messageNode);
    }


    /**
     * Send presence subscription, automatically receive presence updates as long as the socket is open.
     *
     * @throws WhatsAppException
     */
    public void sendPresenceSubscription(String to) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Set the picture for the group
     *
     * @throws WhatsAppException
     */
    public void sendSetGroupPicture(String gjid, String path) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Set the list of numbers you wish to block receiving from.
     *
     * @throws WhatsAppException
     */
    public void sendSetPrivacyBlockedList(List<String> blockedJids) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Set your profile picture. Thumbnail should be 96px size version of image
     *
     * @throws WhatsAppException
     */
    public void sendSetProfilePicture(File image, File thumbnail) throws WhatsAppException {
        sendSetPicture(phoneNumber, image, thumbnail);
    }

    public String sendSync(List<String> numbers, List<String> deletedNumbers, SyncType syncType, int index, boolean last) throws WhatsAppException {
        List<ProtocolNode> users = new LinkedList<ProtocolNode>();

        for (String number : numbers) {
            // number must start with '+' if international contact
            if (number.length() > 1) {
                if (!number.startsWith("+")) {
                    number = "+" + number;
                }
                ProtocolNode user = new ProtocolNode("user", null, null, number.getBytes());
                users.add(user);
            }
        }

        if (deletedNumbers != null && deletedNumbers.size() > 0) {
            for (String number : deletedNumbers) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("jid", getJID(number));
                map.put("type", "delete");
                ProtocolNode user = new ProtocolNode("user", map, null, null);
                users.add(user);
            }
        }

        String mode = null;
        String context = null;
        switch (syncType) {
            case FULL_REGISTRATION:
                mode = "full";
                context = "registration";
                break;
            case FULL_INTERACTIVE:
                mode = "full";
                context = "interactive";
                break;
            case FULL_BACKGROUND:
                mode = "full";
                context = "background";
                break;
            case DELTA_INTERACTIVE:
                mode = "delta";
                context = "interactive";
                break;
            case DELTA_BACKGROUND:
                mode = "delta";
                context = "background";
                break;
            case QUERY_INTERACTIVE:
                mode = "query";
                context = "interactive";
                break;
            case CHUNKED_REGISTRATION:
                mode = "chunked";
                context = "registration";
                break;
            case CHUNKED_INTERACTIVE:
                mode = "chunked";
                context = "interactive";
                break;
            case CHUNKED_BACKGROUND:
                mode = "chunked";
                context = "background";
                break;
            default:
                mode = "delta";
                context = "background";
        }

        String id = createMsgId("sendsync_");
        Date now = new Date();
        long longSid = ((now.getTime() + 11644477200L) * 10000);
        String sid = Long.toString(longSid);

        Map<String, String> syncMap = new HashMap<String, String>();
        syncMap.put("mode", mode);
        syncMap.put("context", context);
        syncMap.put("sid", sid);
        syncMap.put("index", "" + index);
        syncMap.put("last", (last ? "true" : "false"));

        ProtocolNode syncNode = new ProtocolNode("sync",
                syncMap, users, null);

        Map<String, String> nodeMap = new HashMap<String, String>();
        nodeMap.put("id", id);
        nodeMap.put("xmlns", "urn:xmpp:whatsapp:sync");
        nodeMap.put("type", "get");

        List<ProtocolNode> nodeList = new LinkedList<ProtocolNode>();
        nodeList.add(syncNode);

        ProtocolNode node = new ProtocolNode("iq",
                nodeMap, nodeList, null);

        sendNode(node);
        try {
            waitForServer(id);
        } catch (Exception e) {
            Log.e("ERROR", "Failed to wait for server: " + e.getMessage());
            throw new WhatsAppException("Failed while waiting for server", e);
        }

        return id;
    }


    /**
     * Set your profile picture
     *
     * @throws WhatsAppException
     */
    private void sendSetPicture(String jid, File image, File thumbnail) throws WhatsAppException {
        preprocessProfilePicture(image);

        if (image.exists() && image.canRead() && image.isFile()) {
            byte[] data;
            try {
                data = readFile(image);
            } catch (IOException e) {
                throw new WhatsAppException("Failed to read image file", e);
            }
            if (data != null && data.length > 0) {
                //this is where the fun starts
                ProtocolNode picture = new ProtocolNode("picture", null, null, data);

                byte[] icon;
                if (thumbnail != null && thumbnail.isFile() && thumbnail.canRead()) {
                    try {
                        icon = readFile(thumbnail);
                    } catch (IOException e1) {
                        throw new WhatsAppException("Failed to read thumbnail image", e1);
                    }
                } else {
                    icon = createIconGD(image, 96, true);
                }
                HashMap<String, String> typeMap = new HashMap<String, String>();
                typeMap.put("type", "preview");
                ProtocolNode thumb = new ProtocolNode("picture", typeMap, null, icon);

                HashMap<String, String> hash = new HashMap<String, String>();
                String nodeID = createMsgId("setphoto");
                hash.put("id", nodeID);
                hash.put("to", getJID(jid));
                hash.put("type", "set");
                hash.put("xmlns", "w:profile:picture");
                List<ProtocolNode> arr = new LinkedList<ProtocolNode>();
                arr.add(picture);
                arr.add(thumb);
                ProtocolNode node = new ProtocolNode("iq", hash, arr, null);

                sendNode(node);
                try {
                    waitForServer(nodeID);
                } catch (Exception e) {
                    throw new WhatsAppException("Waiting for reply failed", e);
                }
            }
        }
    }

    private byte[] createIconGD(File filepath, int i, boolean b) throws WhatsAppException {
        throw new WhatsAppException("Automatic creation of thumbnail not yet implemented");
        //		list($width, $height) = getimagesize($file);
        //		if ($width > $height) {
        //			//landscape
        //			$nheight = ($height / $width) * $size;
        //			$nwidth = $size;
        //		} else {
        //			$nwidth = ($width / $height) * $size;
        //			$nheight = $size;
        //		}
        //		$image_p = imagecreatetruecolor($nwidth, $nheight);
        //		$image = imagecreatefromjpeg($file);
        //		imagecopyresampled($image_p, $image, 0, 0, 0, 0, $nwidth, $nheight, $width, $height);
        //		ob_start();
        //		imagejpeg($image_p);
        //		$i = ob_get_contents();
        //		ob_end_clean();
        //		if ($raw) {
        //			return $i;
        //		} else {
        //			return base64_encode($i);
        //		}
    }

    private byte[] readFile(File filepath) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //write file data
        FileInputStream fileInputStream = new FileInputStream(filepath);

        // Copy the contents of the file to the output stream
        byte[] buffer = new byte[1024];
        int count = 0;

        while ((count = fileInputStream.read(buffer)) >= 0) {
            out.write(buffer, 0, count);
        }
        fileInputStream.close();
        return out.toByteArray();
    }

    private void preprocessProfilePicture(File filepath) {
        // TODO Auto-generated method stub

    }

    /**
     * Set the recovery token for your account to allow you to
     * retrieve your password at a later stage.
     *
     * @throws WhatsAppException
     */
    public void sendSetRecoveryToken(String token) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Update the user status.
     *
     * @throws WhatsAppException
     */
    public void sendStatusUpdate(String txt) throws WhatsAppException {

        ProtocolNode child = new ProtocolNode("status", null, null, CharsetUtils.toBytes(txt));
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("to", "s.whatsapp.net");
        map.put("type", "set");
        map.put("id", createMsgId("sendstatus"));
        map.put("xmlns", "status");
        ArrayList<ProtocolNode> nodes = new ArrayList<ProtocolNode>();
        nodes.add(child);
        ProtocolNode node = new ProtocolNode("iq", map, nodes, null);
        try {
            sendNode(node);
            eventManager.fireSendStatusUpdate(phoneNumber, txt);
        } catch (Exception e) {
            throw new WhatsAppException("Failed to update status");
        }
    }

    /**
     * Send a vCard to the user/group.
     *
     * @throws WhatsAppException
     */
    public void sendVcard(String to, String name, Object vCard) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Sets the bind of the new message.
     *
     * @throws WhatsAppException
     */
    public void setNewMessageBind(MessageProcessor processor) throws WhatsAppException {
        this.processor = processor;
    }

    /**
     * Upload file to WhatsApp servers.
     *
     * @return String
     * Return the remote url or null on failure.
     * @throws WhatsAppException
     */
    public String uploadFile(String file) throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Wait for message delivery notification.
     *
     * @throws WhatsAppException
     */
    public void waitForMessageReceipt() throws WhatsAppException {
        //TODO implement this
        throw new WhatsAppException("Not yet implemented");
    }

    /**
     * Check if account credentials are valid.
     * <p>
     * WARNING: WhatsApp now changes your password everytime you use this.
     * Make sure you update your config file if the output informs about
     * a password change.
     *
     * @return object
     * An object with server response.
     * - status: Account status.
     * - login: Phone number with country code.
     * - pw: Account password.
     * - type: Type of account.
     * - expiration: Expiration date in UNIX TimeStamp.
     * - kind: Kind of account.
     * - price: Formatted price of account.
     * - cost: Decimal amount of account.
     * - currency: Currency price of account.
     * - price_expiration: Price expiration in UNIX TimeStamp.
     * @throws JSONException
     * @throws WhatsAppException
     * @throws Exception
     */
    public boolean checkCredentials(String number) throws JSONException, WhatsAppException {
        Map<String, String> phone;
        if ((phone = dissectPhone()) == null) {
            throw new WhatsAppException("The prived phone number is not valid.");
        }

        // Build the url.
        String host = "https://" + WHATSAPP_CHECK_HOST;
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("cc", phone.get("cc"));
        query.put("in", phone.get("phone"));
        query.put("id", identity);
        query.put("c", "cookie");

        JSONObject response = getResponse(host, query);
        Log.d("DEBUG", response.toString());
        if (!response.getString("status").equals("ok")) {
            throw new WhatsAppException("There was a problem trying to request the code. Status=" + response.getString("status"));
        } else {
            Log.d("DEBUG", "Setting password: " + response.getString("pw"));
            password = response.getString("pw");
            return true;
        }
    }

    public String sendMessage(String to, String message) throws WhatsAppException {
        return sendMessage(to, message, null);
    }

    /**
     * Send a text message to the user/group.
     *
     * @return String
     */
    public String sendMessage(String to, String message, String id) throws WhatsAppException {
        message = parseMessageForEmojis(message);
        ProtocolNode bodyNode = new ProtocolNode("body", null, null, CharsetUtils.toBytes(message));
        try {
            return sendMessageNode(to, bodyNode, id);
        } catch (Exception e) {
            throw new WhatsAppException("Failed to send message", e);
        }
    }

    private List<Country> readCountries() throws WhatsAppException, IOException {
        List<Country> result = new LinkedList<Country>();
        AssetManager mngr = context.getAssets();
        InputStream is = mngr.open("countries.csv");
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        if (is == null) {
            throw new WhatsAppException("Failed to locate countries.csv");
        }

        try {

            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] entry = line.split(cvsSplitBy);
                Country country = new Country(entry);
                result.add(country);
            }

        } catch (FileNotFoundException e) {
            Log.w("WARNING", "File not found", e);
        } catch (IOException e) {
            Log.w("WARNING", "IO exception", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Log.w("WARNING", "IO exception", e);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w("WARNING", "IO exception", e);
                }
            }
        }
        return result;
    }

    protected List<Country> getCountries() {
        return countries;
    }

    protected String buildIdentity(String id) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] hash = hash("SHA-1", id.getBytes());
        String hashString = new String(hash, "iso-8859-1");
        String newId = URLEncoder.encode(hashString, "iso-8859-1").toLowerCase();
        Log.d("DEBUG", "ID: " + newId);
        
        return newId;
    }

    protected boolean checkIdentity(String id) throws UnsupportedEncodingException {
        if (id != null)
            return (URLDecoder.decode(id, "iso-8859-1").length() == 20);
        return false;
    }

    private void doLogin() throws InvalidKeyException, NoSuchAlgorithmException, IOException, InvalidKeySpecException, WhatsAppException, IncompleteMessageException, InvalidMessageException, InvalidTokenException, JSONException, EncodeException, DecodeException {
        writer.resetKey();
        reader.resetKey();
        String resource = WHATSAPP_DEVICE + "-" + WHATSAPP_VER + "-" + PORT;
        byte[] data = writer.startStream(WHATSAPP_SERVER, resource);
        ProtocolNode feat = createFeaturesNode(false);
        ProtocolNode auth = createAuthNode();
        sendData(data);
        sendNode(feat);
        sendNode(auth);

        pollMessages();
        pollMessages();
        pollMessages();

        if (challengeData != null) {
            ProtocolNode dataNode = createAuthResponseNode();
            sendNode(dataNode);
            reader.setKey(inputKey);
            writer.setKey(outputKey);
            pollMessages();
        }
        if (loginStatus == LoginStatus.DISCONNECTED_STATUS) {
            throw new WhatsAppException("Login failure");
        }
        int cnt = 0;
        poller = new MessagePoller(this);
        poller.start();
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new WhatsAppException(e);
            }
        } while ((cnt++ < 100) && (loginStatus == LoginStatus.DISCONNECTED_STATUS));
        sendPresence("available");
    }

    private void sendPresence(String type) throws IOException, WhatsAppException {
        Map<String, String> presence = new LinkedHashMap<String, String>();
        //		presence.put("type",type);
        presence.put("name", name);
        ProtocolNode node = new ProtocolNode("presence", presence, null, null);
        sendNode(node);
        eventManager.fireSendPresence(
                phoneNumber,
                type,
                presence.get("name")
        );
    }

    /**
     * Add the auth response to protocoltreenode.
     *
     * @return ProtocolNode
     * Return itself.
     * @throws EncodeException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private ProtocolNode createAuthResponseNode() throws EncodeException, IOException {
        byte[] resp = authenticate();
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        //		attributes.put("xmlns","urn:ietf:params:xml:ns:xmpp-sasl");
        ProtocolNode node = new ProtocolNode("response", attributes, null, resp);

        return node;
    }

    /**
     * Authenticate with the Whatsapp Server.
     *
     * @return byte[]
     * Returns binary string
     * @throws EncodeException
     * @throws IOException
     */
    byte[] authenticate() throws EncodeException, IOException {
        List<byte[]> keys = generateKeys();
        inputKey = new KeyStream(keys.get(2), keys.get(3));
        outputKey = new KeyStream(keys.get(0), keys.get(1));

        ByteArrayOutputStream array = new ByteArrayOutputStream();
        array.write(phoneNumber.getBytes());
        array.write(challengeData);
        //		array.write(Long.toString((new Date()).getTime()/1000).getBytes());
        byte[] response = outputKey.encode(array.toByteArray(), 0, 0, array.size());
        return response;
    }

    List<byte[]> generateKeys() throws EncodeException {
        try {
            List<byte[]> keys = new LinkedList<byte[]>();
            for (int i = 0; i < 4; ++i) {
                ByteArrayOutputStream nonce = getChallengeData();
                nonce.write(i + 1);
                byte[] key = pbkdf2("SHA-1", base64_decode(password), nonce.toByteArray(), 2, 20, true);
                keys.add(key);
            }
            return keys;
        } catch (Exception e) {
            throw new EncodeException(e);
        }
    }

    private ByteArrayOutputStream getChallengeData() throws NoSuchAlgorithmException, IOException {
        if (challengeData == null) {
            Log.i("INFO", "Challenge data is missing!");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            challengeData = new byte[20];
            sr.nextBytes(challengeData);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream(challengeData.length);
        os.write(challengeData);
        return os;
    }

    protected byte[] pbkdf2(String algo, byte[] password,
                            byte[] salt, int iterations, int length, boolean raw) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
        if (iterations <= 0 || length <= 0) {
            throw new InvalidKeySpecException("PBKDF2 ERROR: Invalid parameters.");
        }

        int hash_length = 20; //hash(algo, "", true).length();
        double block_count = Math.ceil(length / hash_length);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int i = 1; i <= block_count; i++) {
            ByteArrayOutputStream last = new ByteArrayOutputStream();
            last.write(salt);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(i);
            last.write(buffer.array());
            byte[] lastBuf = last.toByteArray();
            byte[] xorsum = KeyStream.hash_hmac(lastBuf, password);
            byte[] xorsum2 = xorsum;
            for (int j = 1; j < iterations; j++) {
                xorsum2 = KeyStream.hash_hmac(xorsum2, password);
                last.reset();
                int k = 0;
                for (byte b : xorsum) {
                    last.write(b ^ xorsum2[k++]);
                }
                xorsum = last.toByteArray();
            }
            output.write(xorsum);
        }
        if (raw) {
            return output.toByteArray();
        }
        return toHex(output.toByteArray()).getBytes();
    }

    public static String toHex(byte[] array) throws NoSuchAlgorithmException {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    byte[] base64_decode(String pwd) {
        return android.util.Base64.decode(pwd.getBytes(), 0); /** Should I add that zero here? */

    }

    private void processInboundData(byte[] readData) throws IncompleteMessageException, InvalidMessageException, InvalidTokenException, IOException, WhatsAppException, JSONException, NoSuchAlgorithmException, InvalidKeyException, DecodeException {
        if (readData == null || readData.length == 0) {
            return;
        }


        ProtocolNode node = reader.nextTree(readData);
        if (node != null) {
            processInboundDataNode(node);
        }
    }

    /**
     * Will process the data from the server after it's been decrypted and parsed.
     * <p>
     * This also provides a convenient method to use to unit test the event framework.
     *
     * @throws IOException
     * @throws InvalidTokenException
     * @throws InvalidMessageException
     * @throws IncompleteMessageException
     * @throws WhatsAppException
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     * @throws DecodeException
     * @throws InvalidKeyException
     */
    private void processInboundDataNode(ProtocolNode node) throws IncompleteMessageException, InvalidMessageException, InvalidTokenException, IOException, WhatsAppException, JSONException, NoSuchAlgorithmException, InvalidKeyException, DecodeException {
        while (node != null) {
            ProtocolTag tag;
            try {
                tag = ProtocolTag.fromString(node.getTag());
                if (tag == null) {
                    tag = ProtocolTag.UNKNOWN;
                    Log.i("INFO", "Unknown/Unused tag (null) {}");
                    //sendAck(node);
                }
            } catch (IllegalArgumentException e) {
                tag = ProtocolTag.UNKNOWN;
                Log.i("INFO", "Unknown/Unused tag " + node.getTag());
                Log.i("INFO", "Sending ack anywat to: {}");
                //sendAck(node);
            }

            Log.d("DEBUG", "rx  " + node);
            switch (tag) {
                case CHALLENGE:
                    processChallenge(node);
                    break;
                case SUCCESS:
                    loginStatus = LoginStatus.CONNECTED_STATUS;
                    challengeData = node.getData();
                    file_put_contents("nextChallenge.dat", challengeData);
                    writer.setKey(outputKey);
                    break;
                case FAILURE:
                    Log.e("ERROR", "Failure");
                    break;
                case MESSAGE:
                    processMessage(node);
                    break;
                case ACK:
                    processAck(node);
                    break;
                case RECEIPT:
                    processReceipt(node);
                    break;
                case PRESENCE:
                    processPresence(node);
                    break;
                case IQ:
                    processIq(node);
                    break;
                case IB:
                    processIb(node);
                    break;
                case NOTIFICATION:
                    processNotification(node);
                    break;
                case CHATSTATE:
                    processChatState(node);
                    break;
                case STREAM_ERROR:
                    throw new WhatsAppException("stream:error received: " + node);
                case PING:
                    break;
                case QUERY:
                    break;
                case START:
                    break;
                case UNKNOWN:
                    break;
                default:
                    break;
            }
            node = reader.nextTree(null);
        }
    }

    private void processChatState(ProtocolNode node) throws WhatsAppException {
        Log.d("DEBUG", "Processing CHATSTATE");
        String from = parseJID(node.getAttribute("from"));
        String groupId = null;
        if (from.contains("-")) {
            groupId = from;
            from = parseJID(node.getAttribute("participant"));
        }
        if (node.hasChild("composing")) {
            Event event = new Event(EventType.MESSAGE_COMPOSING, phoneNumber);
            event.setGroupId(groupId);
            event.setFrom(from);
            eventManager.fireEvent(event);
        }
        if (node.hasChild("paused")) {
            Event event = new Event(EventType.MESSAGE_PAUSED, phoneNumber);
            event.setGroupId(groupId);
            event.setFrom(from);
            eventManager.fireEvent(event);
        }
    }

    private void processNotification(ProtocolNode node) throws WhatsAppException {
        String name = node.getAttribute("notify");
        String type = node.getAttribute("type");
        Log.d("DEBUG", "Processing " + type + " NOTIFICATION: " + name);
        if (type.equals("status")) {

        }
        if (type.equals("picture")) {

        }
        if (type.equals("contacts")) {

        }
        if (type.equals("encrypt")) {

        }
        if (type.equals("w:gp2")) {
            List<ProtocolNode> groupList = node.getChild(0).getChildren();
            String groupId = parseJID(node.getAttribute("from"));

            if (node.hasChild("create")) {
                Event event = new Event(EventType.GROUP_CREATE, phoneNumber);
                event.setData(groupList);
                event.setGroupId(groupId);
                eventManager.fireEvent(event);
            }
            if (node.hasChild("add")) {
                Event event = new Event(EventType.GROUP_ADD, phoneNumber);
                event.setData(groupList);
                event.setGroupId(groupId);
                eventManager.fireEvent(event);
            }
            if (node.hasChild("remove")) {
                Event event = new Event(EventType.GROUP_REMOVE, phoneNumber);
                event.setData(groupList);
                event.setGroupId(groupId);
                eventManager.fireEvent(event);
            }
            if (node.hasChild("participant")) {

            }
            if (node.hasChild("subject")) {

            }

        }
        if (type.equals("account")) {

        }
        if (type.equals("features")) {

        }
        sendNotificationAck(node);
    }

    private void addServerReceivedId(String receivedId) {

        synchronized (serverReceivedId) {
            serverReceivedId.add(receivedId);
        }
    }

    private void sendNotificationAck(ProtocolNode node) throws WhatsAppException {
        String from = node.getAttribute("from");
        String to = node.getAttribute("to");
        String participant = node.getAttribute("participant");
        String id = node.getAttribute("id");
        String type = node.getAttribute("type");

        Map<String, String> attributes = new HashMap<String, String>();
        if (to != null && !to.isEmpty())
            attributes.put("from", to);
        if (participant != null && !participant.isEmpty())
            attributes.put("participant", participant);
        attributes.put("to", from);
        attributes.put("class", "notification");
        attributes.put("id", id);
        attributes.put("type", type);

        ProtocolNode ack = new ProtocolNode("ack", attributes, null, null);

        sendNode(ack);
    }

    private void processReceipt(ProtocolNode node) throws WhatsAppException {
        Log.d("DEBUG", "Processing RECEIPT");
        addServerReceivedId(node.getAttribute("id"));
        eventManager.fireMessageReceivedClient(
                phoneNumber,
                node.getAttribute("from"),
                node.getAttribute("id"),
                (node.getAttribute("type") == null ? "" : node.getAttribute("type")),
                node.getAttribute("t")
        );

        sendAck(node, "receipt");
    }

    //tx  <ack to="5491157492709@s.whatsapp.net" class="receipt" id="1434247851-1" type="read"></ack>

    private void sendAck(ProtocolNode node, String clazz) throws WhatsAppException {
        Map<String, String> attributes = new HashMap<String, String>();

        attributes.put("to", node.getAttribute("from"));
        attributes.put("class", clazz);
        attributes.put("id", node.getAttribute("id"));
        if (node.getAttribute("type") != null)
            attributes.put("type", node.getAttribute("type"));

        ProtocolNode ack = new ProtocolNode("ack", attributes, null, null);
        sendNode(ack);
    }

    private void processAck(ProtocolNode node) {
        Log.d("DEBUG", "Processing ACK");
        addServerReceivedId(node.getAttribute("id"));
    }

    private void processIb(ProtocolNode node) throws IOException, WhatsAppException, IncompleteMessageException, InvalidMessageException, InvalidTokenException, JSONException, NoSuchAlgorithmException {
        String type = node.getAttribute("type");
        Log.i("INFO", "Processing IB " + (type == null ? "" : type));
        for (ProtocolNode n : node.getChildren()) {
            ProtocolTag tag = ProtocolTag.fromString(n.getTag());
            switch (tag) {
                case DIRTY:
                    List<String> categories = new LinkedList<String>();
                    categories.add(n.getAttribute("type"));
                    sendClearDirty(categories);
                    break;
                case OFFLINE:
                    Log.i("INFO", "Offline count" + n.getAttribute("count"));
                    break;
                default:
            }
        }
    }

    private void processIq(ProtocolNode node) throws IOException, WhatsAppException, IncompleteMessageException, InvalidMessageException, InvalidTokenException, JSONException, NoSuchAlgorithmException, InvalidKeyException, DecodeException {

        Log.i("INFO", "Processing IQ " + node.getAttribute("type"));
        ProtocolNode child = node.getChild(0);

        if (node.getAttribute("type").equals("get") && node.getAttribute("xmlns").equals("urn:xmpp:ping")) {
            eventManager.firePing(phoneNumber, node.getAttribute("id"));
            sendPong(node.getAttribute("id"));
        }
        if (node.getAttribute("type").equals("result")) {

            Log.d("DEBUG", "processIq: setting received id to " + node.getAttribute("id"));

            addServerReceivedId(node.getAttribute("id"));

            if (child != null) {
                if (child.getTag().equals(ProtocolTag.QUERY.toString())) {
                    if (child.getAttribute("xmlns").equals("jabber:iq:privacy")) {
                        // ToDo: We need to get explicitly list out the children as arguments
                        //       here.
                        eventManager.fireGetPrivacyBlockedList(
                                phoneNumber,
                                child.getChild(0).getChildren()
                        );
                    }
                    if (child.getAttribute("xmlns").equals("jabber:iq:last")) {
                        eventManager.fireGetRequestLastSeen(
                                phoneNumber,
                                node.getAttribute("from"),
                                node.getAttribute("id"),
                                child.getAttribute("seconds")
                        );
                    }
                }
                if (child.getTag().equals(ProtocolTag.SYNC.toString())) {
                    //sync result
                    ProtocolNode sync = child;
                    ProtocolNode existing = sync.getChild("in");
                    ProtocolNode nonexisting = sync.getChild("out");

                    //process existing first
                    Map<String, String> existingUsers = new HashMap<String, String>();
                    if (existing != null) {
                        for (ProtocolNode eChild : existing.getChildren()) {
                            existingUsers.put(new String(eChild.getData()), eChild.getAttribute("jid"));
                        }
                    }

                    //now process failed numbers
                    List<String> failedNumbers = new LinkedList<String>();
                    if (nonexisting != null) {
                        for (ProtocolNode neChild : nonexisting.getChildren()) {
                            failedNumbers.add(new String(neChild.getData()));
                        }
                    }

                    String index = sync.getAttribute("index");

                    SyncResult result = new SyncResult(index, sync.getAttribute("sid"), existingUsers, failedNumbers);
                    Log.d("DEBUG", "Sync result: " + result.toString());
                    Event event = new Event(EventType.SYNC_RESULTS, phoneNumber);
                    event.setEventSpecificData(result);

                    eventManager.fireEvent(event);
                }
                messageQueue.add(node);
            }
            if (child != null && child.getTag().equals("props")) {
                //server properties
                Map<String, String> props = new LinkedHashMap<String, String>();
                for (ProtocolNode c : child.getChildren()) {
                    props.put(c.getAttribute("name"), c.getAttribute("value"));
                }
                eventManager.fireGetServerProperties(
                        phoneNumber,
                        child.getAttribute("version"),
                        props
                );
            }
            if (child != null && child.getTag().equals("picture")) {
                eventManager.fireGetProfilePicture(
                        phoneNumber,
                        node.getAttribute("from"),
                        child.getAttribute("type"),
                        child.getData()
                );
            }
            if (child != null && child.getTag().equals("media")) {
                processUploadResponse(node);
            }
            if (child != null && child.getTag().equals("duplicate")) {
                processUploadResponse(node);
            }
            if (node.nodeIdContains("group")) {
                //There are multiple types of Group reponses. Also a valid group response can have NO children.
                //Events fired depend on text in the ID field.
                List<ProtocolNode> groupList = null;
                String groupId = null;
                if (child != null) {
                    groupList = child.getChildren();
                }
                if (node.nodeIdContains("creategroup")) {
                    groupId = child.getAttribute("id");
                    Event event = new Event(EventType.GROUP_CREATE, phoneNumber);
                    event.setData(groupList);
                    event.setGroupId(groupId);
                    eventManager.fireEvent(event);
                }
                if (node.nodeIdContains("endgroup")) {
                    groupId = child.getChild(0).getAttribute("id");
                    Event event = new Event(EventType.GROUP_END, phoneNumber);
                    event.setData(groupList);
                    event.setGroupId(groupId);
                    eventManager.fireEvent(event);
                }
                if (node.nodeIdContains("getgroups")) {
                    Event event = new Event(EventType.GET_GROUPS, phoneNumber);
                    event.setData(groupList);
                    eventManager.fireEvent(event);

                }
                if (node.nodeIdContains("getgroupinfo")) {
                    Event event = new Event(EventType.GET_GROUPINFO, phoneNumber);
                    event.setData(groupList);
                    eventManager.fireEvent(event);
                }
                if (node.nodeIdContains("getgroupparticipants")) {
                    groupId = parseJID(node.getAttribute("from"));
                    Event event = new Event(EventType.GET_GROUPS, phoneNumber);
                    event.setData(groupList);
                    event.setGroupId(groupId);
                    eventManager.fireEvent(event);
                }

            }

            if (node.getTag().equals("iq") && node.getAttribute("type").equals("error")) {
                addServerReceivedId(node.getAttribute("id"));
            }
        }

    }

    public String getLastSendMsgId() {
        return this.lastSendMsgId;
    }

    /**
     * Process media upload response
     *
     * @return bool
     * @throws WhatsAppException
     * @throws InvalidTokenException
     * @throws InvalidMessageException
     * @throws IncompleteMessageException
     * @throws IOException
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     * @throws DecodeException
     * @throws InvalidKeyException
     */
    private boolean processUploadResponse(ProtocolNode node) throws IOException, IncompleteMessageException, InvalidMessageException, InvalidTokenException, WhatsAppException, JSONException, NoSuchAlgorithmException, InvalidKeyException, DecodeException {
        String url = null;
        String filesize = null;
        String filetype = null;
        String filename = null;
        String to = null;
        String id = node.getAttribute("id");
        Map<String, Object> messageNode = mediaQueue.get(id);
        if (messageNode == null) {
            //message not found, can't send!
            eventManager.fireMediaUploadFailed(
                    phoneNumber,
                    id,
                    node,
                    messageNode,
                    "Message node not found in queue"
            );
            return false;
        }

        ProtocolNode duplicate = node.getChild("duplicate");
        if (duplicate != null) {
            //file already on whatsapp servers
            url = duplicate.getAttribute("url");
            filesize = duplicate.getAttribute("size");
            filetype = duplicate.getAttribute("type");
            String[] exploded = url.split("/");
            filename = exploded[exploded.length - 1];
            mediaInfo = createMediaInfo(duplicate);
        } else {
            //upload new file
            JSONObject json = WhatsMediaUploader.pushFile(node, messageNode, mediaFile.getMediaFile(), phoneNumber);

            if (json == null) {
                //failed upload
                eventManager.fireMediaUploadFailed(
                        phoneNumber,
                        id,
                        node,
                        messageNode,
                        "Failed to push file to server"
                );
                return false;
            }

            Log.d("DEBUG", "Setting mediaInfo to: " + json.toString());
            
            mediaInfo = json;
            url = json.getString("url");
            filesize = json.getString("size");
            filetype = json.getString("type");
            filename = json.getString("name");
        }

        Map<String, String> mediaAttribs = new HashMap<String, String>();
        mediaAttribs.put("xmlns", "urn:xmpp:whatsapp:mms");
        mediaAttribs.put("type", filetype);
        mediaAttribs.put("url", url);
        mediaAttribs.put("encoding", "raw");
        mediaAttribs.put("file", filename);
        mediaAttribs.put("size", filesize);
        if (messageNode.containsKey("caption") && !((String) messageNode.get("caption")).isEmpty()) {
            mediaAttribs.put("caption", (String) messageNode.get("caption"));
        }

        to = (String) messageNode.get("to");

        byte[] icon = null;
        if (filetype.equals("image")) {
            icon = readFile(mediaFile.getPreviewFile());
        }
        if (filetype.equals("video")) {
            icon = readFile(mediaFile.getPreviewFile());
        }

        ProtocolNode mediaNode = new ProtocolNode("media", mediaAttribs, null, icon);
        /*
         * TODO support multiple recipients
		 */
        //        if (is_array($to)) {
        //            $this->sendBroadcast($to, $mediaNode);
        //        } else {
        //            $this->sendMessageNode($to, $mediaNode);
        //        }
        sendMessageNode(to, mediaNode, null);
        eventManager.fireMediaMessageSent(
                phoneNumber,
                to,
                id,
                filetype,
                url,
                filename,
                filesize,
                icon
        );
        return true;
    }

    private JSONObject createMediaInfo(ProtocolNode duplicate) {
        JSONObject info = new JSONObject();
        Map<String, String> attributes = duplicate.getAttributes();
        for (String key : attributes.keySet()) {
            try {
                info.put(key, attributes.get(key));
            } catch (JSONException e) {
                Log.w("WARNING", "Failed to add " + key + " to media info: " + e.getMessage());
            }
        }

        Log.d("DEBUG", "Created media info (for duplicate): " + info.toString());

        return info;
    }

    private void sendPong(String msgid) throws IOException, WhatsAppException {
        Map<String, String> messageHash = new LinkedHashMap<String, String>();
        messageHash.put("to", WHATSAPP_SERVER);
        messageHash.put("id", msgid);
        messageHash.put("type", "result");

        ProtocolNode messageNode = new ProtocolNode("iq", messageHash, null, null);
        sendNode(messageNode);
        eventManager.fireSendPong(
                phoneNumber,
                msgid
        );
    }

    private void file_put_contents(String string, Object challengeData2) {
        // TODO Auto-generated method stub

    }

    private void processChallenge(ProtocolNode node) {
        Log.d("DEBUG", "processChallenge: " + node.getData().length);
        challengeData = node.getData();
    }

    private void processPresence(ProtocolNode node) throws WhatsAppException {
        if (node.getAttribute("status") != null && node.getAttribute("status").equals("dirty")) {
            //clear dirty
            List<String> categories = new LinkedList<String>();
            if (node.getChildren() != null && node.getChildren().size() > 0) {
                for (ProtocolNode child : node.getChildren()) {
                    if (child.getTag().equals("category")) {
                        categories.add(child.getAttribute("name"));
                    }
                }
            }
            sendClearDirty(categories);
        }
        String from = node.getAttribute("from");
        String type = node.getAttribute("type");
        if (from != null && type != null) {
            if (from.startsWith(phoneNumber)
                    && !from.contains("-")) {
                eventManager.firePresence(
                        phoneNumber,
                        from,
                        type
                );
            }
            if (!from.startsWith(phoneNumber)
                    && from.contains("-")) {
            }
        }
    }

    private String parseJID(String attribute) {
        String[] parts = attribute.split("@");
        return parts[0];
    }

    private void sendClearDirty(List<String> categories) throws WhatsAppException {
        String msgId = createMsgId("cleardirty");

        List<ProtocolNode> catnodes = new LinkedList<ProtocolNode>();
        for (String category : categories) {
            Map<String, String> catmap = new HashMap<String, String>();
            catmap.put("type", category);
            ProtocolNode catnode = new ProtocolNode("clean", catmap, null, null);
            catnodes.add(catnode);
        }
        Map<String, String> nodemap = new HashMap<String, String>();
        nodemap.put("id", msgId);
        nodemap.put("type", "set");
        nodemap.put("to", WHATSAPP_SERVER);
        nodemap.put("xmlns", "urn:xmpp:whatsapp:dirty");
        ProtocolNode node = new ProtocolNode("iq", nodemap, catnodes, null);
        sendNode(node);
    }

    private void processMessage(ProtocolNode node) throws IOException, WhatsAppException {
        Log.d("DEBUG", "processMessage:");
        messageQueue.add(node);

        //do not send received confirmation if sender is yourself
        if (node.getAttribute("type").equals("text")) {
            sendMessageReceived(node, "read");
        }
        if (node.getAttribute("type").equals("media")) {
            processMediaMessage(node);
            sendMessageReceived(node, "read");
        }
        // check if it is a response to a status request
        String[] foo = node.getAttribute("from").split("@");
        if (foo.length > 1 && foo[1].equals("s.us") && node.getChild("body") != null) {
            eventManager.fireGetStatus(
                    phoneNumber,
                    node.getAttribute("from"),
                    node.getAttribute("type"),
                    node.getAttribute("id"),
                    node.getAttribute("t"),
                    node.getChild("body").getData()
            );
        }
        if (node.hasChild("x") && lastId.equals(node.getAttribute("id"))) {
            sendNextMessage();
        }

        if (processor != null && (node.hasChild("body") || node.hasChild("media"))) {
            Message message = createMessage(node);
            processor.processMessage(message);
        }

        if (node.hasChild("notify") && node.getChild(0).getAttribute("name") != null &&
                node.getChild(0).getAttribute("name").length() < 1 && node.getChild("body") != null) {
            String author = node.getAttribute("author");
            if (author == null || author.length() < 1) {
                //private chat message
                eventManager.fireGetMessage(
                        phoneNumber,
                        node.getAttribute("from"),
                        node.getAttribute("id"),
                        node.getAttribute("type"),
                        node.getAttribute("t"),
                        node.getChild("notify").getAttribute("name"),
                        node.getChild("body").getData()
                );
            } else {
                //group chat message
                eventManager.fireGetGroupMessage(
                        phoneNumber,
                        node.getAttribute("from"),
                        author,
                        node.getAttribute("id"),
                        node.getAttribute("type"),
                        node.getAttribute("t"),
                        node.getChild("notify").getAttribute("name"),
                        node.getChild("body").getData()
                );
            }
        }
        if (node.hasChild("notification") && node.getChild("notification").getAttribute("type").equals("picture")) {
            if (node.getChild("notification").hasChild("set")) {
                eventManager.fireProfilePictureChanged(
                        phoneNumber,
                        node.getAttribute("from"),
                        node.getAttribute("id"),
                        node.getAttribute("t")
                );
            } else if (node.getChild("notification").hasChild("delete")) {
                eventManager.fireProfilePictureDeleted(
                        phoneNumber,
                        node.getAttribute("from"),
                        node.getAttribute("id"),
                        node.getAttribute("t")
                );
            }
        }
        if (node.getChild("notify") != null && node.getChild(0).getAttribute("name") != null && node.getChild("media") != null) {
            if (node.getChild(2).getAttribute("type") == "image") {
                eventManager.fireGetImage(
                        phoneNumber,
                        node.getAttribute("from"),
                        node.getAttribute("id"),
                        node.getAttribute("type"),
                        node.getAttribute("t"),
                        node.getChild(0).getAttribute("name"),
                        node.getChild(2).getAttribute("size"),
                        node.getChild(2).getAttribute("url"),
                        node.getChild(2).getAttribute("file"),
                        node.getChild(2).getAttribute("mimetype"),
                        node.getChild(2).getAttribute("filehash"),
                        node.getChild(2).getAttribute("width"),
                        node.getChild(2).getAttribute("height"),
                        node.getChild(2).getData()
                );
            }
            if (node.getChild(2).getAttribute("type") == "video") {
                eventManager.fireGetVideo(
                        phoneNumber,
                        node.getAttribute("from"),
                        node.getAttribute("id"),
                        node.getAttribute("type"),
                        node.getAttribute("t"),
                        node.getChild(0).getAttribute("name"),
                        node.getChild(2).getAttribute("url"),
                        node.getChild(2).getAttribute("file"),
                        node.getChild(2).getAttribute("size"),
                        node.getChild(2).getAttribute("mimetype"),
                        node.getChild(2).getAttribute("filehash"),
                        node.getChild(2).getAttribute("duration"),
                        node.getChild(2).getAttribute("vcodec"),
                        node.getChild(2).getAttribute("acodec"),
                        node.getChild(2).getData()
                );
            } else if (node.getChild(2).getAttribute("type") == "audio") {
                eventManager.fireGetAudio(
                        phoneNumber,
                        node.getAttribute("from"),
                        node.getAttribute("id"),
                        node.getAttribute("type"),
                        node.getAttribute("t"),
                        node.getChild(0).getAttribute("name"),
                        node.getChild(2).getAttribute("size"),
                        node.getChild(2).getAttribute("url"),
                        node.getChild(2).getAttribute("file"),
                        node.getChild(2).getAttribute("mimetype"),
                        node.getChild(2).getAttribute("filehash"),
                        node.getChild(2).getAttribute("duration"),
                        node.getChild(2).getAttribute("acodec")
                );
            }
            if (node.getChild(2).getAttribute("type") == "vcard") {
                eventManager.fireGetvCard(
                        phoneNumber,
                        node.getAttribute("from"),
                        node.getAttribute("id"),
                        node.getAttribute("type"),
                        node.getAttribute("t"),
                        node.getChild(0).getAttribute("name"),
                        node.getChild(2).getChild(0).getAttribute("name"),
                        node.getChild(2).getChild(0).getData()
                );
            }
            if (node.getChild(2).getAttribute("type") == "location") {
                String url = node.getChild(2).getAttribute("url");
                String name = node.getChild(2).getAttribute("name");
                eventManager.fireGetLocation(
                        phoneNumber,
                        node.getAttribute("from"),
                        node.getAttribute("id"),
                        node.getAttribute("type"),
                        node.getAttribute("t"),
                        node.getChild(0).getAttribute("name"),
                        name,
                        node.getChild(2).getAttribute("longitude"),
                        node.getChild(2).getAttribute("latitude"),
                        url,
                        node.getChild(2).getData()
                );
            }
        }
        if (node.getChild("x") != null) {
        	
            Log.d("DEBUG", "processMessage: setting received id to " + node.getAttribute("id"));

            addServerReceivedId(node.getAttribute("id"));
            eventManager.fireMessageReceivedServer(
                    phoneNumber,
                    node.getAttribute("from"),
                    node.getAttribute("id"),
                    node.getAttribute("type"),
                    node.getAttribute("t")
            );
        }
        if (node.getChild("received") != null) {
            eventManager.fireMessageReceivedClient(
                    phoneNumber,
                    node.getAttribute("from"),
                    node.getAttribute("id"),
                    node.getAttribute("type"),
                    node.getAttribute("t")
            );
        }
        if (node.getAttribute("type").equals("subject")) {
            Log.d("DEBUG", node.toString());
            String[] reset_from = node.getAttribute("from").split("@");
            String[] reset_author = node.getAttribute("author").split("@");
            eventManager.fireGetGroupsSubject(
                    phoneNumber,
                    reset_from,
                    node.getAttribute("t"),
                    reset_author,
                    reset_author,
                    node.getChild(0).getAttribute("name"),
                    node.getChild(2).getData()
            );
        }
    }

    private Message createMessage(ProtocolNode message) {

        String from = parseJID(message.getAttribute("from"));
        String contentType = message.getAttribute("type");
        String participant = message.getAttribute("participant");
        String group = null;
        if (participant != null && !participant.isEmpty()) {
            group = from;
            from = parseJID(participant);
        }
        if (contentType.equals("text")) {
            ProtocolNode body = message.getChild("body");
            String hex = new String(body.getData());
            TextMessage text = new TextMessage(message, from, group);
            text.setText(hex);
            return text;
        }
        if (contentType.equals("media")) {
            ProtocolNode media = message.getChild("media");
            String type = media.getAttribute("type");
            if (type.equals("location")) {
                LocationMessage msg = new LocationMessage(message, from, group);
                msg.setLongitude(media.getAttribute("longitude"));
                msg.setLatitude(media.getAttribute("latitude"));
            } else if (type.equals("image")) {
                ImageMessage msg = new ImageMessage(message, from, group);
                String caption = media.getAttribute("caption");

                if (caption == null)
                    caption = "";
                msg.setCaption(caption);
                byte[] preview = media.getData();
                msg.setPreview(preview);
                msg.setContent(media.getAttribute("url"));
                return msg;
            } else if (type.equals("video")) {
                VideoMessage msg = new VideoMessage(message, from, group);
                String caption = media.getAttribute("caption");

                if (caption == null)
                    caption = "";
                msg.setCaption(caption);
                byte[] preview = media.getData();
                msg.setPreview(preview);
                msg.setContent(media.getAttribute("url"));
                return msg;
            } else if (type.equals("audio")) {
                AudioMessage msg = new AudioMessage(message, from, group);
                String caption = media.getAttribute("caption");

                if (caption == null)
                    caption = "";
                msg.setCaption(caption);
                msg.setContent(media.getAttribute("url"));
                msg.setAbitrate(media.getAttribute("abitrate"));
                msg.setAcodec(media.getAttribute("acodec"));
                msg.setAsampfreq(media.getAttribute("asampfreq"));
                msg.setDuration(media.getAttribute("duration"));
                msg.setFile(media.getAttribute("file"));
                msg.setFileHash(media.getAttribute("filehash"));
                msg.setIp(media.getAttribute("ip"));
                msg.setMimetype(media.getAttribute("mimetype"));
                msg.setOrigin(media.getAttribute("origin"));
                msg.setSeconds(media.getAttribute("seconds"));
                msg.setSize(media.getAttribute("size"));
                return msg;
            }

        }
        //TODO add specific classes for all supported messages
        Log.i("INFO", "Other message type found: " + message.toString());
        BasicMessage msg = new BasicMessage(MessageType.OTHER, message, from, group);
        return msg;
    }

    private void processMediaMessage(ProtocolNode node) throws WhatsAppException {
        // TODO Auto-generated method stub
        if (node.getChild(0).getAttribute("type").equals("image")) {
            String msgId = createMsgId("ack-media");

            Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("url", node.getChild(0).getAttribute("url"));
            ProtocolNode ackNode = new ProtocolNode("ack", attributes, null, null);

            Map<String, String> iqAttributes = new HashMap<String, String>();
            iqAttributes.put("id", msgId);
            iqAttributes.put("xmlns", "w:m");
            iqAttributes.put("type", "set");
            iqAttributes.put("to", WHATSAPP_SERVER);
            List<ProtocolNode> nodeList = new LinkedList<ProtocolNode>();
            nodeList.add(ackNode);
            ProtocolNode iqNode = new ProtocolNode("iq", iqAttributes, nodeList, null);

            sendNode(iqNode);
        }

    }

    private void sendNextMessage() throws IOException, WhatsAppException {
        if (outQueue.size() > 0) {
            ProtocolNode msgnode = outQueue.remove(0);
            msgnode.refreshTimes();
            lastId = msgnode.getAttribute("id");
            sendNode(msgnode);
        } else {
            lastId = null;
        }
    }

    private void sendMessageReceived(ProtocolNode msg, String type) throws IOException, WhatsAppException {
        Map<String, String> messageHash = new LinkedHashMap<String, String>();
        messageHash.put("to", msg.getAttribute("from"));
        if (type != null && type.equals("read"))
            messageHash.put("type", "type");

        messageHash.put("id", msg.getAttribute("id"));
        messageHash.put("t", Long.toString(new Date().getTime()));
        ProtocolNode messageNode = new ProtocolNode("receipt", messageHash, null, null);
        sendNode(messageNode);
        eventManager.fireSendMessageReceived(
                phoneNumber,
                msg.getAttribute("from"),
                messageHash.get("t")
        );
    }

    private byte[] readData() throws IOException {
        byte[] buf = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (socket != null && socket.isConnected()) {
            InputStream stream = socket.getInputStream();
            buf = new byte[3];
            try {
                //Read header first
                int ret = stream.read(buf);
                if (ret == 3) {
                    //					log.debug("Read header: "+ProtocolNode.bin2hex(Arrays.copyOf(buf, ret)));
                    int treeLength = ((buf[0] & 0x0f) & 0xFF) << 16;
                    treeLength += (buf[1] & 0xFF) << 8;
                    treeLength += (buf[2] & 0xFF) << 0;
                    //					log.debug("Tree length = "+treeLength);
                    out.write(buf);
                    buf = new byte[treeLength];
                    int read = 0;
                    while (read < treeLength) {
                        ret = stream.read(buf);
                        if (ret > 0) {
                            //							log.debug("Read content: "+ProtocolNode.bin2hex(Arrays.copyOf(buf, ret)));
                            out.write(Arrays.copyOf(buf, ret));
                            read += ret;
                        } else {
                            if (ret == 0) {
                                break;
                            } else {
                                Log.e("ERROR", "socket EOF, closing socket...");
                                socket.close();
                                socket = null;
                                disconnect();
                            }
                        }
                    }
                } else {
                    if (ret > 0) {
                        Log.w("WARNING", "Failed to read stanza header");
                    }
                    if (ret == -1) {
                        Log.e("ERROR", "socket EOF, closing socket...");
                        socket.close();
                        socket = null;
                        disconnect();
                    }
                }
            } catch (SocketTimeoutException e) {

            } catch (SocketException e) {
                Log.e("ERROR", "Exception reading from socket: " + e.getMessage());
                socket.close();
                socket = null;
                disconnect();
            }
        }
        byte[] outBytes = out.toByteArray();
        return outBytes;
    }

    private void sendNode(ProtocolNode node) throws WhatsAppException {
        try {
            byte[] data = writer.write(node, true);
            Log.d("DEBUG", "tx: " + node.toString());
            sendData(data);
        } catch (Exception e) {
            throw new WhatsAppException("Failed to send node", e);
        }
    }

    private void sendData(byte[] data) throws IOException {
        if (socket != null && socket.isConnected()) {
            socket.getOutputStream().write(data);
        }
    }

    /**
     * Add the authentication nodes.
     *
     * @return ProtocolNode
     * Return itself.
     * @throws EncodeException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private ProtocolNode createAuthNode() throws NoSuchAlgorithmException, EncodeException, IOException {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        //		attributes.put("xmlns", "urn:ietf:params:xml:ns:xmpp-sasl");
        attributes.put("mechanism", "WAUTH-2");
        attributes.put("user", phoneNumber);
        byte[] data;
        data = createAuthBlob();
        ProtocolNode node = new ProtocolNode("auth", attributes, null, data);

        return node;
    }


    private byte[] createAuthBlob() throws EncodeException, IOException, NoSuchAlgorithmException {
        if (challengeData != null) {
            // TODO
            //			byte[] key = pbkdf2("PBKDF2WithHmacSHA1", base64_decode(password), challengeData, 16, 20, true);
            List<byte[]> keys = generateKeys();
            inputKey = new KeyStream(keys.get(2), keys.get(3));
            outputKey = new KeyStream(keys.get(0), keys.get(1));
            reader.setKey(inputKey);
            //			writer.setKey(outputKey);
            Map<String, String> phone = dissectPhone();
            ByteArrayOutputStream array = new ByteArrayOutputStream();
            array.write(phoneNumber.getBytes());
            array.write(challengeData);
            array.write(time().getBytes());
            array.write(WHATSAPP_USER_AGENT.getBytes());
            array.write(" MccMnc/".getBytes());
            array.write(phone.get("mcc").getBytes());
            array.write("001".getBytes());
            Log.d("DEBUG", "createAuthBlog: challengeData=" + toHex(challengeData));
            Log.d("DEBUG", "createAuthBlog: array=" + toHex(array.toByteArray()));
            challengeData = null;
            return outputKey.encode(array.toByteArray(), 0, 4, array.size() - 4);
        }
        return null;
    }

    /**
     * Dissect country code from phone number.
     *
     * @return map
     * An associative map with country code and phone number.
     * - country: The detected country name.
     * - cc: The detected country code (phone prefix).
     * - phone: The phone number.
     * - ISO3166: 2-Letter country code
     * - ISO639: 2-Letter language code
     * Return null if country code is not found.
     */
    private Map<String, String> dissectPhone() {
        Map<String, String> ret = new LinkedHashMap<String, String>();
        for (Country country : countries) {
            if (phoneNumber.startsWith(country.getCountryCode())) {
                ret.put("country", country.getName());
                ret.put("cc", country.getCountryCode());
                ret.put("phone", phoneNumber.substring(country.getCountryCode().length()));
                ret.put("mcc", country.getMcc());
                ret.put("ISO3166", country.getIso3166());
                ret.put("ISO639", country.getIso639());
                return ret;
            }
        }
        return null;
    }

    private String time() {
        Date now = new Date();

        return Long.toString(now.getTime() / 1000);
    }

    /**
     * Add stream features.
     *
     * @return ProtocolNode
     * Return itself.
     */
    private ProtocolNode createFeaturesNode(boolean profileSubscribe) {
        LinkedList<ProtocolNode> nodes = new LinkedList<ProtocolNode>();
        ProtocolNode node = new ProtocolNode("readreceipts", null, null, null);
        nodes.add(node);
        if (profileSubscribe) {
            Map<String, String> attributes = new LinkedHashMap<String, String>();
            attributes.put("type", "all");
            ProtocolNode profile = new ProtocolNode("w:profile:picture", attributes, null, null);
            nodes.add(profile);
        }
        node = new ProtocolNode("privacy", null, null, null);
        nodes.add(node);
        node = new ProtocolNode("presence", null, null, null);
        nodes.add(node);
        node = new ProtocolNode("groups_v2", null, null, null);
        nodes.add(node);
        ProtocolNode parent = new ProtocolNode("stream:features", null, nodes, null);

        return parent;
    }


    private JSONObject getResponse(String host, Map<String, String> query) throws JSONException {

        /**
         * Just a quick test, maybe it will work! Cross fingers!
         */
    	
        StringBuilder url = new StringBuilder();
        url.append(host);
        String delimiter = "?";
        for (String key : query.keySet()) {
            url.append(delimiter);
            url.append(key);
            url.append("=");
            url.append(query.get(key));
            delimiter = "&";
        }

        
        HttpClient httpClient = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(httpClient.getParams(), WHATSAPP_USER_AGENT);
        HttpPost httpPost = new HttpPost(url.toString());

            HttpResponse response;
			try {
				response = httpClient.execute(httpPost);
	            Log.d("Response", response.toString());
	            String result = EntityUtils.toString(response.getEntity()); 
	            return new JSONObject(result);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return null; // What should I do?
			} catch (IOException e) {
				e.printStackTrace();
				return null; // What should I do?
			}
       
		
    }


    /**
     * Send node to the servers.
     *
     * @param to   The recipient to send.
     * @param node The node that contains the message.
     * @return message id
     * @throws IOException
     * @throws InvalidTokenException
     * @throws InvalidMessageException
     * @throws IncompleteMessageException
     * @throws WhatsAppException
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     * @throws DecodeException
     * @throws InvalidKeyException
     */
    private String sendMessageNode(String to, ProtocolNode node, String id) throws IOException, IncompleteMessageException, InvalidMessageException, InvalidTokenException, WhatsAppException, JSONException, NoSuchAlgorithmException, InvalidKeyException, DecodeException {
        Map<String, String> messageHash = new LinkedHashMap<String, String>();
        messageHash.put("to", getJID(to));
        if (node.getTag().equals("body")) {
            messageHash.put("type", "text");
        } else {
            messageHash.put("type", "media");
        }
        messageHash.put("id", (id == null ? createMsgId("message") : id));
        messageHash.put("t", time());

        List<ProtocolNode> list = new LinkedList<ProtocolNode>();
        list.add(node);
        ProtocolNode messageNode = new ProtocolNode("message", messageHash, list, null);
        sendNode(messageNode);
        eventManager.fireSendMessage(
                phoneNumber,
                getJID(to),
                messageHash.get("id"),
                node
        );
        return lastSendMsgId = messageHash.get("id");
    }

    private void waitForServer(String id) throws IncompleteMessageException, InvalidMessageException, InvalidTokenException, IOException, WhatsAppException, JSONException, NoSuchAlgorithmException, InvalidKeyException, DecodeException {
        long start = System.currentTimeMillis();
        while (!checkReceivedId(id) && (System.currentTimeMillis() - start) < 10000) {
            if (poller.isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                pollMessages();
            }
        }
        Log.d("DEBUG", "waitForServer done waiting for " + id);
    }

    private boolean checkReceivedId(String id) {

        synchronized (serverReceivedId) {
            Log.d("DEBUG", "Checking received id (" + serverReceivedId + " against " + id);
            if (serverReceivedId != null && serverReceivedId.contains(id)) {
            Log.d("DEBUG", "received id matched");
                serverReceivedId.remove(id);
                return true;
            }
            Log.d("DEBUG", "received id did NOT match");
            return false;
        }
    }

    public synchronized void pollMessages() throws InvalidKeyException, NoSuchAlgorithmException, IncompleteMessageException, InvalidMessageException, InvalidTokenException, IOException, WhatsAppException, JSONException, DecodeException {
        //		log.debug("Polling messages");
        processInboundData(readData());
    }

    private String createMsgId(String prefix) {
        String msgid = prefix + "-" + time() + "-" + ++messageCounter;

        return msgid;
    }

    private String getJID(String number) {
        if (!number.contains("@")) {
            //check if group message
            if (number.contains("-")) {
                //to group
                number = number + "@" + WHATSAPP_GROUP_SERVER;
            } else {
                //to normal user
                number = number + "@" + WHATSAPP_SERVER;
            }
        }

        return number;
    }

    /**
     * Parse the message text for emojis
     * <p>
     * This will look for special strings in the message text
     * that need to be replaced with a unicode character to show
     * the corresponding emoji.
     * <p>
     * Emojis should be entered in the message text either as the
     * correct unicode character directly, or if this isn't possible,
     * by putting a placeholder of ##unicodeNumber## in the message text.
     * Include the surrounding ##
     * eg:
     * ##1f604## this will show the smiling face
     * ##1f1ec_1f1e7## this will show the UK flag.
     * <p>
     * Notice that if 2 unicode characters are required they should be joined
     * with an underscore.
     *
     * @return string
     */
    private String parseMessageForEmojis(String txt) {
        // TODO Auto-generated method stub
        return txt;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void setChallengeData(String challenge) {
        challengeData = BinHex.hex2bin(challenge);
    }

    public void setPassword(String pw) {
        this.password = pw;
    }

    public KeyStream getInputKey() {
        return inputKey;
    }

    public void setInputKey(KeyStream inputKey) {
        this.inputKey = inputKey;
    }

    public KeyStream getOutputKey() {
        return outputKey;
    }

}

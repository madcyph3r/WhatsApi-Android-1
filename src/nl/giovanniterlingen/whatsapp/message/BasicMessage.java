package nl.giovanniterlingen.whatsapp.message;

import nl.giovanniterlingen.whatsapp.ProtocolNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

import java.util.Date;

public class BasicMessage implements Message {

    private final MessageType type;
    private final ProtocolNode node;
    private final String from;
    private final String groupId;
    private final Date date;

    public BasicMessage(MessageType type, ProtocolNode node, String from, String groupId) {
        this.type = type;
        this.node = node;
        this.from = from;
        this.groupId = groupId;
        String t = node.getAttribute("t");
        Log.d("DEBUG", "t=" + t);
        if (t != null) {
            long tl = Long.parseLong(t) * 1000;
            this.date = new Date(tl);
        } else {
            this.date = new Date();
        }
        Log.d("DEBUG", "date = " + date);
    }

    public MessageType getType() {
        return type;
    }

    public ProtocolNode getProtocolNode() {
        return node;
    }

    public String getFrom() {
        return from;
    }

    public String getGroupId() {
        return groupId;
    }

    public Date getDate() {
        return date;
    }

}

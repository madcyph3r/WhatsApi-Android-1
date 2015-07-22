package nl.giovanniterlingen.whatsapp;

import nl.giovanniterlingen.whatsapp.tools.BinHex;
import nl.giovanniterlingen.whatsapp.tools.CharsetUtils;

import java.util.List;
import java.util.Map;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ProtocolNode {

    private Map<String, String> attributes;
    private List<ProtocolNode> children;
    private byte[] data;
    private String tag;

    public ProtocolNode(String tag, Map<String, String> attributes,
                        List<ProtocolNode> nodes, byte[] data) {
        this.attributes = attributes;
        this.tag = tag;
        this.children = nodes;
        this.data = data;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public List<ProtocolNode> getChildren() {
        return children;
    }

    public void setChildren(List<ProtocolNode> children) {
        this.children = children;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAttribute(String key) {
        if (attributes != null && attributes.containsKey(key)) {
            return attributes.get(key);
        }
        return null;
    }

    public ProtocolNode getChild(int i) {
        if (children != null && children.size() >= i + 1) {
            return children.get(i);
        }
        return null;
    }

    public boolean hasChild(String tag) {
        for (ProtocolNode child : children) {
            if (child.getTag().equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public ProtocolNode getChild(String childTag) {
        for (ProtocolNode child : children) {
            if (child.getTag().equals(childTag)) {
                return child;
            }
        }
        return null;
    }

    public void refreshTimes() {
        // TODO Auto-generated method stub

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String lt = "<";
        String gt = ">";
        sb.append(lt + tag);
        if (attributes != null) {
            for (String key : attributes.keySet()) {
                sb.append(" " + key + "=\"" + attributes.get(key) + "\"");
            }
        }
        sb.append(gt);
        if (data != null && data.length > 0) {
            if (data.length < 1024) {
                sb.append(BinHex.bin2hex(data));
            } else {
                //raw data
                sb.append(data.length);
                sb.append(" byte data");
            }
        }
        if (children != null) {
            sb.append('\n');
            for (ProtocolNode child : children) {
                sb.append(child.toString());
            }
            sb.append('\n');
        }
        sb.append(lt + "/" + tag + gt);
        return sb.toString();
    }

    /**
     * @param String needle
     * @return boolean
     */
    public boolean nodeIdContains(String needle) {
        if (attributes != null && attributes.containsKey("id")) {
            return attributes.get("id").contains(needle);
        }
        return false;
    }

    public String getDataAsString() {

        return CharsetUtils.toString(data);
    }

}

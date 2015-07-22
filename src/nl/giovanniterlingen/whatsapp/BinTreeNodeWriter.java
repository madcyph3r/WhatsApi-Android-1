package nl.giovanniterlingen.whatsapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class BinTreeNodeWriter {

	private ByteArrayOutputStream output = new ByteArrayOutputStream();
	private KeyStream key;

	public byte[] startStream(String domain, String resource) throws IOException, InvalidKeyException, NoSuchAlgorithmException, EncodeException {
        Map<String,String> attributes = new LinkedHashMap<String, String>();
        ByteArrayOutputStream ret = new ByteArrayOutputStream();
        ret.write('W');
        ret.write('A');
        ret.write(writeInt8(1));
        ret.write(writeInt8(5));

        attributes.put("to",domain);
        attributes.put("resource",resource);
        writeListStart(attributes.size() * 2 + 1);

        output.write(0x01);
        writeAttributes(attributes);
        flushBuffer(true).writeTo(ret);

        return ret.toByteArray();
	}

    public void resetKey()
    {
        this.key = null;
    }
    
    public void setKey(KeyStream key)
    {
        this.key = key;
    }

    /**
     * @param ProtocolNode $node
     * @return string
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws EncodeException 
     */
    public byte[] write(ProtocolNode node, boolean encrypted) throws IOException, InvalidKeyException, NoSuchAlgorithmException, EncodeException
    {
        if (node == null) {
            output.write(0x00);
        } else {
            writeInternal(node);
        }

        return flushBuffer(encrypted).toByteArray();
    }

    /**
     * @param ProtocolNode $node
     * @throws IOException 
     */
    protected void writeInternal(ProtocolNode node) throws IOException
    {
        int len = 1;
        if (node.getAttributes() != null) {
            len += (node.getAttributes().size()) * 2;
        }
        if (node.getChildren() != null && node.getChildren().size() > 0) {
            len += 1;
        }
        if (node.getData() != null && node.getData().length > 0) {
            len += 1;
        }
        writeListStart(len);
        writeString(node.getTag());
        writeAttributes(node.getAttributes());
        if (node.getData() != null && node.getData().length > 0) {
            writeBytes(node.getData());
        }
        if (node.getChildren() != null) {
            writeListStart(node.getChildren().size());
            for (ProtocolNode child : node.getChildren()) {
                writeInternal(child);
            }
        }
    }

    protected ByteArrayOutputStream flushBuffer(boolean encrypted) throws EncodeException, IOException
    {
		byte[] data;
		int size;
		data = output.toByteArray();
		size = data.length;
		ByteArrayOutputStream ret = new ByteArrayOutputStream();
		if(key != null && encrypted) {
			byte[] bsize = { 0,0,0 }; 
			data = key.encode(output.toByteArray(), size, 0, size);
			int len = data.length;
			bsize[0] = (byte)((8 << 4) | ((len & 16711680) >> 16));
			bsize[1] = (byte)(((len & 65280) >> 8));
			bsize[2] = (byte)((len & 255) );
			ret.write(bsize);
		} else {
			ret.write(writeInt8(key != null ? (1 << 4) : 0));
			ret.write(writeInt16(size));
		}
        ret.write(data);
        output.reset();
        return ret;
    }

    protected void writeToken(int token)
    {
        if(token < 0xf5) {
            output.write((char)token);
        } else {
        	if (token <= 0x1f4) {
        		output.write(0xfe);
        		output.write((char)(token - 0xf5));
        	}
        }
    }

	protected void writeJid(String user, String server) throws IOException
    {
        output.write(0xfa);
        if (user != null && user.length() > 0) {
            writeString(user);
        } else {
            writeToken(0);
        }
        writeString(server);
    }

    protected byte writeInt8(int v)
    {
        return (byte)(v & 0xff);
    }

    protected byte[] writeInt16(int v)
    {
    	byte[] ret = new byte[] { (byte) ((v & 0xff00) >> 8),(byte) ((v & 0x00ff) >> 0) };

        return ret;
    }

    protected byte[] writeInt24(long v)
    {
    	byte[] ret = new byte[] { (byte) ((v & 0xff0000) >> 16),(byte) ((v & 0xff00) >> 8),(byte) ((v & 0x00ff) >> 0) };

        return ret;
    }

    protected void writeBytes(byte[] bytes) throws IOException
    {
        int len = bytes.length;
        if (len >= 0x100) {
            output.write(0xfd);
            output.write(writeInt24(len));
        } else {
        	output.write(0xfc);
        	output.write(writeInt8(len));
        }
        output.write(bytes);
    }

    protected void writeString(String tag) throws IOException
    {
    	Token t = TokenMap.tryGetToken(tag);
    	if( t != null ) {
    		if(t.isSubdictionary()) {
    			writeToken(236);
    		}
            writeToken(t.getToken());
        } else {
            int index = tag.indexOf('@');
            if (index >= 0) {
            	String[] split = tag.split("@");
            	String server = split[1];
            	String user = split[0];
            	writeJid(user,server);
            } else {
                writeBytes(tag.getBytes());
            }
        }
    }

    protected void writeAttributes(Map<String,String> attributes) throws IOException
    {
        if (attributes != null) {
            for (String key : attributes.keySet()) {
                writeString(key);
                writeString(attributes.get(key));
            }
        }
    }

    protected void writeListStart(int len)
    {
        if (len == 0) {
            output.write(0x00);
        } else {
        	if (len < 256) {
        		output.write(0xf8);
        		output.write(len);
        	} else {
        		output.write(0xf9);
        		output.write(len);
        	}
        }
    }

}

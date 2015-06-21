package nl.giovanniterlingen.whatsapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import android.util.Log;

import nl.giovanniterlingen.whatsapp.tools.BinHex;

public class BinTreeNodeReader {

	private KeyStream key;
	private byte[] input;

	public void resetKey() {
		this.key = null;
	}
	
	public void setKey(KeyStream key) {
		this.key = key;
	}

	/**
	 * 
	 * @param readData
	 * @return
	 * @throws IncompleteMessageException 
	 * @throws InvalidMessageException 
	 * @throws InvalidTokenException 
	 * @throws IOException 
	 * @throws DecodeException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public ProtocolNode nextTree(byte[] readData) throws IncompleteMessageException, InvalidMessageException, InvalidTokenException, IOException, InvalidKeyException, NoSuchAlgorithmException, DecodeException {
		if(readData != null) {
			input = readData;
		}
		
		int firstByte  = peekInt8(0);
		int stanzaFlag = (firstByte & 0xF0) >> 4;
		int stanzaSize = peekInt16(1) | ((firstByte & 0x0f) << 16);
		if(stanzaSize > input.length) {
			Log.i("INFO", "fb:"+firstByte+", sf="+stanzaFlag+", sz="+stanzaSize);
			if(input != null && input.length > 0) {
				Log.d("DEBUG", "Input:"+BinHex.bin2hex(input));
			}
			throw new IncompleteMessageException("incomplete message: "+stanzaSize+">"+input.length);
		}
	    readInt24();
	    if ((stanzaFlag & 8) != 0) {
	    	if (key != null) {
	    		try {
	    			byte[] decoded = key.decode(input, stanzaSize-4, 0, stanzaSize-4);
//	    			log.debug("Input decoded ="+BinHex.bin2hex(decoded));
	    			ByteArrayOutputStream s = new ByteArrayOutputStream();
	    			s.write(Arrays.copyOf(decoded, stanzaSize-4));
	    			s.write(Arrays.copyOfRange(input, stanzaSize, input.length));
	    			input = s.toByteArray();
	    			
	    		} catch (DecodeException e) {
	    			Log.i("INFO", "Decode failed");
					Log.d("DEBUG", "Input ="+BinHex.bin2hex(Arrays.copyOf(readData,stanzaSize+3)));
					throw e;
	    		}
	        } else {
	            throw new InvalidMessageException("Encountered encrypted message, missing key");
	        }
	    }
	    if (stanzaSize > 0) {
	        return nextTreeInternal();
	    }
		return null;
	}


	private ProtocolNode nextTreeInternal() throws InvalidTokenException, IOException {
        int token = readInt8();
        int size = readListSize(token);
        token = readInt8();
        if (token == 1) {
            Map<String, String> attributes = readAttributes(size);

            return new ProtocolNode("start", attributes, null, null);
        } 
        if (token == 2) {
            return null;
        }
        String tag = new String(readString(token));
        Map<String, String> attributes = readAttributes(size);
        if ((size % 2) == 1) {
            return new ProtocolNode(tag, attributes, null, null);
        }
        token = readInt8();
        if (isListTag(token)) {
            return new ProtocolNode(tag, attributes, readList(token), null);
        }

        return new ProtocolNode(tag, attributes, null, readString(token));
    }

	private LinkedList<ProtocolNode> readList(int token) throws InvalidTokenException, IOException {
        int size = readListSize(token);
        LinkedList<ProtocolNode> ret = new LinkedList<ProtocolNode>();
        for (int i = 0; i < size; i++) {
            ret.add(nextTreeInternal());
        }

        return ret;	
    }

	private boolean isListTag(int token) {
		return ((token == 248) || (token == 0) || (token == 249));	}

	private byte[] readString(int token) throws IOException, InvalidTokenException {
        ByteArrayOutputStream ret = new ByteArrayOutputStream();
        int size;
        if (token == -1) {
            throw new InvalidTokenException("BinTreeNodeReader->readString: Invalid token $token");
        }
        switch(token) {
        case 0:
        	break;
        case 0xfc:
            size = readInt8();
            ret.write(fillArray(size));
            break;
        case 0xfd:
            size = readInt24();
            ret.write(fillArray(size));
            break;
        case 0xfe:
            token = readInt8();
            ret.write(getToken(token + 0xf5));
            break;
        case 0xfa:
            byte[] user = readString(readInt8());
            byte[] server = readString(readInt8());
            if((user.length > 0) && (server.length > 0)) {
                ret.write(user);
                ret.write('@');
                ret.write(server);
            } else {
            	if (server.length > 0) {
            		ret.write(server);
            	}
            }
            break;
        case 0xff:
        	byte[] nibble = readNibble();
        	ret.write(nibble);
        	break;
        default:
            if(token > 2 && token < 0xf5) {
            	ret.write(getToken(token));
            }
            break;

        }

        return ret.toByteArray();
    }

	private byte[] readNibble() throws InvalidTokenException {
	      int b = readInt8();

	      int ignoreLastNibble = (b & 0x80);
	      int size = (b & 0x7f);
	      int nrOfNibbles = size * 2 - (ignoreLastNibble>0?1:0);

	      byte[] data = fillArray(size);
//	      log.debug(ProtocolNode.bin2hex(data));
	      String string = "";

	      for (int i = 0; i < nrOfNibbles; i++) {
	        b = data[(int) Math.floor(i / 2)];

	        int shift = 4 * (1 - i % 2);
	        int decimal = (b & (15 << shift)) >> shift;

	        switch (decimal) {
	          case 0:
	          case 1:
	          case 2:
	          case 3:
	          case 4:
	          case 5:
	          case 6:
	          case 7:
	          case 8:
	          case 9:
	        	  string += decimal;
	        	  break;
	          case 10:
	          case 11:
	        	  string += ((char)(decimal - 10 + 45));
	          break;
	          default:
	          throw new InvalidTokenException("Bad nibble: "+decimal);
	        }
	      }

//	      log.debug(string);
	      return string.getBytes();
	}

	private byte[] fillArray(int size) {
        byte[] ret = null;
        if (input.length >= size) {
            ret = Arrays.copyOfRange(input, 0, size);
            input = Arrays.copyOfRange(input, size, input.length);
        }

        return ret;
    }

	private byte[] getToken(int token) throws InvalidTokenException {
        String ret = "";
        Token t = null;
        boolean subdict = false;
        if(token >= 236) {
        	subdict = true;
        	token = readInt8();
        	t = TokenMap.getToken(token, subdict);
        } else {
        	t = TokenMap.getToken(token, subdict);
        }
        if (t == null) {
        	Log.i("INFO", "Token "+token+ "("+subdict+") not found!");
        	throw new InvalidTokenException("BinTreeNodeReader->getToken: Invalid token "+token);
        }
        ret = t.getName();
        return ret.getBytes();
    }

	private int readListSize(int token) throws InvalidTokenException {
        int size = 0;
        switch(token) {
        case 0xf8:
        case -8:
        	size = readInt8();
        	break;
        case 0xf9:
        case -7:
        	size = readInt16();
        	break;
        default:
        	throw new InvalidTokenException("BinTreeNodeReader->readListSize: Invalid token "+token);
        }
        return size;
    }

	private Map<String, String> readAttributes(int size) throws IOException, InvalidTokenException {
        Map<String, String> attributes = new LinkedHashMap<String, String>();
        int attribCount = (size - 2 + size % 2) / 2;
        for (int i = 0; i < attribCount; i++) {
            String key = new String(readString(readInt8()));
            String value = new String(readString(readInt8()));
            attributes.put(key, value);
        }

        return attributes;	
    }
	
	private int readInt24() {
		int ret = peekInt24(0);
		if (input.length >= 3) {
			input = Arrays.copyOfRange(input, 3, input.length+1);
		}
		
		return ret;
	}
	
	private int peekInt24(int offset) {
		int ret = 0;
		if (input.length >= (3 + offset)) {
			ret = (input[offset]&0xFF) << 16;
			ret |= (input[offset + 1]&0xFF) << 8;
			ret |= (input[offset + 2]&0xFF) << 0;
		}
		
		return ret;	
	}

	private int readInt8() {
		int ret = peekInt8(0);
		if (input.length >= 1) {
			input = Arrays.copyOfRange(input, 1, input.length+1);
		}
		
		return ret;
	}

	private int peekInt8(int offset) {
		if(input.length >= (1+offset)) {
			return input[offset] & 0xFF;
		}
		return 0;
	}

	private int readInt16() {
		int ret = peekInt16(0);
		if (input.length >= 2) {
			input = Arrays.copyOfRange(input, 2, input.length+1);
		}
		
		return ret;
	}

	private int peekInt16(int offset) {
		int ret = 0;
		if(input.length >= (2+offset)) {
			ret += (input[offset]&0xFF) << 8;
			ret += (input[offset+1]&0xFF) << 0;
		}
		return ret;
	}
}

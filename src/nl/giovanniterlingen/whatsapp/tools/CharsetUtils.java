package nl.giovanniterlingen.whatsapp.tools;

import java.io.UnsupportedEncodingException;

public class CharsetUtils {

	public static final String DEFAULT_CHARSET = "UTF-8";
	
	public static String toString(byte[] data) {
		try {
			return new String(data, DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			return new String(data);
		}
	}
	
	public static byte[] toBytes(String str) {
		try {
			return str.getBytes(DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			return str.getBytes();
		}
	}
	
}

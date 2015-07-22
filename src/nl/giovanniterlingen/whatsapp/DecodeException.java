package nl.giovanniterlingen.whatsapp;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class DecodeException extends Exception {

	public DecodeException(String message) {
		super(message);
	}

	public DecodeException(Throwable t) {
		super(t);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4649546029091323467L;

}

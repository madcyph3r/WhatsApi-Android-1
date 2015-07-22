package nl.giovanniterlingen.whatsapp;

public class InvalidTokenException extends Exception {

	/**
	 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
	 * ://github.com/WHAnonymous/Chat-API/}
	 * 
	 * @author Giovanni Terlingen
	 */
	public InvalidTokenException(String msg) {
		super(msg);
	}

	private static final long serialVersionUID = -3030083692560520998L;

}

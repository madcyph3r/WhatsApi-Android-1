package nl.giovanniterlingen.whatsapp;

public class InvalidMessageException extends Exception {

	/**
	 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
	 * ://github.com/WHAnonymous/Chat-API/}
	 * 
	 * @author Giovanni Terlingen
	 */
	public InvalidMessageException(String message) {
		super(message);
	}

}

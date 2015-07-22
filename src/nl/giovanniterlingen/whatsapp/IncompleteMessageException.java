package nl.giovanniterlingen.whatsapp;

public class IncompleteMessageException extends Exception {

	/**
	 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
	 * ://github.com/WHAnonymous/Chat-API/}
	 * 
	 * @author Giovanni Terlingen
	 */
	public IncompleteMessageException(String message) {
		super(message);
	}

}

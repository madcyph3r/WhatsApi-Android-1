package nl.giovanniterlingen.whatsapp;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class WhatsAppException extends Exception {

	public WhatsAppException(String message) {
		super(message);
	}

	public WhatsAppException(Throwable t) {
		super(t);
	}

	public WhatsAppException(String message, Exception e) {
		super(message,e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5818815745432441776L;

}

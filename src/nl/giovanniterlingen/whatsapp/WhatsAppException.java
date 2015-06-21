package nl.giovanniterlingen.whatsapp;


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

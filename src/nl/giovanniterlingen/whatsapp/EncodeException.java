package nl.giovanniterlingen.whatsapp;

public class EncodeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5567649814462441377L;
	
	public EncodeException(Throwable t) {
		super(t);
	}

	public EncodeException(String message) {
		super(message);
	}
}

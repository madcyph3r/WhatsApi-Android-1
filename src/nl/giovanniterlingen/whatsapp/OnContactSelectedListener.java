package nl.giovanniterlingen.whatsapp;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public interface OnContactSelectedListener {

	public void onContactNameSelected(long contactId);

	public void onContactNumberSelected(String contactNumber, String contactName);
}
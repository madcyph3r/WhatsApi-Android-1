package nl.giovanniterlingen.whatsapp;

public interface OnContactSelectedListener {

	public void onContactNameSelected(long contactId);

	public void onContactNumberSelected(String contactNumber, String contactName);
}
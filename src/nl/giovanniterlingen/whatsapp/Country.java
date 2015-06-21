package nl.giovanniterlingen.whatsapp;

public class Country {
	private String name;
	private String countryCode;
	private String mcc;
	private String iso3166;
	private String iso639;

	public Country(String[] entry) {
		name = entry[0];
		countryCode = entry[1];
		mcc = entry[2];
		iso3166 = entry[3];
		iso639 = entry[4];
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getMcc() {
		return mcc;
	}

	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	public String getIso3166() {
		return iso3166;
	}

	public void setIso3166(String iso3166) {
		this.iso3166 = iso3166;
	}

	public String getIso639() {
		return iso639;
	}

	public void setIso639(String iso639) {
		this.iso639 = iso639;
	}

	@Override
	public String toString() {
		return name+": "+countryCode+","+mcc+","+iso3166+","+iso639;
	}
}

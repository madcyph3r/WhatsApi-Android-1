package nl.giovanniterlingen.whatsapp;

public class Token {

	private int token;
	private String name;
	private boolean subdictionary;

	public Token(int token, String name, boolean subdictionatory) {
		this.token = token;
		this.name = name;
		this.subdictionary = subdictionatory;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSubdictionary() {
		return subdictionary;
	}

	public void setSubdictionary(boolean subdictionary) {
		this.subdictionary = subdictionary;
	}

	@Override
	public String toString() {
		return "Token "+token+" = " + name + "("+subdictionary+")";
	}
}

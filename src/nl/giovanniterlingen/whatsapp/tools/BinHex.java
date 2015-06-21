package nl.giovanniterlingen.whatsapp.tools;

public class BinHex {
	public static byte[] hex2bin(String challenge) {
		byte[] bytes = null;

		//hexString = removeSpaces(hexString);        
		if ( challenge.indexOf( " " ) < 0 )
		{
			bytes = new byte[challenge.length()/2];
			for ( int i=0; i<challenge.length(); i+=2 ) 
			{
				bytes[i/2]=(byte) Integer.parseInt( challenge.substring(i,i+2), 16);
			}
		}
		else
		{
			String[] parts = challenge.split(" ");
			bytes = new byte[parts.length];
			for ( int i=0; i<parts.length; i++ ) 
			{
				bytes[i] = (byte) Integer.parseInt( parts[i], 16);
			}
		}
		return bytes;
	}

	public static String bin2hex(byte[] bin) {
	    StringBuilder sb = new StringBuilder();
	    for(int i=0; i< bin.length ;i++)
	    {
	        sb.append(Integer.toString((bin[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
}

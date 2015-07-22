package nl.giovanniterlingen.whatsapp;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class WhatsMediaUploader {

	public static JSONObject pushFile(ProtocolNode uploadResponseNode,
			Map<String, Object> messageContainer, File mediaFile, String selfJID) throws NoSuchAlgorithmException, IOException {
		//get vars
		String url = uploadResponseNode.getChild("media").getAttribute("url");
		String filepath = mediaFile.getName();
		String to = (String) messageContainer.get("to");
//		String resume = uploadResponseNode.getChild("media").getAttribute("resume");
		return getPostString(filepath, url, mediaFile, to, selfJID, null);
	}

	private static JSONObject getPostString(String filepath, String url,
			File mediaFile, String to, String from, String resume) throws NoSuchAlgorithmException, IOException {
		int startFrom = 0;
		if(resume != null && resume.length() > 0) {
			startFrom = Integer.parseInt(resume);
		}
		
		URL u = new URL(url);
		
		String host = u.getHost();

		//filename to md5 digest
		String cryptoname = md5(filepath) + mediaFile.getName().substring(mediaFile.getName().lastIndexOf('.'));
		String boundary = "zzXXzzYYzzXXzzQQ";
		int contentlength = 0;

		String headers = "--" + boundary + "\r\n";
		headers += "Content-Disposition: form-data; name=\"to\"\r\n\r\n";
		headers += to + "\r\n";
		headers += "--" + boundary + "\r\n";
		headers += "Content-Disposition: form-data; name=\"from\"\r\n\r\n";
		headers += from + "\r\n";
		headers += "--" + boundary + "\r\n";
		headers += "Content-Disposition: form-data; name=\"file\"; filename=\"" + cryptoname + "\"\r\n";
		headers += "Content-Type: " + getMimeType(mediaFile) + "\r\n\r\n";

		String fBAOS = "\r\n--" + boundary + "--\r\n";

		contentlength += headers.length();
		contentlength += fBAOS.length();
		contentlength += mediaFile.length()-startFrom;

		String post = "POST " + url + "\r\n";
		post += "Content-Type: multipart/form-data; boundary=" + boundary + "\r\n";
		post += "Host: " + host + "\r\n";
		post += "User-Agent: WhatsApp/2.3.53 S40Version/14.26 Device/Nokia302\r\n";
		if(startFrom > 0) {
			post += "Content-Range: " + startFrom + "-" + mediaFile.length() + "\r\n";
		}
		post += "Content-Length: " + contentlength + "\r\n\r\n";

		return sendData(host, post, headers, filepath, mediaFile, fBAOS, startFrom);
	}

	private static JSONObject sendData(String host, String post, String head,
			String filepath, File mediaFile, String tail, int startFrom) throws IOException {
		SSLSocketFactory factory = 
				(SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket socket = 
				(SSLSocket)factory.createSocket(host, 443);

		OutputStream out = socket.getOutputStream();
		Log.d("DEBUG", "Writing post: "+post);
		out.write(post.getBytes());
		Log.d("DEBUG", "Writing head: "+head);
		out.write(head.getBytes());

		//write file data
		FileInputStream fileInputStream = new FileInputStream(mediaFile);

		if(startFrom > 0) {
			Log.d("DEBUG", "Skipping to "+startFrom);
			fileInputStream.skip(startFrom-1);
		}
		// Copy the contents of the file to the output stream
		byte[] buffer = new byte[1024];
		int count = 0;

		while ((count = fileInputStream.read(buffer)) >= 0) {
			out.write(buffer, 0, count);
		}                
		fileInputStream.close();
		out.write(tail.getBytes());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		String inputLine;
		StringBuilder data = new StringBuilder();
		while ((inputLine = in.readLine()) != null) 
			data.append(inputLine);
		in.close();
		out.close();
		socket.close();

		return getJson(data.toString());
	}

	private static JSONObject getJson(String data) {
		String[] parts = data.split("\r\n\r\n");

		if(parts.length > 1) {
			try {
				JSONObject json = new JSONObject(parts[1]);
				if(json != null) {
					return json;
				}
			} catch (JSONException e) {
				Log.w("WARNING", "Invalid json returned from upload: "+parts[1],e);
			}
		} else {
			Log.i("INFO", "No JSON body found in response"+data.toString());
			if(data.contains("{")) {
				String substring = data.substring(data.indexOf("{"));
				try {
					Log.d("DEBUG", "Trying with substring: "+substring);
					JSONObject json = new JSONObject(substring);
					if(json != null) {
						return json;
					}
				} catch (JSONException e) {
					Log.w("WARNING", "Invalid json returned from upload: "+substring,e);
				}
			}
		}
		return null;
	}

	private static String getMimeType(File mediaFile) throws IOException {
		/**
		 * TODO This needs improvement!
		 */
		String contentType = URLConnection.guessContentTypeFromName(mediaFile.getName());
//        if( contentType == null)
//        {
//            contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
//        }
        if(contentType == null) {
			if(mediaFile.getName().endsWith(".mp3")) {
				contentType = "audio/mpeg";
			}
		}
		Log.d("DEBUG", "Got content type "+contentType+" for: "+mediaFile.getPath());
		return contentType;
	}

	static String md5(String filepath) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		byte[] digest = md5.digest(filepath.getBytes());
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< digest.length ;i++)
        {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
	}

}

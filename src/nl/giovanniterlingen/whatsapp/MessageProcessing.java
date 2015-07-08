package nl.giovanniterlingen.whatsapp;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import nl.giovanniterlingen.whatsapp.MessageProcessor;
import nl.giovanniterlingen.whatsapp.ProtocolNode;
import nl.giovanniterlingen.whatsapp.message.Message;
import nl.giovanniterlingen.whatsapp.message.TextMessage;

public class MessageProcessing implements MessageProcessor {

	private Context mContext;
	 
	public void processMessage(ProtocolNode message) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
		.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		String from = message.getAttribute("from");
		if(message.getAttribute("type").equals("text")) {
			ProtocolNode body = message.getChild("body");
			String hex = new String(body.getData());
			String participant = message.getAttribute("participant");
			if(participant != null && !participant.isEmpty()) {
				//Group message
				Toast.makeText(this.mContext, "TOAST",
						Toast.LENGTH_SHORT).show();
				System.out.println(participant+"("+from+") ::: "+hex);
			} else {
				//Private message
				Toast.makeText(this.mContext, "TOAST",
						Toast.LENGTH_SHORT).show();
				System.out.println(from+" ::: "+hex);
			}
		}
		if(message.getAttribute("type").equals("media")) {
			ProtocolNode media = message.getChild("media");
			String type = media.getAttribute("type");
			if(type.equals("location")) {
				System.out.println(from+" ::: ("+media.getAttribute("longitude")+","+media.getAttribute("latitude")+")");
			} else if (type.equals("image")) {
				String caption = media.getAttribute("caption");
				if(caption == null)
					caption = "";
				String pathname = "preview-image-"+(new Date().getTime())+".jpg";
				System.out.println(from+" ::: "+caption+"(image): "+media.getAttribute("url"));
				byte[] preview = media.getData();
			} else if (type.equals("video")) {
				String caption = media.getAttribute("caption");
				if(caption == null)
					caption = "";
				String pathname = "preview-video-"+(new Date().getTime())+".jpg";
				System.out.println(from+" ::: "+caption+"(video): "+media.getAttribute("url"));
				byte[] preview = media.getData();
			} else {
				System.out.println(from+" ::: media/"+type);
			}
			
		}
	}


	public void processMessage(Message message) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
		.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		//TODO add all supported message types
		switch(message.getType()) {
		case TEXT:
			TextMessage msg = (TextMessage)message;
			if(msg.getGroupId() != null && !msg.getGroupId().isEmpty()) {
				//Group message
				Toast.makeText(this.mContext, msg.getText(),
						Toast.LENGTH_SHORT).show();
				System.out.println(msg.getDate()+" :: "+msg.getFrom()+"("+msg.getGroupId()+"): "+msg.getText());
			} else {
				//Private message
				Toast.makeText(this.mContext, msg.getText(),
						Toast.LENGTH_SHORT).show();
				System.out.println(msg.getDate()+" :: "+msg.getFrom()+" : "+msg.getText());
			}
			break;
		default:
			processMessage(message.getProtocolNode());
		}
	}

}
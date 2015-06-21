package nl.giovanniterlingen.whatsapp.example;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import nl.giovanniterlingen.whatsapp.MessageProcessor;
import nl.giovanniterlingen.whatsapp.ProtocolNode;
import nl.giovanniterlingen.whatsapp.message.Message;
import nl.giovanniterlingen.whatsapp.message.TextMessage;

import com.google.common.io.Files;

public class ExampleMessageProcessor implements MessageProcessor {

	public void processMessage(ProtocolNode message) {
		String from = message.getAttribute("from");
		if(message.getAttribute("type").equals("text")) {
			ProtocolNode body = message.getChild("body");
			String hex = new String(body.getData());
			String participant = message.getAttribute("participant");
			if(participant != null && !participant.isEmpty()) {
				//Group message
				System.out.println(participant+"("+from+") ::: "+hex);
			} else {
				//Private message
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
				writePreview(pathname, preview);
			} else if (type.equals("video")) {
				String caption = media.getAttribute("caption");
				if(caption == null)
					caption = "";
				String pathname = "preview-video-"+(new Date().getTime())+".jpg";
				System.out.println(from+" ::: "+caption+"(video): "+media.getAttribute("url"));
				byte[] preview = media.getData();
				writePreview(pathname, preview);
			} else {
				System.out.println(from+" ::: media/"+type);
			}
			
		}
	}

	private void writePreview(String pathname, byte[] preview) {
		File path = new File(pathname);
		try {
			Files.write(preview, path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Preview: "+path.getAbsolutePath());
	}

	public void processMessage(Message message) {
		//TODO add all supported message types
		switch(message.getType()) {
		case TEXT:
			TextMessage msg = (TextMessage)message;
			if(msg.getGroupId() != null && !msg.getGroupId().isEmpty()) {
				//Group message
				System.out.println(msg.getDate()+" :: "+msg.getFrom()+"("+msg.getGroupId()+"): "+msg.getText());
			} else {
				//Private message
				System.out.println(msg.getDate()+" :: "+msg.getFrom()+" : "+msg.getText());
			}
			break;
		default:
			processMessage(message.getProtocolNode());
		}
	}

}

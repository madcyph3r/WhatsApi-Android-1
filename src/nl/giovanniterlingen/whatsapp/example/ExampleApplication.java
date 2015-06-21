package nl.giovanniterlingen.whatsapp.example;

import java.io.Console;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nl.giovanniterlingen.whatsapp.EventManager;
import nl.giovanniterlingen.whatsapp.MessageProcessor;
import nl.giovanniterlingen.whatsapp.SyncType;
import nl.giovanniterlingen.whatsapp.WhatsApi;
import nl.giovanniterlingen.whatsapp.WhatsAppException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Example application
 * Launch with: java -cp target/dependency/*:target/whatsapi4j-1.0.0-SNAPSHOT.jar nl.giovanniterlingen.whatsapp.example.ExampleApplication 358401122333 'mypassword' 'mytestapplication' 'My Test Account'
 * @author kim
 *
 */
public class ExampleApplication {
	private enum WhatsAppCommand {
		send,request,register,status,text,sendText,image,sendImage,video,sendVideo,groups,sync,help,exit
	}

	public static boolean running = true;
	
	public static void main(String[] args) {
		boolean loggedIn = false;
		String filename = "exampleapplication.log";
		System.setProperty("org.slf4j.simpleLogger.logFile", filename);
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
		if(args.length != 4) {
			System.out.println("Usage: ExampleApplication <username> <password> <id> <nick>");
			System.exit(1);
		}
		Console cons = System.console();
		if(cons == null) {
			System.out.println("No console found. Aborting");
			System.exit(1);
		}

		String username = args[0];
		String password = args[1];
		if(password.length() == 0) {
			password = null;
		}
		String identity = args[2];
		if(identity.length() == 0) {
			identity = null;
		}
		String nickname = args[3];
		WhatsApi wa = null;
		try {
			wa = new WhatsApi(username, identity, nickname);

			EventManager eventManager = new ExampleEventManager();
			wa.setEventManager(eventManager );
			MessageProcessor mp = new ExampleMessageProcessor();
			wa.setNewMessageBind(mp);
			if(!wa.connect()) {
				System.out.println("Failed to connect to WhatsApp");
				System.exit(1);
			}
			if(password != null) {
				wa.loginWithPassword(password);
				loggedIn = true;
			}
			String cmd;
//			ExampleMessagePoller poller = new ExampleMessagePoller(wa);
//			poller.start();
			System.out.print("$ ");
			while(running && (cmd=cons.readLine()) != null) {
				try {
					WhatsAppCommand wac = WhatsAppCommand.valueOf(cmd);
					switch(wac) {
					case sync:
						if(loggedIn) {
							syncContacts(cons,wa);
						} else {
							System.out.println("Not logged in!");
						}
						break;
					case send:
					case text:
					case sendText:
						if(loggedIn) {
							sendTextMessage(cons,wa);
						} else {
							System.out.println("Not logged in!");
						}
						break;
					case image:
					case sendImage:
						if(loggedIn) {
							sendImageMessage(cons,wa);
						} else {
							System.out.println("Not logged in!");
						}
						break;
					case video:
					case sendVideo:
						if(loggedIn) {
							sendVideoMessage(cons,wa);
						} else {
							System.out.println("Not logged in!");
						}
						break;
					case status:
						if(loggedIn) {
							setStatus(cons,wa);
						} else {
							System.out.println("Not logged in!");
						}
						break;
					case request:
						if(!loggedIn) {
							sendRequest(wa);
							running = false;
						} else {
							System.out.println("Already logged in!");
						}
						break;
					case register:
						if(!loggedIn) {
							sendRegister(cons,wa);
							running = false;
						} else {
							System.out.println("Already logged in!");
						}
						break;
					case groups:
						if(loggedIn) {
							getGroups(cons,wa);
						} else {
							System.out.println("Not logged in!");
						}
						break;
					case exit:
						running = false;
						break;
					case help:
						System.out.println("Allowed commands: "+Arrays.toString(WhatsAppCommand.values()));
						break;
					default: 
						System.out.println("Unknown command: "+cmd);
					}
				} catch (IllegalArgumentException e) {
					if(cmd.length() > 0)
						System.out.println("Unknown command: "+cmd);
					e.printStackTrace();
				}
				System.out.print("$ ");
			}
//			poller.setRunning(false);
			System.out.println("Done! Logging out");
			wa.disconnect();
		} catch (Exception e) {
			System.out.println("Caught exception: "+e.getMessage());
			e.printStackTrace();
			if(wa != null) {
				wa.disconnect();
			}
			System.exit(1);
		}
	}

	private static void syncContacts(Console cons, WhatsApi wa) throws WhatsAppException {
		List<String> contacts = new LinkedList<String>();
		String contact;
		do {
			System.out.print("Add contact (finish with empty): ");
			contact = cons.readLine();
			contacts.add(contact);
		} while(contact.length() > 0);
		wa.sendSync(contacts, null, SyncType.DELTA_BACKGROUND, 0, true);
		System.out.println("Sync sent");
	}

	private static void getGroups(Console cons, WhatsApi wa) throws WhatsAppException {
		wa.getGroups();
	}

	private static void setStatus(Console cons, WhatsApi wa) throws WhatsAppException {
		System.out.print("Status: ");
		String status = cons.readLine();
		if(status == null || status.length() == 0) {
			return;
		}
		wa.sendStatusUpdate(status);
		System.out.println("Ok");
	}

	private static void sendRegister(Console cons, WhatsApi wa) throws JSONException, WhatsAppException {
		System.out.print("Code: ");
		String code = cons.readLine();
		if(code == null || code.length() == 0) {
			return;
		}
		JSONObject res = wa.codeRegister(code);
		System.out.println(res.toString(2));
	}

	private static void sendRequest(WhatsApi wa) throws WhatsAppException, JSONException, UnsupportedEncodingException {
		JSONObject resp = wa.codeRequest("sms", null, null);
		System.out.println("Registration sent: "+resp.toString(2));
	}

	private static void sendTextMessage(Console cons, WhatsApi wa) throws WhatsAppException {
		System.out.print("To: ");
		String to = cons.readLine();
		if(to == null || to.length() == 0) {
			return;
		}
		wa.sendMessageComposing(to);
		System.out.print("Message: ");
		String message = cons.readLine();
		wa.sendMessagePaused(to);
		if(message == null || message.length() == 0) {
			return;
		}
		String res = wa.sendMessage(to, message);
		System.out.println(res);
	}
	
	private static void sendImageMessage(Console cons, WhatsApi wa) throws WhatsAppException, URISyntaxException {
		System.out.print("To: ");
		String to = cons.readLine();
		if(to == null || to.length() == 0) {
			return;
		}
		System.out.print("Caption: ");
		String message = cons.readLine();
		if(message == null || message.length() == 0) {
			return;
		}
		File image = new File("exampleData/bananas.jpg");
		File preview = new File("exampleData/bananas-preview.jpg");
		JSONObject res = wa.sendMessageImage(to, image, preview, message);
		System.out.println(res);
	}
	
	private static void sendVideoMessage(Console cons, WhatsApi wa) throws WhatsAppException {
		System.out.print("To: ");
		String to = cons.readLine();
		if(to == null || to.length() == 0) {
			return;
		}
		System.out.print("Caption: ");
		String message = cons.readLine();
		if(message == null || message.length() == 0) {
			return;
		}
		File video = new File("exampleData/video.mp4");
		File preview = new File("exampleData/video-preview.jpg");
		JSONObject res = wa.sendMessageVideo(to, video,preview,message);
		System.out.println(res);
	}

}

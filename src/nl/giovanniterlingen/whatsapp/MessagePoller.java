package nl.giovanniterlingen.whatsapp;

import java.net.SocketTimeoutException;

import android.util.Log;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class MessagePoller extends Thread {
	private boolean running = true;
	private final WhatsApi wa;

	public MessagePoller(WhatsApi wa) {
		this.wa = wa;
	}

	@Override
	public void run() {
		Log.i("INFO", "Message poller starting");
		while(isRunning())
		try {
			wa.pollMessages();
		} catch (SocketTimeoutException e) {
		} catch (Exception e) {
			Log.e("ERROR", "Message poller caught exception: "+e.getMessage(), e);
		}
		Log.i("INFO", "Message poller finishing");
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}

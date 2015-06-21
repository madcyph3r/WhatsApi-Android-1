package nl.giovanniterlingen.whatsapp.example;

import java.net.SocketTimeoutException;

import nl.giovanniterlingen.whatsapp.WhatsApi;

class ExampleMessagePoller extends Thread {
	private boolean running = true;
	private final WhatsApi wa;

	public ExampleMessagePoller(WhatsApi wa) {
		this.wa = wa;
	}

	@Override
	public void run() {
		while(isRunning())
		try {
			wa.pollMessages();
		} catch (SocketTimeoutException e) {
		} catch (Exception e) {
			System.err.println("Message poller caught exception: "+e.getMessage());
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
package nl.giovanniterlingen.whatsapp;

import java.io.File;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class MediaInfo {
	private File mediaFile;
	private File previewFile;
	private String caption;
	
	public File getMediaFile() {
		return mediaFile;
	}
	public void setMediaFile(File mediaFile) {
		this.mediaFile = mediaFile;
	}
	public File getPreviewFile() {
		return previewFile;
	}
	public void setPreviewFile(File previewFile) {
		this.previewFile = previewFile;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
}

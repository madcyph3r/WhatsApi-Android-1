package nl.giovanniterlingen.whatsapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class BitmapHelper {

	public static File createIcon(String path, Context context)
			throws IOException {

		Bitmap image = BitmapFactory.decodeFile(path);
		Bitmap resized;
		int width = image.getWidth();
		int height = image.getHeight();
		float scale;

		// magic happens here
		float scaleHeight = (float) height / (float) 100;
		float scaleWidth = (float) width / (float) 100;
		if (scaleWidth < scaleHeight)
			scale = scaleHeight;
		else
			scale = scaleWidth;

		// resize the image here
		resized = Bitmap.createScaledBitmap(image, (int) (width / scale),
				(int) (height / scale), true);

		// create a file to write bitmap data
		File file = new File(context.getCacheDir() + "/preview.jpg");
		file.createNewFile();

		// Convert bitmap to byte array
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		resized.compress(CompressFormat.JPEG, 100, bos);
		byte[] bitmapdata = bos.toByteArray();

		// write the bytes in file
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bitmapdata);
		fos.flush();
		fos.close();

		return file;
	}
	
	public static Bitmap getRoundedBitmap(Bitmap bitmap) {
	    final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
	    final Canvas canvas = new Canvas(output);

	    final int color = Color.RED;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	    final RectF rectF = new RectF(rect);

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    canvas.drawOval(rectF, paint);

	    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);

	    bitmap.recycle();

	    return output;
	  }
}
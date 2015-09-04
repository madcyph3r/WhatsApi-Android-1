package nl.giovanniterlingen.whatsapp;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ContactsAdapter extends CursorAdapter {

	public ContactsAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, 0);
	}

	// The newView method is used to inflate a new view and return it,
	// you don't bind any data to the view at this point.
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(
				R.layout.list_item_contacts, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		TextView name = (TextView) view.findViewById(R.id.display_name);
		ImageView image = (ImageView) view.findViewById(R.id.account_photo);
		String contact = cursor.getString(cursor
				.getColumnIndexOrThrow("number"));
		// Populate fields with extracted properties
		name.setText(cursor.getString(cursor
				.getColumnIndexOrThrow("name")));
		
		File file = new File(context.getFilesDir().getParent() + File.separator
				+ "Avatars" + File.separator + contact + ".jpg");

		if (!file.exists()) {

			Intent i1 = new Intent();
			i1.setAction(MessageService.ACTION_GET_AVATAR);
			i1.putExtra("to", contact);
			context.sendBroadcast(i1);

		}

		if (file.exists()) {

			Bitmap avatar = BitmapHelper.getRoundedBitmap(BitmapFactory
					.decodeFile(file.getAbsolutePath()));
			image.setImageBitmap(avatar);

		} else {
			image.setImageDrawable(context.getResources().getDrawable(
					R.drawable.contact_photo_sample));
		}
	}
}

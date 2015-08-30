package nl.giovanniterlingen.whatsapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class ContactsDatabaseHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database
	// version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "contactsstore.db";

	public ContactsDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE contacts (_id INTEGER PRIMARY KEY AUTOINCREMENT, name, number TEXT);");
	}
	
	public static Cursor getContacts(SQLiteDatabase db) {
		// Select All Query
		String selectQuery = "SELECT _id, name, number FROM contacts ORDER BY name ASC";
		Cursor cursor = db.rawQuery(selectQuery, null);

		return cursor;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy
		// is
		// to simply to discard the data and start over
		onCreate(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}
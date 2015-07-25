package nl.giovanniterlingen.whatsapp;

import android.provider.BaseColumns;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public final class DatabaseContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DatabaseContract() {}

    /* Inner class that defines the table contents */
    public static abstract class DbEntries implements BaseColumns {
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_FROM = "`from`";
        public static final String COLUMN_NAME_TO = "`to`";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TIME = "t";
        public static final String COLUMN_NAME_NULLABLE = null;
    }
}
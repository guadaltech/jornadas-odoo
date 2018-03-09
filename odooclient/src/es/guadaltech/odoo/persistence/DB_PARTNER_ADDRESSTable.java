package es.guadaltech.odoo.persistence;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.guadaltech.odoo.misc.Constants;

public class DB_PARTNER_ADDRESSTable {

	public static final String TABLE_NAME = "partneraddress";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_IDHIDDEN = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_FUNCTION = "function";
	public static final String COLUMN_PARTNER_ID = "partner_id";
	public static final String COLUMN_STREET = "street";
	public static final String COLUMN_STREET_2 = "street2";
	public static final String COLUMN_CITY = "city";
	public static final String COLUMN_MOBILE = "mobile";
	public static final String COLUMN_EMAIL = "email";
	public static final String COLUMN_WRITE_DATE = "write_date";
	// TODO foreign keys
	public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
			+ COLUMN_IDHIDDEN + " integer primary key autoincrement," + COLUMN_ID + " integer," + COLUMN_NAME
			+ " text," + COLUMN_FUNCTION + " text," + COLUMN_PARTNER_ID + " integer," + COLUMN_STREET
			+ " text," + COLUMN_STREET_2 + " text," + COLUMN_CITY + " text," + COLUMN_MOBILE + " text,"
			+ COLUMN_EMAIL + " text," + COLUMN_WRITE_DATE + " integer);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
		Log.i(Constants.TAG, "Creating database " + TABLE_NAME);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(Constants.TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(database);
	}

}

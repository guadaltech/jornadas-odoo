package es.guadaltech.odoo.persistence;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.guadaltech.odoo.misc.Constants;

public class DB_PARTNERTable {

	public static final String TABLE_NAME = "partner";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_IDHIDDEN = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_FUNCTION = "function";
	public static final String COLUMN_CUSTOMER = "customer";
	public static final String COLUMN_SUPPLIER = "supplier";
	public static final String COLUMN_PHONE = "phone";
	public static final String COLUMN_MOBILE = "mobile";
	public static final String COLUMN_REFERENCE = "ref";
	public static final String COLUMN_STANDARD_PRICE = "standard_price";
	public static final String COLUMN_WRITE_DATE = "write_date";
	public static final String COLUMN_CITY = "city";
	public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
			+ COLUMN_IDHIDDEN + " integer primary key autoincrement," + COLUMN_ID + " integer unique,"
			+ COLUMN_NAME + " text," + COLUMN_MOBILE + " text," + COLUMN_PHONE + " text," + COLUMN_FUNCTION + " text," + COLUMN_REFERENCE + " text,"
			+ COLUMN_CUSTOMER + " integer," + COLUMN_SUPPLIER + " integer," + COLUMN_CITY + " text,"
			+ COLUMN_WRITE_DATE + " integer);";

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

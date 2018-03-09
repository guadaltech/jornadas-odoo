package es.guadaltech.odoo.persistence;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.guadaltech.odoo.misc.Constants;

public class DB_ORDERTable {
	public static final String TABLE_NAME = "orders";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_IDHIDDEN = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DATE_ORDER = "date_order";
	public static final String COLUMN_PARTNER_ID = "partner_id";
	public static final String COLUMN_SHOP_ID = "shop_id";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_AMOUNT = "amount_total";
	public static final String COLUMN_STATE = "state";
	public static final String COLUMN_WRITE_DATE = "write_date";
	// TODO foreign keys
	public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
			+ COLUMN_IDHIDDEN + " integer primary key autoincrement," + COLUMN_ID + " integer," + COLUMN_NAME
			+ " text," + COLUMN_DATE_ORDER + " integer," + COLUMN_PARTNER_ID + " integer," + COLUMN_SHOP_ID
			+ " integer," + COLUMN_USER_ID + " integer," + COLUMN_AMOUNT + " real," + COLUMN_STATE + " text,"
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

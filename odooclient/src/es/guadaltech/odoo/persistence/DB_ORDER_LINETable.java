package es.guadaltech.odoo.persistence;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.guadaltech.odoo.misc.Constants;

public class DB_ORDER_LINETable {
	public static final String TABLE_NAME = "orderline";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_IDHIDDEN = "_id";
	public static final String COLUMN_PRODUCT_ID = "product_id";
	public static final String COLUMN_ORDER_ID = "order_id";
	public static final String COLUMN_DISCOUNT = "discount";
	public static final String COLUMN_QTY = "qty";
	public static final String COLUMN_PRICE_UNIT = "price_unit";
	public static final String COLUMN_NOTICE = "notice";
	public static final String COLUMN_WRITE_DATE = "write_date";
	// TODO foreign keys
	public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
			+ COLUMN_IDHIDDEN + " integer primary key autoincrement," + COLUMN_ID + " integer,"
			+ COLUMN_DISCOUNT + " real," + COLUMN_QTY + " real," + COLUMN_PRICE_UNIT + " real,"
			+ COLUMN_NOTICE + " text," + COLUMN_WRITE_DATE + " integer," + COLUMN_PRODUCT_ID + " integer,"
			+ COLUMN_ORDER_ID + " integer);";

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

package es.guadaltech.odoo.persistence;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.guadaltech.odoo.misc.Constants;

public class DB_PRODUCTable {

	public static final String TABLE_NAME = "product";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_IDHIDDEN = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_BARCODE = "ean13";
	public static final String COLUMN_DEFAULT_CODE = "reference";
	public static final String COLUMN_CANBESOLD = "sale_ok";
	public static final String COLUMN_CANBEPURCHASED = "purchase_ok";
	public static final String COLUMN_CANBEEXPENSE = "hr_expense_ok";
	public static final String COLUMN_LIST_PRICE = "list_price";
	public static final String COLUMN_STANDARD_PRICE = "standard_price";
	public static final String COLUMN_QTY_AVA = "qty_available";
	public static final String COLUMN_VIRTUAL_AVA = "virtual_available";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_DESCRIPTION_SALE = "description_sale";
	public static final String COLUMN_PRODUCT_IMAGE_SMALL = "product_image_small";
	public static final String COLUMN_WRITE_DATE = "write_date";
	public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
			+ COLUMN_IDHIDDEN + " integer primary key autoincrement," + COLUMN_ID + " integer unique,"
			+ COLUMN_NAME + " text," + COLUMN_BARCODE + " integer," + COLUMN_DEFAULT_CODE + " text,"
			+ COLUMN_CANBESOLD + " integer," + COLUMN_CANBEPURCHASED + " integer," + COLUMN_CANBEEXPENSE
			+ " integer," + COLUMN_LIST_PRICE + " real," + COLUMN_STANDARD_PRICE + " real," + COLUMN_QTY_AVA
			+ " real," + COLUMN_VIRTUAL_AVA + " real," + COLUMN_DESCRIPTION + " text,"
			+ COLUMN_DESCRIPTION_SALE + " text," + COLUMN_PRODUCT_IMAGE_SMALL + " text," + COLUMN_WRITE_DATE
			+ " integer);";

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

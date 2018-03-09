package es.guadaltech.odoo.persistence;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.guadaltech.odoo.misc.Constants;

public class DB_COMPANY_PARAMSTable {
	
	public static final String TABLE_NAME = "companyparams";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_IDOERP = "idorep";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TYPE = "type";
	
	public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
			+ COLUMN_ID + " integer primary key autoincrement," + COLUMN_IDOERP + " integer,"
			+ COLUMN_NAME + " text unique," + COLUMN_TYPE + " text);";
	
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

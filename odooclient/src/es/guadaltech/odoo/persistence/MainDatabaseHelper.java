	package es.guadaltech.odoo.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MainDatabaseHelper extends SQLiteOpenHelper {

	private static MainDatabaseHelper mInstance = null;

	public static final String DATABASE_NAME = "persistence.db";
	public static final int DATABASE_VERSION = 1;

	public static MainDatabaseHelper getInstance(Context context) {

		if (mInstance == null) {
			mInstance = new MainDatabaseHelper(context.getApplicationContext());
		}
		return mInstance;
	}
	
	public static void destroyInstance() 
	{
		mInstance = null;
	}

	private MainDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		System.out.println("main database helper created, private constructor");
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		DB_ORDERTable.onCreate(database);
		DB_ORDER_LINETable.onCreate(database);
		DB_PRODUCTable.onCreate(database);
		DB_PARTNERTable.onCreate(database);
		DB_PARTNER_ADDRESSTable.onCreate(database);
		DB_COMPANY_PARAMSTable.onCreate(database);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		DB_ORDERTable.onUpgrade(database, oldVersion, newVersion);
		DB_ORDER_LINETable.onUpgrade(database, oldVersion, newVersion);
		DB_PRODUCTable.onUpgrade(database, oldVersion, newVersion);
		DB_PARTNERTable.onUpgrade(database, oldVersion, newVersion);
		DB_PARTNER_ADDRESSTable.onUpgrade(database, oldVersion, newVersion);
		DB_COMPANY_PARAMSTable.onUpgrade(database, oldVersion, newVersion);
	}	
	
	
}

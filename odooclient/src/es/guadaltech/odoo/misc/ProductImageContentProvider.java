package es.guadaltech.odoo.misc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.Settings.System;
import android.util.Log;
import es.guadaltech.odoo.persistence.DB_PRODUCTable;
import es.guadaltech.odoo.persistence.MainDatabaseHelper;

public class ProductImageContentProvider extends ContentProvider {
	static final String TAG = "ProductImageContentProvider";

	private MainDatabaseHelper mOpenHelper;
	private SQLiteDatabase db;

	static final String AUTHORITY = "content://es.guadaltech.openerp.provider";
	public static final Uri CONTENT_URI = Uri.parse(AUTHORITY);
	static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.es.guadaltech.openerp.providers.productimage";
	static final String MULTIPLE_RECORDS_MIME_TYPE = "vnd.android.cursor.dir/vnd.es.guadaltech.openerp.providers.productimage";

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate");

		mOpenHelper = MainDatabaseHelper.getInstance(getContext());

		return true;
	}

	@Override
	public String getType(Uri uri) {
		String ret = getContext().getContentResolver().getType(System.CONTENT_URI);
		Log.d(TAG, "getType returning: " + ret);
		return ret;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert uri: " + uri.toString());
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Log.d(TAG, "update uri: " + uri.toString());
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "delete uri: " + uri.toString());
		return 0;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Log.d(TAG, "query with uri: " + uri.toString());

		db = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

		builder.setTables(DB_PRODUCTable.TABLE_NAME);

		// limit query to one row at most
		builder.appendWhere(DB_PRODUCTable.COLUMN_IDHIDDEN + " = " + uri.getLastPathSegment());

		Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

}
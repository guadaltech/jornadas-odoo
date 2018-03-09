package es.guadaltech.odoo.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.model.CompanyParams;
import es.guadaltech.odoo.persistence.DB_COMPANY_PARAMSTable;
import es.guadaltech.odoo.persistence.MainDatabaseHelper;

public class CompanyParamsDAO {
	public static final int MAX_RESULTS = 30;
	public static final boolean D = true; // debug

	public Future<List<CompanyParams>> getAll(final Context context) {

		Callable<List<CompanyParams>> c = new Callable<List<CompanyParams>>() {

			@Override
			public List<CompanyParams> call() {

				if (D)
					Log.d(Constants.TAG, "Calling getAll() on CompanyParam");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<CompanyParams> items = new ArrayList<CompanyParams>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = db.query(DB_COMPANY_PARAMSTable.TABLE_NAME, null, null, null, null, null,
						null, null);

				while (cursor.moveToNext()) {
					items.add(buildCompanyParams(cursor));
				}
				if (D)
					Log.d(Constants.TAG, "CompanyParams getAll() returned " + items.size());
				return items;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public void delete(final Context context, final long idToDelete) {

		Log.i(Constants.TAG, "onDeleteItem single" + idToDelete);
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				long idDeleted = -1;

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				idDeleted = db.delete(DB_COMPANY_PARAMSTable.TABLE_NAME, DB_COMPANY_PARAMSTable.COLUMN_ID
						+ " = " + idToDelete, null);

				Log.i(Constants.TAG, "ITEM DELETED, id " + idDeleted);
			}
		});
		t.start();
	}

	public Future<Long> insert(final Context context, final CompanyParams cp) {

		Callable<Long> c = new Callable<Long>() {

			@Override
			public Long call() throws Exception {

				if (D)
					Log.d(Constants.TAG, "Calling insert() on CompanyParams");

				long id = -1;

				ContentValues cv = new ContentValues();
				cv.put(DB_COMPANY_PARAMSTable.COLUMN_IDOERP, cp.getIdOERP());
				cv.put(DB_COMPANY_PARAMSTable.COLUMN_NAME, cp.getName());
				cv.put(DB_COMPANY_PARAMSTable.COLUMN_TYPE, cp.getType());

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				id = db.insertWithOnConflict(DB_COMPANY_PARAMSTable.TABLE_NAME, "<empty>", cv,
						SQLiteDatabase.CONFLICT_IGNORE);

				if (D)
					Log.d(Constants.TAG, "CompanyParam insert returned " + id);
				return id;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	private static CompanyParams buildCompanyParams(Cursor cursor) {

		CompanyParams cp = new CompanyParams();
		cp.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_COMPANY_PARAMSTable.COLUMN_ID)));
		cp.setIdOERP(cursor.getInt(cursor.getColumnIndexOrThrow(DB_COMPANY_PARAMSTable.COLUMN_IDOERP)));
		cp.setName(cursor.getString(cursor.getColumnIndexOrThrow(DB_COMPANY_PARAMSTable.COLUMN_NAME)));
		cp.setType(cursor.getString(cursor.getColumnIndexOrThrow(DB_COMPANY_PARAMSTable.COLUMN_TYPE)));

		return cp;
	}

}

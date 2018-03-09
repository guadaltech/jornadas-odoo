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
import es.guadaltech.odoo.model.PartnerAddress;
import es.guadaltech.odoo.persistence.DB_PARTNER_ADDRESSTable;
import es.guadaltech.odoo.persistence.MainDatabaseHelper;

public class PartnerAddressDAO {
	public static final Integer MAX_RESULTS = 30;

	public Future<List<PartnerAddress>> getAll(final Context context, final int pagination) {

		Callable<List<PartnerAddress>> c = new Callable<List<PartnerAddress>>() {

			@Override
			public List<PartnerAddress> call() {

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<PartnerAddress> items = new ArrayList<PartnerAddress>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				// No parameters, load MAX_RESULTS first items
				if (pagination < 1) {
					cursor = db.query(DB_PARTNER_ADDRESSTable.TABLE_NAME, null, null, null, null, null,
							DB_PARTNER_ADDRESSTable.COLUMN_WRITE_DATE + " DESC", MAX_RESULTS.toString());
				} else {
					int firstLimit = MAX_RESULTS * pagination;
					int lastLimit = firstLimit + MAX_RESULTS;
					cursor = db
							.query(DB_PARTNER_ADDRESSTable.TABLE_NAME, null, null, null, null, null,
									DB_PARTNER_ADDRESSTable.COLUMN_WRITE_DATE + " DESC", firstLimit + ","
											+ lastLimit);
				}

				while (cursor.moveToNext()) {
					PartnerAddress p = new PartnerAddress();
					p.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_ID)));
					p.setPartner_id(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_PARTNER_ID)));
					p.setFunction(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_FUNCTION)));

					p.setStreet(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_STREET)));

					p.setStreet2(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_STREET_2)));
					p.setCity(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_CITY)));
					p.setName(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_NAME)));
					p.setMobile(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_MOBILE)));
					p.setEmail(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_EMAIL)));

					p.setWriteDate(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_WRITE_DATE)));

					items.add(p);
				}
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
				idDeleted = db.delete(DB_PARTNER_ADDRESSTable.TABLE_NAME, DB_PARTNER_ADDRESSTable.COLUMN_ID
						+ " = " + idToDelete, null);

				Log.i(Constants.TAG, "ITEM DELETED, id " + idDeleted);
			}
		});
		t.start();
	}

	public Future<Long> insert(final Context context, final PartnerAddress p) {

		Callable<Long> c = new Callable<Long>() {

			@Override
			public Long call() throws Exception {
				long id = -1;

				ContentValues cv = new ContentValues();
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_ID, p.getId());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_NAME, p.getName());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_PARTNER_ID, p.getPartner_id());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_STREET, p.getStreet());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_FUNCTION, p.getFunction());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_STREET_2, p.getStreet2());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_CITY, p.getCity());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_MOBILE, p.getMobile());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_EMAIL, p.getEmail());
				cv.put(DB_PARTNER_ADDRESSTable.COLUMN_WRITE_DATE, p.getWriteDate());

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				id = db.insert(DB_PARTNER_ADDRESSTable.TABLE_NAME, "false", cv);

				return id;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

}

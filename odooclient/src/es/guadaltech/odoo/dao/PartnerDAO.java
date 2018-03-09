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
import es.guadaltech.odoo.model.Partner;
import es.guadaltech.odoo.model.PartnerAddress;
import es.guadaltech.odoo.persistence.DB_PARTNERTable;
import es.guadaltech.odoo.persistence.DB_PARTNER_ADDRESSTable;
import es.guadaltech.odoo.persistence.MainDatabaseHelper;

public class PartnerDAO {
	public static final boolean D = true; // debug
	public static final Integer MAX_RESULTS = 30;

	public Future<List<Partner>> getAll(final Context context, final int pagination) {

		Callable<List<Partner>> c = new Callable<List<Partner>>() {

			@Override
			public List<Partner> call() {

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Partner> items = new ArrayList<Partner>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				// No parameters, load MAX_RESULTS first items
				if (pagination < 1) {
					cursor = db.query(DB_PARTNERTable.TABLE_NAME, null, null, null, null, null,
							DB_PARTNERTable.COLUMN_WRITE_DATE + " DESC", MAX_RESULTS.toString());
				} else {
					int firstLimit = MAX_RESULTS * pagination;
					int lastLimit = firstLimit + MAX_RESULTS;
					cursor = db.query(DB_PARTNERTable.TABLE_NAME, null, null, null, null, null,
							DB_PARTNERTable.COLUMN_WRITE_DATE + " DESC", firstLimit + "," + lastLimit);
				}

				while (cursor.moveToNext()) {
					Partner p = new Partner();
					p.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_ID)));
					p.setName(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_NAME)));
					p.setMobile(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_PHONE)));
					p.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_MOBILE)));
					p.setFunction(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_FUNCTION)));
					int intCustomer = (cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_CUSTOMER)));
					// casting int 2 boolean
					p.setCustomer(intCustomer != 0);
					int intSupplier = cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_SUPPLIER));
					p.setSupplier(intSupplier != 0);
					p.setReference(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_REFERENCE)));
					p.setWriteDate(cursor.getLong(cursor
							.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_WRITE_DATE)));
					p.setCity(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_CITY)));

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
				idDeleted = db.delete(DB_PARTNERTable.TABLE_NAME, DB_PARTNERTable.COLUMN_ID + " = "
						+ idToDelete, null);

				Log.i(Constants.TAG, "Partner DELETED, id " + idDeleted);
			}
		});
		t.start();
	}

	public Future<Long> insert(final Context context, final Partner p) {

		Callable<Long> c = new Callable<Long>() {

			@Override
			public Long call() throws Exception {
				long id = -1;

				ContentValues cv = new ContentValues();
				cv.put(DB_PARTNERTable.COLUMN_ID, p.getId());
				cv.put(DB_PARTNERTable.COLUMN_NAME, p.getName());
				cv.put(DB_PARTNERTable.COLUMN_MOBILE, p.getMobile());
				cv.put(DB_PARTNERTable.COLUMN_PHONE, p.getPhone());
				cv.put(DB_PARTNERTable.COLUMN_CUSTOMER, p.isCustomer());
				cv.put(DB_PARTNERTable.COLUMN_SUPPLIER, p.isSupplier());
				cv.put(DB_PARTNERTable.COLUMN_FUNCTION, p.getFunction());
				cv.put(DB_PARTNERTable.COLUMN_REFERENCE, p.getReference());
				cv.put(DB_PARTNERTable.COLUMN_WRITE_DATE, p.getWriteDate());
				cv.put(DB_PARTNERTable.COLUMN_CITY, p.getCity());

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				id = db.insertWithOnConflict(DB_PARTNERTable.TABLE_NAME, "<empty>", cv,
						SQLiteDatabase.CONFLICT_REPLACE);

				return id;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<Partner> searchByID(final Context context, final String id) {

		Callable<Partner> c = new Callable<Partner>() {

			@Override
			public Partner call() {

				if (D)
					Log.d(Constants.TAG, "Calling searchByID() on Partner");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Partner> items = new ArrayList<Partner>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_PARTNERTable.TABLE_NAME, null, DB_PARTNERTable.COLUMN_ID + "=?",
						new String[] { id }, null, null, null);
				// No parameters, load MAX_RESULTS first items

				while (cursor.moveToNext()) {
					Partner p = buildPartner(cursor);
					Cursor cursorAdrress = db.query(DB_PARTNER_ADDRESSTable.TABLE_NAME, null,
							DB_PARTNER_ADDRESSTable.COLUMN_ID + "=?",
							new String[] { p.getId().toString() }, null, null, null);
					List<PartnerAddress> addresses = new ArrayList<PartnerAddress>();
					while (cursorAdrress.moveToNext()) {
						addresses.add(buildPartnerAddress(cursorAdrress));
					}
					p.setAdresses(addresses);
					items.add(p);
				}
				if (D)
					Log.d(Constants.TAG, "PartnerDAO searchByID() returned " + items.size());
				return items.get(0);
			}

		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<List<Partner>> searchByName(final Context context, final String name) {

		Callable<List<Partner>> c = new Callable<List<Partner>>() {

			@Override
			public List<Partner> call() {

				if (D)
					Log.d(Constants.TAG, "Calling searchByID() on Partner");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Partner> items = new ArrayList<Partner>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_PARTNERTable.TABLE_NAME, null, DB_PARTNERTable.COLUMN_NAME + " LIKE ?",
						new String[] { "%" + name + "%" }, null, null, null);
				// No parameters, load MAX_RESULTS first items

				while (cursor.moveToNext()) {
					Partner p = buildPartner(cursor);
					Cursor cursorAdrress = db.query(DB_PARTNER_ADDRESSTable.TABLE_NAME, null,
							DB_PARTNER_ADDRESSTable.COLUMN_ID + "=?",
							new String[] { p.getId().toString() }, null, null, null);
					List<PartnerAddress> addresses = new ArrayList<PartnerAddress>();
					while (cursorAdrress.moveToNext()) {
						addresses.add(buildPartnerAddress(cursorAdrress));
					}
					p.setAdresses(addresses);
					items.add(p);
				}
				if (D)
					Log.d(Constants.TAG, "PartnerDAO searchByName() returned " + items.size());
				return items;
			}

		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	private PartnerAddress buildPartnerAddress(Cursor cursorAdrress) {
		PartnerAddress pa = new PartnerAddress();
		pa.setCity(cursorAdrress.getString(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_CITY)));
		pa.setEmail(cursorAdrress.getString(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_EMAIL)));
		pa.setFunction(cursorAdrress.getString(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_FUNCTION)));
		pa.setId(cursorAdrress.getInt(cursorAdrress.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_ID)));
		pa.setMobile(cursorAdrress.getString(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_MOBILE)));
		pa.setName(cursorAdrress.getString(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_NAME)));
		pa.setPartner_id(cursorAdrress.getInt(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_PARTNER_ID)));
		pa.setStreet(cursorAdrress.getString(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_STREET)));
		pa.setStreet2(cursorAdrress.getString(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_STREET_2)));
		pa.setWriteDate(cursorAdrress.getInt(cursorAdrress
				.getColumnIndexOrThrow(DB_PARTNER_ADDRESSTable.COLUMN_WRITE_DATE)));
		return pa;
	}

	private Partner buildPartner(Cursor cursor) {
		Partner p = new Partner();
		p.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_ID)));
		p.setCustomer(1 == (cursor.getInt(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_CUSTOMER))));
		p.setSupplier(1 == (cursor.getInt(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_SUPPLIER))));
		p.setName(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_NAME)));
		p.setCity(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_CITY)));
		p.setReference(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_REFERENCE)));
		p.setName(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_NAME)));
		p.setFunction(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_FUNCTION)));
		p.setMobile(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_MOBILE)));
		p.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_PHONE)));
		p.setWriteDate(cursor.getLong(cursor.getColumnIndexOrThrow(DB_PARTNERTable.COLUMN_WRITE_DATE)));

		return p;
	};

}

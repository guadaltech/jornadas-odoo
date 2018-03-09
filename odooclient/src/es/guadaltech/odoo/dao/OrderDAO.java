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
import es.guadaltech.odoo.model.Order;
import es.guadaltech.odoo.persistence.DB_ORDERTable;
import es.guadaltech.odoo.persistence.MainDatabaseHelper;

public class OrderDAO {
	public static final Integer MAX_RESULTS = 30;

	public Future<List<Order>> getAll(final Context context, final int pagination) {

		Callable<List<Order>> c = new Callable<List<Order>>() {

			@Override
			public List<Order> call() {

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Order> items = new ArrayList<Order>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				// No parameters, load MAX_RESULTS first items
				if (pagination < 1) {
					cursor = db.query(DB_ORDERTable.TABLE_NAME, null, null, null, null, null,
							DB_ORDERTable.COLUMN_WRITE_DATE + " DESC", MAX_RESULTS.toString());
				} else {
					int firstLimit = MAX_RESULTS * pagination;
					int lastLimit = firstLimit + MAX_RESULTS;
					cursor = db.query(DB_ORDERTable.TABLE_NAME, null, null, null, null, null,
							DB_ORDERTable.COLUMN_WRITE_DATE + " DESC", firstLimit + "," + lastLimit);
				}

				while (cursor.moveToNext()) {
					Order o = new Order();
					o.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_ID)));
					o.setName(cursor.getString(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_NAME)));
					o.setDateOrder(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_DATE_ORDER)));
					o.setPartnerID(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_PARTNER_ID)));

					o.setShopID(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_SHOP_ID)));

					o.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_USER_ID)));
					o.setState(Order.STATE.valueOf(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_STATE))));

					o.setTotal(cursor.getFloat(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_AMOUNT)));

					o.setWriteDate(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_WRITE_DATE)));

					items.add(o);
				}
				return items;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<List<Order>> getLastOrders(final Context context, final Integer num) {

		Callable<List<Order>> c = new Callable<List<Order>>() {

			@Override
			public List<Order> call() {

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Order> items = new ArrayList<Order>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_ORDERTable.TABLE_NAME, null, null, null, null, null,
						DB_ORDERTable.COLUMN_DATE_ORDER + " DESC", num.toString());

				while (cursor.moveToNext()) {
					Order o = new Order();
					o.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_ID)));
					o.setName(cursor.getString(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_NAME)));
					o.setDateOrder(cursor.getLong(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_DATE_ORDER)));
					o.setPartnerID(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_PARTNER_ID)));

					o.setShopID(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_SHOP_ID)));

					o.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_USER_ID)));
					// o.setState(Order.STATE.valueOf(cursor.getString(cursor
					// .getColumnIndexOrThrow(DB_ORDERTable.COLUMN_STATE))));

					o.setTotal(cursor.getFloat(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_AMOUNT)));

					o.setWriteDate(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_WRITE_DATE)));

					items.add(o);
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
				idDeleted = db.delete(DB_ORDERTable.TABLE_NAME, DB_ORDERTable.COLUMN_ID + " = " + idToDelete,
						null);

				Log.i(Constants.TAG, "ITEM DELETED, id " + idDeleted);
			}
		});
		t.start();
	}

	public Future<Long> insert(final Context context, final Order p) {

		Callable<Long> c = new Callable<Long>() {

			@Override
			public Long call() throws Exception {
				long id = -1;

				ContentValues cv = new ContentValues();
				cv.put(DB_ORDERTable.COLUMN_ID, p.getId());
				cv.put(DB_ORDERTable.COLUMN_NAME, p.getName());
				cv.put(DB_ORDERTable.COLUMN_PARTNER_ID, p.getPartnerID());
				cv.put(DB_ORDERTable.COLUMN_SHOP_ID, p.getShopID());
				cv.put(DB_ORDERTable.COLUMN_USER_ID, p.getUserID());
				cv.put(DB_ORDERTable.COLUMN_DATE_ORDER, p.getDateOrder());
				cv.put(DB_ORDERTable.COLUMN_STATE, p.getState().toString());
				cv.put(DB_ORDERTable.COLUMN_AMOUNT, p.getTotal());
				cv.put(DB_ORDERTable.COLUMN_WRITE_DATE, p.getWriteDate());

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				id = db.insert(DB_ORDERTable.TABLE_NAME, "<empty>", cv);

				return id;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<Order> searchByID(final Context context, final String id) {
		Callable<Order> c = new Callable<Order>() {

			@Override
			public Order call() {
				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_ORDERTable.TABLE_NAME, null, DB_ORDERTable.COLUMN_ID + "= ?",
						new String[] { id }, null, null, null, null);
				Order o = null;
				while (cursor.moveToNext()) {
					o = new Order();
					o.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_ID)));
					o.setName(cursor.getString(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_NAME)));
					o.setDateOrder(cursor.getLong(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_DATE_ORDER)));
					o.setPartnerID(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_PARTNER_ID)));

					o.setShopID(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_SHOP_ID)));

					o.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_USER_ID)));
					// o.setState(Order.STATE.valueOf(cursor.getString(cursor
					// .getColumnIndexOrThrow(DB_ORDERTable.COLUMN_STATE))));

					o.setTotal(cursor.getFloat(cursor.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_AMOUNT)));

					o.setWriteDate(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDERTable.COLUMN_WRITE_DATE)));

				}
				return o;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

}

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
import es.guadaltech.odoo.model.OrderLine;
import es.guadaltech.odoo.persistence.DB_ORDER_LINETable;
import es.guadaltech.odoo.persistence.MainDatabaseHelper;

public class OrderLineDAO {

	public static final Integer MAX_RESULTS = 30;

	public OrderLineDAO() {
	}

	public Future<List<OrderLine>> getAll(final Context context, final int pagination) {

		Callable<List<OrderLine>> c = new Callable<List<OrderLine>>() {

			@Override
			public List<OrderLine> call() {

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<OrderLine> items = new ArrayList<OrderLine>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				// No parameters, load MAX_RESULTS first items
				if (pagination < 1) {
					cursor = db.query(DB_ORDER_LINETable.TABLE_NAME, null, null, null, null, null,
							DB_ORDER_LINETable.COLUMN_WRITE_DATE + " DESC",MAX_RESULTS.toString());
				} else {
					int firstLimit = MAX_RESULTS * pagination;
					int lastLimit = firstLimit + MAX_RESULTS;
					cursor = db.query(DB_ORDER_LINETable.TABLE_NAME, null, null, null, null, null,
							DB_ORDER_LINETable.COLUMN_WRITE_DATE + " DESC", firstLimit + "," + lastLimit);
				}

				while (cursor.moveToNext()) {
					OrderLine o = new OrderLine();
					o.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_ID)));
					o.setDiscount(cursor.getFloat(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_DISCOUNT)));
					o.setNotice(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_NOTICE)));
					o.setOrderID(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_ORDER_ID)));
					o.setPriceUnit(cursor.getFloat(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_PRICE_UNIT)));
					o.setProductID(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_PRODUCT_ID)));
					o.setQuantity(cursor.getFloat(cursor.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_QTY)));
					o.setWriteDate(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_WRITE_DATE)));

					items.add(o);
				}
				return items;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<List<OrderLine>> searchById(final Context context, final String id) {

		Callable<List<OrderLine>> c = new Callable<List<OrderLine>>() {

			@Override
			public List<OrderLine> call() {

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<OrderLine> items = new ArrayList<OrderLine>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				// No parameters, load MAX_RESULTS first items
				
					cursor = db.query(DB_ORDER_LINETable.TABLE_NAME, null,DB_ORDER_LINETable.COLUMN_ORDER_ID+" = ?" , new String[] {id}, null, null,
							null, null);
				

				while (cursor.moveToNext()) {
					OrderLine o = new OrderLine();
					o.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_ID)));
					o.setDiscount(cursor.getFloat(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_DISCOUNT)));
					o.setNotice(cursor.getString(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_NOTICE)));
					o.setOrderID(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_ORDER_ID)));
					o.setPriceUnit(cursor.getFloat(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_PRICE_UNIT)));
					o.setProductID(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_PRODUCT_ID)));
					o.setQuantity(cursor.getFloat(cursor.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_QTY)));
					o.setWriteDate(cursor.getInt(cursor
							.getColumnIndexOrThrow(DB_ORDER_LINETable.COLUMN_WRITE_DATE)));

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
				idDeleted = db.delete(DB_ORDER_LINETable.TABLE_NAME, DB_ORDER_LINETable.COLUMN_ID + " = "
						+ idToDelete, null);

				Log.i(Constants.TAG, "ITEM DELETED, id " + idDeleted);
			}
		});
		t.start();
	}

	public Future<Long> insert(final Context context, final OrderLine o) {

		Callable<Long> c = new Callable<Long>() {

			@Override
			public Long call() throws Exception {
				long id = -1;

				ContentValues cv = new ContentValues();
				cv.put(DB_ORDER_LINETable.COLUMN_DISCOUNT, o.getDiscount());
				cv.put(DB_ORDER_LINETable.COLUMN_ID, o.getId());
				cv.put(DB_ORDER_LINETable.COLUMN_NOTICE, o.getNotice());
				cv.put(DB_ORDER_LINETable.COLUMN_ORDER_ID, o.getOrderID());
				cv.put(DB_ORDER_LINETable.COLUMN_PRICE_UNIT, o.getPriceUnit());
				cv.put(DB_ORDER_LINETable.COLUMN_PRODUCT_ID, o.getProductID());
				cv.put(DB_ORDER_LINETable.COLUMN_QTY, o.getQuantity());
				cv.put(DB_ORDER_LINETable.COLUMN_WRITE_DATE, o.getWriteDate());

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				id = db.insert(DB_ORDER_LINETable.TABLE_NAME, "<empty>", cv);

				return id;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

}

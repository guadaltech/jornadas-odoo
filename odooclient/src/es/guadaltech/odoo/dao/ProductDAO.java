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
import es.guadaltech.odoo.model.Product;
import es.guadaltech.odoo.persistence.DB_PRODUCTable;
import es.guadaltech.odoo.persistence.MainDatabaseHelper;

public class ProductDAO {

	public static final Integer MAX_RESULTS = 30;
	public static final boolean D = true; // debug

	public ProductDAO() {
	}

	public Future<List<Product>> getAll(final Context context, final int pagination) {

		Callable<List<Product>> c = new Callable<List<Product>>() {

			@Override
			public List<Product> call() {

				if (D)
					Log.d(Constants.TAG, "Calling getAll() on Product");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Product> items = new ArrayList<Product>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				// No parameters, load MAX_RESULTS first items
				if (pagination < 1) {
					cursor = db.query(DB_PRODUCTable.TABLE_NAME, null, null, null, null, null,
							DB_PRODUCTable.COLUMN_WRITE_DATE + " DESC", MAX_RESULTS.toString());
				} else {
					int firstLimit = MAX_RESULTS * pagination;
					int lastLimit = firstLimit + MAX_RESULTS;
					cursor = db.query(DB_PRODUCTable.TABLE_NAME, null, null, null, null, null,
							DB_PRODUCTable.COLUMN_WRITE_DATE + " DESC", firstLimit + "," + lastLimit);
				}

				while (cursor.moveToNext()) {
					items.add(buildProduct(cursor));
				}
				if (D)
					Log.d(Constants.TAG, "Product getAll() returned " + items.size());
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
				idDeleted = db.delete(DB_PRODUCTable.TABLE_NAME, DB_PRODUCTable.COLUMN_ID + " = "
						+ idToDelete, null);

				Log.i(Constants.TAG, "ITEM DELETED, id " + idDeleted);
			}
		});
		t.start();
	}

	public Future<Long> insert(final Context context, final Product p) {

		Callable<Long> c = new Callable<Long>() {

			@Override
			public Long call() throws Exception {

				if (D)
					Log.d(Constants.TAG, "Calling insert() on Product");

				long id = -1;

				ContentValues cv = new ContentValues();
				cv.put(DB_PRODUCTable.COLUMN_BARCODE, p.getEan13());
				cv.put(DB_PRODUCTable.COLUMN_CANBESOLD, p.getCanBeSold());
				cv.put(DB_PRODUCTable.COLUMN_CANBEPURCHASED, p.getCanBePurchased());
				cv.put(DB_PRODUCTable.COLUMN_CANBEEXPENSE, p.getCanBeExpense());
				cv.put(DB_PRODUCTable.COLUMN_DEFAULT_CODE, p.getDefaultCode());
				cv.put(DB_PRODUCTable.COLUMN_DESCRIPTION, p.getDescription());
				cv.put(DB_PRODUCTable.COLUMN_DESCRIPTION_SALE, p.getDescriptionSale());
				cv.put(DB_PRODUCTable.COLUMN_ID, p.getId());
				cv.put(DB_PRODUCTable.COLUMN_LIST_PRICE, p.getListPrice());
				cv.put(DB_PRODUCTable.COLUMN_NAME, p.getName());
				cv.put(DB_PRODUCTable.COLUMN_PRODUCT_IMAGE_SMALL, p.getProductImageSmall());
				cv.put(DB_PRODUCTable.COLUMN_QTY_AVA, p.getRealStock());
				cv.put(DB_PRODUCTable.COLUMN_STANDARD_PRICE, p.getCostPrice());
				cv.put(DB_PRODUCTable.COLUMN_VIRTUAL_AVA, p.getVirtualStock());
				cv.put(DB_PRODUCTable.COLUMN_WRITE_DATE, p.getWriteDate());

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				id = db.insertWithOnConflict(DB_PRODUCTable.TABLE_NAME, "<empty>", cv,
						SQLiteDatabase.CONFLICT_REPLACE);

				if (D)
					Log.d(Constants.TAG, "Product insert returned " + id);
				return id;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<List<Product>> searchByName(final Context context, final String name) {

		Callable<List<Product>> c = new Callable<List<Product>>() {

			@Override
			public List<Product> call() {

				if (D)
					Log.d(Constants.TAG, "Calling searchByName() on Product");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Product> items = new ArrayList<Product>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_PRODUCTable.TABLE_NAME, null, DB_PRODUCTable.COLUMN_NAME + " LIKE ?",
						new String[] { "%" + name + "%" }, null, null, null);
				// No parameters, load MAX_RESULTS first items

				while (cursor.moveToNext()) {
					items.add(buildProduct(cursor));
				}
				if (D)
					Log.d(Constants.TAG, "Product getAll() returned " + items.size());
				return items;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<List<Product>> searchByDescription(final Context context, final String desc) {

		Callable<List<Product>> c = new Callable<List<Product>>() {

			@Override
			public List<Product> call() {

				if (D)
					Log.d(Constants.TAG, "Calling getAll() on Product");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Product> items = new ArrayList<Product>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_PRODUCTable.TABLE_NAME, null, DB_PRODUCTable.COLUMN_DESCRIPTION
						+ " LIKE ?", new String[] { "%" + desc + "%" }, null, null, null);
				// No parameters, load MAX_RESULTS first items

				while (cursor.moveToNext()) {
					items.add(buildProduct(cursor));
				}
				if (D)
					Log.d(Constants.TAG, "Product getAll() returned " + items.size());
				return items;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<Product> searchEan13(final Context context, final String ean13) {

		Callable<Product> c = new Callable<Product>() {

			@Override
			public Product call() {

				if (D)
					Log.d(Constants.TAG, "Calling getAll() on Product");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				Product p = null;

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_PRODUCTable.TABLE_NAME, null, DB_PRODUCTable.COLUMN_BARCODE + " = ?",
						new String[] { ean13 }, null, null, null);

				if (cursor.moveToNext()) {
					p = (buildProduct(cursor));
				}
				return p;
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<Product> searchByID(final Context context, final String id) {

		Callable<Product> c = new Callable<Product>() {

			@Override
			public Product call() {

				if (D)
					Log.d(Constants.TAG, "Calling getAll() on Product");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Product> items = new ArrayList<Product>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_PRODUCTable.TABLE_NAME, null, DB_PRODUCTable.COLUMN_ID + "=?",
						new String[] { id }, null, null, null);
				// No parameters, load MAX_RESULTS first items

				while (cursor.moveToNext()) {
					items.add(buildProduct(cursor));
				}
				if (D)
					Log.d(Constants.TAG, "Product getAll() returned " + items.size());
				return items.get(0);
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<Product> searchByStock(final Context context) {

		Callable<Product> c = new Callable<Product>() {

			@Override
			public Product call() {

				if (D)
					Log.d(Constants.TAG, "Calling searchByStock() on Product");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Product> items = new ArrayList<Product>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_PRODUCTable.TABLE_NAME, null, DB_PRODUCTable.COLUMN_QTY_AVA + ">0",
						null, null, null, null);
				// No parameters, load MAX_RESULTS first items

				while (cursor.moveToNext()) {
					items.add(buildProduct(cursor));
				}
				if (D)
					Log.d(Constants.TAG, "Product searchByStock() returned " + items.size());
				return items.get(0);
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	public Future<Product> searchBetweenPrices(final Context context, final Double min, final Double max) {

		Callable<Product> c = new Callable<Product>() {

			@Override
			public Product call() {

				if (D)
					Log.d(Constants.TAG, "Calling searchByStock() on Product");

				MainDatabaseHelper dbHelper = MainDatabaseHelper.getInstance(context);
				ArrayList<Product> items = new ArrayList<Product>();

				SQLiteDatabase db = dbHelper.getWritableDatabase();

				Cursor cursor = null;

				cursor = db.query(DB_PRODUCTable.TABLE_NAME, null, DB_PRODUCTable.COLUMN_LIST_PRICE
						+ ">? AND " + DB_PRODUCTable.COLUMN_LIST_PRICE + "<?", new String[] { min.toString(),
						max.toString() }, null, null, null);

				while (cursor.moveToNext()) {
					items.add(buildProduct(cursor));
				}
				if (D)
					Log.d(Constants.TAG, "Product searchByStock() returned " + items.size());
				return items.get(0);
			};
		};
		ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
		return s.submit(c);
	}

	private static Product buildProduct(Cursor cursor) {

		Product p = new Product();
		p.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_ID)));
		p.setCanBeSold(1 == (cursor.getInt(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_CANBESOLD))));
		p.setCanBePurchased(1 == (cursor.getInt(cursor
				.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_CANBEPURCHASED))));
		p.setCanBeExpense(1 == (cursor.getInt(cursor
				.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_CANBEEXPENSE))));
		p.setCostPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_STANDARD_PRICE)));
		p.setDefaultCode(cursor.getString(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_DEFAULT_CODE)));
		p.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_DESCRIPTION)));
		p.setDescriptionSale(cursor.getString(cursor
				.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_DESCRIPTION_SALE)));
		p.setEan13(cursor.getString(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_BARCODE)));
		p.setListPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_LIST_PRICE)));
		p.setName(cursor.getString(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_NAME)));
		p.setProductImageSmall(cursor.getString(cursor
				.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_PRODUCT_IMAGE_SMALL)));
		p.setRealStock(cursor.getFloat(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_QTY_AVA)));
		p.setVirtualStock(cursor.getFloat(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_VIRTUAL_AVA)));
		p.setWriteDate(cursor.getLong(cursor.getColumnIndexOrThrow(DB_PRODUCTable.COLUMN_WRITE_DATE)));

		return p;
	}

}

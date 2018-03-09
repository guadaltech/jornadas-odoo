package es.guadaltech.odoo;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import es.guadaltech.odoo.dao.ProductDAO;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.GenericList;
import es.guadaltech.odoo.misc.ImageUtils;
import es.guadaltech.odoo.misc.JSONBuilder;
import es.guadaltech.odoo.misc.ListCellItem;
import es.guadaltech.odoo.misc.NumberUtils;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.model.Product;

public class ShowProductListActivity extends GenericList implements OnItemClickListener {

	private List<ListCellItem> items;
	private SharedPreferences prefs;
	private ProductAdapter adapter;
	private Gson gson;
	private ProductDAO pdao;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Spinner spinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		loadCustomActionBar();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showlist);

		// Prepare spinner
		spinner = (Spinner) findViewById(R.id.sp_sliding);
		String[] texto = getResources().getStringArray(R.array.filters_spinner_products);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown,
				texto);
		spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
		spinner.setAdapter(spinnerArrayAdapter);

		items = new ArrayList<ListCellItem>();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		gson = new Gson();
		pdao = new ProductDAO();

		adapter = new ProductAdapter(this);
		setListAdapter(adapter);

		if (getIntent().getBooleanExtra(Constants.INTENT_SHOW_PRODUCT_LIST_TO_SELECT, false)) {
			// Click on a product will select it and send to another activity
			OnItemClickListener selectorListener = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(ShowProductListActivity.this, TPVActivity.class);
					String oerp = adapter.getItem(position).getOpenerpid();
					intent.putExtra(Constants.INTENT_PRODUCT_FROM_LIST, oerp);
					intent.setAction(Constants.INTENT_ACTION_OERPID);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(intent);
					finish();
				}
			};

			getListView().setOnItemClickListener(selectorListener);
		} else {
			// Click on a product will show details
			getListView().setOnItemClickListener(this);
		}
		getListView().setOnScrollListener(new EndlessScrollListener());

		getOfflineItems(pagination);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, // host Activity
				mDrawerLayout, // DrawerLayout object
				R.drawable.ic_drawer, // nav drawer icon to replace 'Up' caret
				R.string.drawer_open, // "open drawer" description
				R.string.drawer_close // "close drawer" description
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {

			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {

			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(false);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

	}

	public class ProductAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public ProductAdapter(Context c) {
			inflater = LayoutInflater.from(c);
		}

		public int getCount() {
			return items.size();
		}

		public ListCellItem getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View v, ViewGroup parent) {
			ProductViewHolder viewHolder;
			if (v == null) { // if it's not recycled, initialize some attrs
				v = inflater.inflate(R.layout.row_listcellitem_product, null, false);

				viewHolder = new ProductViewHolder();
				viewHolder.tvCellProduct1 = (TextView) v.findViewById(R.id.tv_cell_product_1);
				viewHolder.tvCellProduct2 = (TextView) v.findViewById(R.id.tv_cell_product_2);
				viewHolder.tvCellProduct3 = (TextView) v.findViewById(R.id.tv_cell_product_3);
				viewHolder.ivCellProduct = (ImageView) v.findViewById(R.id.iv_cell_product);

				v.setTag(viewHolder);
			} else {
				viewHolder = (ProductViewHolder) v.getTag();
			}

			ListCellItem item = items.get(position);
			if (item != null) {
				// Set text views
				viewHolder.tvCellProduct1.setText(item.getText1());
				if (item.getText2().equals("false"))
					viewHolder.tvCellProduct2.setText("SIN REF");
				else
					viewHolder.tvCellProduct2.setText(item.getText2());
				viewHolder.tvCellProduct3.setText(item.getText3() + " €");

				if (item.getImage() != null && item.getImage() != "") {
					new BitmapWorkerTask((viewHolder.ivCellProduct)).execute(item.getImage());
				}

				// TODO Trying with Picasso and Content Provider, not working :(

				// Uri uri =
				// Uri.parse(ProductImageContentProvider.CONTENT_URI.toString()
				// + "/"
				// + item.getOpenerpid());
				// Picasso p = Picasso.with(getApplicationContext());
				// p.setDebugging(true);
				// if (item.getImage() != null && item.getImage() != "") {
				// p.load(uri).into(viewHolder.ivCellProduct);
				// }

			}
			return v;
		}
	}

	static class ProductViewHolder {
		TextView tvCellProduct1;
		TextView tvCellProduct2;
		TextView tvCellProduct3;
		ImageView ivCellProduct;
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private String data = null;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			data = params[0];
			return data != null ? ImageUtils.decodeSampledBitmapFromBase64(data, 128, 128) : null;
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	public void doSearch(View v) {
		// Hide keyboard when search button is hitted
		Spinner spinner = (Spinner) findViewById(R.id.sp_sliding);
		String query = ((EditText) mDrawerLayout.findViewById(R.id.et_clientes_sliding_busqueda)).getText()
				.toString();
		View mSearchView = findViewById(R.id.et_clientes_sliding_busqueda);
		if (mSearchView != null) {
			mSearchView.clearFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
		}
		// Show indeterminate spinner progress
		setProgressBarIndeterminateVisibility(true);
		int tipoBusqueda = spinner.getSelectedItemPosition();

		try {
			items.clear();
			List<Product> result = new ArrayList<Product>();
			switch (tipoBusqueda) {
			// nombre
			case 0:
				result = pdao.searchByName(this, query).get();
				for (Product product : result) {
					ListCellItem item = new ListCellItem(product.getProductImageSmall(), product.getName(),
							product.getDefaultCode(), String.format("%.2f", product.getListPrice()),
							product.getRealStock(), String.valueOf(product.getId()));
					items.add(item);
				}
				break;
			case 1:
				result = pdao.searchByDescription(this, query).get();
				for (Product product : result) {
					ListCellItem item = new ListCellItem(product.getProductImageSmall(), product.getName(),
							product.getDefaultCode(), String.format("%.2f", product.getListPrice()),
							product.getRealStock(), String.valueOf(product.getId()));
					items.add(item);
				}
				break;
			default:
				break;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		applyFilters();
		adapter.notifyDataSetChanged();
		mDrawerLayout.closeDrawers();
		setProgressBarIndeterminateVisibility(false);
		mDrawerLayout.closeDrawers();
	}

	// Between prices filter is performed first, after that, stock filter is
	// performed
	public void applyFilters() {
		String etEntre, etY;
		etEntre = ((EditText) findViewById(R.id.et_sliding_entre)).getText().toString();
		etY = ((EditText) findViewById(R.id.et_sliding_y)).getText().toString();
		List<ListCellItem> filtrados = null;
		if (etEntre != "" || etY.toString() != "") {
			filtrados = new ArrayList<ListCellItem>();
			int entre, y;
			try {
				entre = Integer.valueOf(etEntre);
				y = Integer.valueOf(etY);
			} catch (NumberFormatException e) {
				entre = 0;
				y = Integer.MAX_VALUE;
			}
			for (ListCellItem item : items) {
				NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
				double price = 0.0;
				try {
					price = format.parse(item.getText3()).doubleValue();
				} catch (ParseException e) {
					e.printStackTrace();
				}

				if (price > entre && price < y)
					filtrados.add(item);
			}
			items = filtrados;

		}

		CheckBox cbStock = (CheckBox) findViewById(R.id.cb_sliding);
		if (cbStock.isChecked()) {
			List<ListCellItem> filtrados2 = new ArrayList<ListCellItem>();
			for (ListCellItem item : filtrados) {
				if (item.getQty() > 0)
					filtrados2.add(item);
			}
			items = filtrados2;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(ShowProductListActivity.this, ProductShowDetailsActivity.class);
		intent.putExtra(Constants.INTENT_KEY_CODE, items.get(position).getOpenerpid());
		startActivity(intent);
	}

	@Override
	protected void getServerItems() throws JSONException {
		getNewlyCreated();
		getNewlyModified();
	}

	protected void getNewlyCreated() throws JSONException {
		AsyncHttpClient client = new AsyncHttpClient();

		PersistentCookieStore myCookieStore = new PersistentCookieStore(ShowProductListActivity.this);
		client.setCookieStore(myCookieStore);

		JSONBuilder jsonBuilder = new JSONBuilder(prefs);
		String json = null;
		final long fetchDate = prefs.getLong(Constants.PREFS_KEY_PRODUCT_CREATED_FETCH_DATE, -1);

		try {
			JSONArray filter = null;// Inserts deliberately null filter if no
									// conditions

			if (fetchDate > 0) {
				String sdate = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date(fetchDate)).toString();

				filter = new JSONArray();
				filter.put("create_date");
				filter.put(">");
				filter.put(sdate);
			}
			json = jsonBuilder.get("product.product", "searchandread", filter);
		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		Log.i("OpenERP", "## Show list request -> " + json);

		RequestParams p = new RequestParams();
		p.put("json", json);

		client.post(
				NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_MIDDLEWARE_IP,
						Constants.OERP_MIDDLEWARE_DEFAULT)) + "/json", p, new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(String response) {
						Log.i(Constants.TAG, "### Sucess loading list ##RESPONSE##-> " + response);
						JSONArray jsonArray = null;
						try {
							jsonArray = new JSONArray(response);
						} catch (JSONException e2) {
							e2.printStackTrace();
						}

						// Si el array es nulo, ha habido un problema con el
						// server
						// Si el array está vacío, no hay nuevos elementos,
						// fetch_date NO se debe actualizar
						if (jsonArray == null || jsonArray.length() == 0)
							return;

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = null;
							try {
								item = jsonArray.getJSONObject(i);
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
							System.out.println("Processing item " + i);
							ListCellItem cellItem = null;
							try {
								String code = item.getString("code").equals("false") ? getString(R.string.dots)
										: item.getString("code");
								cellItem = new ListCellItem(item.getString("product_image"), item
										.getString("name_template"), code, String.format("%.2f",
										item.getDouble("lst_price")), item.getDouble("qty_available"), String
										.valueOf(item.getInt("id")));
							} catch (JSONException e) {
								Log.e(Constants.TAG, "Json parsing exception");
							}
							if (cellItem != null) {
								if (!items.contains(cellItem))
									items.add(0, cellItem);
							}

							// Persist
							Product p = gson.fromJson(item.toString(), Product.class);
							p.setWriteDate(fetchDate);
							pdao.insert(ShowProductListActivity.this, p);

							// Magic happens here, server time system ;)
							Date date = new Date(System.currentTimeMillis() - 7200000);
							Editor e = prefs.edit();
							e.putLong(Constants.PREFS_KEY_PRODUCT_CREATED_FETCH_DATE, date.getTime());
							e.commit();
						}
						adapter.notifyDataSetChanged();
					}

					@Override
					public void onFailure(Throwable arg0, String response) {
						Log.i(Constants.TAG, "### Failure loading list ##RESPONSE##-> " + response);
						Toast.makeText(ShowProductListActivity.this, response, Toast.LENGTH_LONG).show();
					}

					@Override
					public void onStart() {
						Log.i(Constants.TAG, "### Start populating list RECENTLY CREATED");
						setProgressBarIndeterminateVisibility(true);
					}

					@Override
					public void onFinish() {
						Log.i(Constants.TAG, "### End populating list RECENTLY CREATED");
						setProgressBarIndeterminateVisibility(false);
					}
				});

	}

	protected void getNewlyModified() throws JSONException {
		AsyncHttpClient client = new AsyncHttpClient();

		PersistentCookieStore myCookieStore = new PersistentCookieStore(ShowProductListActivity.this);
		client.setCookieStore(myCookieStore);

		JSONBuilder jsonBuilder = new JSONBuilder(prefs);
		String json = null;
		final long fetchDate = prefs.getLong(Constants.PREFS_KEY_PRODUCT_WROTE_FETCH_DATE, -1);

		try {
			JSONArray filter = null;// Inserts deliberately null filter if no
									// conditions

			if (fetchDate > 0) {
				String sdate = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date(fetchDate)).toString();

				filter = new JSONArray();
				filter.put("write_date");
				filter.put(">");
				filter.put(sdate);
			}
			json = jsonBuilder.get("product.product", "searchandread", filter);
		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		Log.i("OpenERP", "## Show list request -> " + json);

		RequestParams p = new RequestParams();
		p.put("json", json);

		client.post(
				NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_MIDDLEWARE_IP,
						Constants.OERP_MIDDLEWARE_DEFAULT)) + "/json", p, new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(String response) {
						Log.i(Constants.TAG, "### Sucess loading list ##RESPONSE##-> " + response);
						JSONArray jsonArray = null;
						try {
							jsonArray = new JSONArray(response);
						} catch (JSONException e2) {
							e2.printStackTrace();
						}

						// Si el array es nulo, ha habido un problema con el
						// server
						// Si el array está vacío, no hay nuevos elementos,
						// fetch_date
						// NO se debe actualizar
						if (jsonArray == null || jsonArray.length() == 0)
							return;

						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject item = null;
							try {
								item = jsonArray.getJSONObject(i);
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
							System.out.println("Processing item " + i);
							ListCellItem cellItem = null;
							try {
								String code = item.getString("code").equals("false") ? getString(R.string.dots)
										: item.getString("code");
								cellItem = new ListCellItem(item.getString("product_image"), item
										.getString("name_template"), code, String.format("%.2f",
										item.getDouble("lst_price")), item.getDouble("qty_available"), String
										.valueOf(item.getInt("id")));
							} catch (JSONException e) {
								Log.e(Constants.TAG, "Json parsing exception");
							}
							if (cellItem != null) {
								if (!items.contains(cellItem))
									items.add(0, cellItem);
							}

							// Persist
							Product p = gson.fromJson(item.toString(), Product.class);
							p.setWriteDate(fetchDate);
							pdao.insert(ShowProductListActivity.this, p);

							// Magic happens here, server time system ;)

							Calendar calendar = new GregorianCalendar();
							calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
							long cal = calendar.getTimeInMillis();
							Editor e = prefs.edit();
							e.putLong(Constants.PREFS_KEY_PRODUCT_WROTE_FETCH_DATE, cal);
							e.commit();
						}
						adapter.notifyDataSetChanged();
					}

					@Override
					public void onFailure(Throwable arg0, String response) {
						Log.i(Constants.TAG, "### Failure loading list ##RESPONSE##-> " + response);
						Toast.makeText(ShowProductListActivity.this, response, Toast.LENGTH_LONG).show();
					}

					@Override
					public void onStart() {
						Log.i(Constants.TAG, "### Start populating list RECENTLY MODIFIED");
						setProgressBarIndeterminateVisibility(true);
					}

					@Override
					public void onFinish() {
						Log.i(Constants.TAG, "### End populating list RECENTLY MODIFIED");
						setProgressBarIndeterminateVisibility(false);
					}
				});

	}

	@Override
	protected void getOfflineItems(Integer pagination) {
		List<Product> products = new ArrayList<Product>();
		try {
			products = pdao.getAll(ShowProductListActivity.this, pagination).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		for (Product p : products) {
			ListCellItem lci = new ListCellItem(p.getProductImageSmall(), p.getName(), p.getDefaultCode(),
					String.format("%.2f", p.getListPrice()), p.getRealStock(), String.valueOf(p.getId()));
			items.add(lci);
		}

		adapter.notifyDataSetChanged();
	}

	private void loadCustomActionBar() {
		// Custom typeface
		SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
				+ getString(R.string.ab_title_products_list));
		s.setSpan(new TypefaceSpan(this, "RobotoSlab-Bold", true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);

		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_product_partner, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_show:
			toggleRight();
			return true;
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_refresh:
			try {
				getServerItems();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public boolean toggleRight() {
		View vr = findViewById(R.id.right_drawer);
		if (mDrawerLayout.isDrawerOpen(vr)) {
			mDrawerLayout.closeDrawer(vr);
			return false;
		} else {
			mDrawerLayout.openDrawer(vr);
			return true;
		}
	}

}

package es.guadaltech.odoo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import es.guadaltech.odoo.dao.PartnerAddressDAO;
import es.guadaltech.odoo.dao.PartnerDAO;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.GenericList;
import es.guadaltech.odoo.misc.JSONBuilder;
import es.guadaltech.odoo.misc.ListCellItem;
import es.guadaltech.odoo.misc.NumberUtils;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.model.Partner;
import es.guadaltech.odoo.model.PartnerAddress;

public class ShowPartnersListActivity extends GenericList implements OnItemClickListener {

	private List<ListCellItem> items;
	private SharedPreferences prefs;
	private PartnerAdapter adapter;
	private Gson gson;
	private PartnerDAO pdao;
	private PartnerAddressDAO pAddressDAO;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Spinner spinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		loadCustomActionBar();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showlist);

		items = new ArrayList<ListCellItem>();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		gson = new Gson();
		pdao = new PartnerDAO();

		adapter = new PartnerAdapter(this);
		setListAdapter(adapter);

		LinearLayout llClientes = (LinearLayout) findViewById(R.id.ll_sliding_productos);
		llClientes.setVisibility(View.GONE);

		// Prepare spinner
		spinner = (Spinner) findViewById(R.id.sp_sliding);
		String[] texto = getResources().getStringArray(R.array.filters_spinner_partners);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown,
				texto);
		spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
		spinner.setAdapter(spinnerArrayAdapter);

		getListView().setOnItemClickListener(this);
		getListView().setOnScrollListener(new EndlessScrollListener());

		getOfflineItems(pagination);

		if (getIntent().getBooleanExtra(Constants.INTENT_SHOW_PARTNER_LIST_TO_SELECT, false)) {
			// Click on a product will select it and send to another activity
			OnItemClickListener selectorListener = new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(ShowPartnersListActivity.this, TPVActivity.class);
					String oerp = adapter.getItem(position).getOpenerpid();

					Editor editor = prefs.edit();
					editor.putString("partner_name", adapter.getItem(position).getText1());
					editor.putString("partner_id", oerp);
					editor.commit();

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

	@Override
	protected void getOfflineItems(Integer pagination) {
		List<Partner> partners = new ArrayList<Partner>();
		try {
			partners = pdao.getAll(ShowPartnersListActivity.this, pagination).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		for (Partner p : partners) {
			items.add(createCellItem(p));
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void getServerItems() throws JSONException {
		getNewlyCreated();
		getNewlyModified();
	}

	protected void getNewlyCreated() throws JSONException {
		// Prepare the http client
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(ShowPartnersListActivity.this);
		client.setCookieStore(myCookieStore);

		JSONBuilder jsonBuilder = new JSONBuilder(prefs);
		String json = null;
		final long fetchDate = prefs.getLong(Constants.PREFS_KEY_PARTNER_CREATED_FETCH_DATE, -1);
		JSONArray filter = null;// Inserts deliberately null filter
		try {

			if (fetchDate > 0) {
				String sdate = DateFormat.format("yyyy-MM-dd kk:mm:ss", new Date(fetchDate)).toString();

				filter = new JSONArray();
				filter.put("create_date");
				filter.put(">");
				filter.put(sdate);
			}
			json = jsonBuilder.get("res.partner", "searchandread", filter);
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
								cellItem = createCellItem(item);
							} catch (JSONException e) {
								Log.e(Constants.TAG, "Json parsing exception");
							}
							if (cellItem != null) {
								if (!items.contains(cellItem))
									items.add(0, cellItem);
							}

							// Persist
							Partner p = gson.fromJson(item.toString(), Partner.class);
							p.setWriteDate(fetchDate);
							pdao.insert(ShowPartnersListActivity.this, p);

							Calendar calendar = new GregorianCalendar();
							calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
							long cal = calendar.getTimeInMillis();

							Editor e = prefs.edit();

							Log.e(Constants.TAG, "Calendar -> " + cal);

							e.putLong(Constants.PREFS_KEY_PARTNER_CREATED_FETCH_DATE, cal);
							e.commit();
						}
						adapter.notifyDataSetChanged();
					}

					@Override
					public void onFailure(Throwable arg0, String response) {
						Log.i(Constants.TAG, "### Failure loading list ##RESPONSE##-> " + response);
						Toast.makeText(ShowPartnersListActivity.this, response, Toast.LENGTH_LONG).show();
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
		// partner address query
		json = jsonBuilder.get("res.partner.address", "searchandread", filter);
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

							// Persist
							PartnerAddress pa = gson.fromJson(item.toString(), PartnerAddress.class);
							pa.setWriteDate(fetchDate);
							pAddressDAO = new PartnerAddressDAO();
							pAddressDAO.insert(ShowPartnersListActivity.this, pa);

							// Magic happens here, server time system ;)
							Date date = new Date(System.currentTimeMillis() - 7200000);
							Editor e = prefs.edit();
							e.putLong(Constants.PREFS_KEY_PARTNER_CREATED_FETCH_DATE, date.getTime());
							e.commit();
						}
						adapter.notifyDataSetChanged();
					}

					@Override
					public void onFailure(Throwable arg0, String response) {
						Log.i(Constants.TAG, "### Failure loading list ##RESPONSE##-> " + response);
						Toast.makeText(ShowPartnersListActivity.this, response, Toast.LENGTH_LONG).show();
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

	public void doSearch(View v) {
		// Hide keyboard when search button is hitted

		Spinner spinner = (Spinner) findViewById(R.id.sp_sliding);
		String[] texto = { "Nombre", "Notas" };
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, texto);
		spinner.setAdapter(spinnerArrayAdapter);
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
			List<Partner> result = new ArrayList<Partner>();
			switch (tipoBusqueda) {
			// nombre
			case 0:
				result = pdao.searchByName(this, query).get();
				for (Partner p : result) {
					ListCellItem item = createCellItem(p);
					items.add(item);
				}

				break;
			// TODO implementar notas
			// case 1:
			// result = pdao.searchByDescription(this, query).get();
			// for (Product product : result) {
			// ListCellItem item = new ListCellItem(null, product.getName(),
			// product.getDefaultCode(),
			// String.valueOf(product.getListPrice()), product.getCostPrice(),
			// String.valueOf(product.getId()));
			// items.add(item);
			// }
			// break;
			default:
				break;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		adapter.notifyDataSetChanged();
		mDrawerLayout.closeDrawers();
		setProgressBarIndeterminateVisibility(false);
		mDrawerLayout.closeDrawers();
		//
		// query = TextParser.removeQuote(query);

		//
		// manageSearchResults(spinner.getSelectedItemPosition(), query, marks,
		// 1.0);
	}

	protected void getNewlyModified() throws JSONException {
		AsyncHttpClient client = new AsyncHttpClient();

		PersistentCookieStore myCookieStore = new PersistentCookieStore(ShowPartnersListActivity.this);
		client.setCookieStore(myCookieStore);

		JSONBuilder jsonBuilder = new JSONBuilder(prefs);
		String json = null;
		final long fetchDate = prefs.getLong(Constants.PREFS_KEY_PARTNER_CREATED_FETCH_DATE, -1);

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
			json = jsonBuilder.get("res.partner", "searchandread", filter);
		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		Log.i("OpenERP", "## Show list request -> " + json);

		RequestParams p = new RequestParams();
		p.put("json", json);

		client.post(NumberUtils.decode(Constants.OERP_MIDDLEWARE_DEFAULT) + "/json", p,
				new AsyncHttpResponseHandler() {
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
								cellItem = createCellItem(item);
							} catch (JSONException e) {
								Log.e(Constants.TAG, "Json parsing exception");
							}
							if (cellItem != null) {
								if (!items.contains(cellItem))
									items.add(0, cellItem);
							}

							// Persist
							Partner p = gson.fromJson(item.toString(), Partner.class);
							p.setWriteDate(fetchDate);
							pdao.insert(ShowPartnersListActivity.this, p);

							// Magic happens here, server time system ;)
							Date date = new Date(System.currentTimeMillis() - 7200000);
							Editor e = prefs.edit();
							e.putLong(Constants.PREFS_KEY_PARTNER_CREATED_FETCH_DATE, date.getTime());
							e.commit();
						}
						adapter.notifyDataSetChanged();
					}

					@Override
					public void onFailure(Throwable arg0, String response) {
						Log.i(Constants.TAG, "### Failure loading list ##RESPONSE##-> " + response);
						Toast.makeText(ShowPartnersListActivity.this, response, Toast.LENGTH_LONG).show();
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

	// ===================== List adapter ==============================

	public class PartnerAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public PartnerAdapter(Context c) {
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

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) { // if it's not recycled, initialize some attrs
				v = inflater.inflate(R.layout.row_listcellitem_partner, null);
			} else {

			}

			ListCellItem item = items.get(position);
			if (item != null) {

				// Set text views, using placeholder when null
				String text1, text2, text3;
				text1 = text2 = text3 = getString(R.string.dots);
				if (item.getText1() != null && !"false".equals(item.getText1()))
					text1 = item.getText1();
				((TextView) v.findViewById(R.id.tv_cell_partner_1)).setText(text1);
				if (item.getText2() != null && !"false".equals(item.getText2()))
					text2 = item.getText2();
				((TextView) v.findViewById(R.id.tv_cell_partner_2)).setText(text2);
				if (item.getText3() != null && !"false".equals(item.getText3()))
					text3 = item.getText3();
				((TextView) v.findViewById(R.id.tv_cell_partner_3)).setText(text3);
			}

			return v;
		}

	}

	// ============================= Misc methods =========================

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//
		Intent intent = new Intent(ShowPartnersListActivity.this, PartnerShowDetailsActivity.class);
		intent.putExtra(Constants.INTENT_KEY_CODE, items.get(position).getOpenerpid());
		startActivity(intent);
	}

	protected ListCellItem createCellItem(Partner p) {
		return new ListCellItem(null, p.getName(), p.getFunction(), p.getPhone(), 0.0, p.getId().toString());
	}

	protected ListCellItem createCellItem(JSONObject o) throws JSONException {
		return new ListCellItem(null, o.getString("name"), o.getString("function"), o.getString("phone"),
				0.0, String.valueOf(o.getInt("id")));
	}

	private void loadCustomActionBar() {
		// Custom typeface
		SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
				+ getString(R.string.ab_title_partners_list));
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
		case R.id.action_refresh:
			try {
				getServerItems();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return true;
		case android.R.id.home:
			finish();
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

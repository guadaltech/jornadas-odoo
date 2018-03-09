package es.guadaltech.odoo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import es.guadaltech.odoo.dao.CompanyParamsDAO;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.JSONBuilder;
import es.guadaltech.odoo.misc.NumberUtils;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.model.CompanyParams;

public class InitSaleActivity extends Activity {
	private SharedPreferences prefs;
	private ArrayAdapter<CharSequence> adptTienda, adptTarifa, adptDiario;
	private Spinner tienda, tarifa, diario;
	private Map<String, Integer> ids;
	private Context ctx;

	CompanyParamsDAO dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		loadCustomActionBar();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_sale);
		ctx = this;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		dao = new CompanyParamsDAO();
		ids = new HashMap<String, Integer>();
		// Spinners inits
		tienda = (Spinner) findViewById(R.id.activity_init_sale_spinner1);
		tarifa = (Spinner) findViewById(R.id.activity_init_sale_spinner2);
		diario = (Spinner) findViewById(R.id.activity_init_sale_spinner3);

		adptTienda = new ArrayAdapter<CharSequence>(this, R.layout.spinner_dropdown);
		adptTienda.setDropDownViewResource(R.layout.spinner_dropdown);

		adptTarifa = new ArrayAdapter<CharSequence>(this, R.layout.spinner_dropdown);
		adptTarifa.setDropDownViewResource(R.layout.spinner_dropdown);

		adptDiario = new ArrayAdapter<CharSequence>(this, R.layout.spinner_dropdown);
		adptDiario.setDropDownViewResource(R.layout.spinner_dropdown);

		tienda.setAdapter(adptTienda);
		tarifa.setAdapter(adptTarifa);
		diario.setAdapter(adptDiario);

		fetchValuesDB();

		Button btnButton = (Button) findViewById(R.id.btn_init_sale_next);
		btnButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Integer diarioID = ids.get(tienda.getSelectedItem().toString());
				Integer tarifaID = ids.get(tarifa.getSelectedItem().toString());
				Integer tiendaID = ids.get(tienda.getSelectedItem().toString());
				Intent intent = new Intent(InitSaleActivity.this, ShowPartnersListActivity.class);
				intent.putExtra(Constants.INTENT_SHOW_PARTNER_LIST_TO_SELECT, true);

				Editor editor = prefs.edit();
				editor.putInt(Constants.CP_TYPE_DIARIO, diarioID);
				editor.putInt(Constants.CP_TYPE_TARIFA, tarifaID);
				editor.putInt(Constants.CP_TYPE_TIENDA, tiendaID);
				editor.commit();

				startActivity(intent);
			}
		});

	}

	private ArrayAdapter<CharSequence> setSpinnerValues(int which) {

		switch (which) {
		case Constants.SPINNER_TIENDA:
			fetchValues(getString(R.string.oerp_tienda));
			break;
		case Constants.SPINNER_TARIFA:
			fetchValues(getString(R.string.oerp_tarifa));
			break;
		case Constants.SPINNER_DIARIO:
			fetchValues(getString(R.string.oerp_diario));
			break;
		default:
			break;
		}
		return null;
	}

	private void clearAdapters() {
		adptDiario.clear();
		adptTienda.clear();
		adptTarifa.clear();
	}

	// Fetch values from database
	private void fetchValuesDB() {
		clearAdapters();
		try {
			List<CompanyParams> params = dao.getAll(ctx).get();
			if (params.isEmpty())
				return;
			for (CompanyParams cp : params) {
				ids.put(cp.getName(), cp.getIdOERP());
				if (cp.getType().equals(Constants.CP_TYPE_DIARIO)) {
					adptDiario.add(cp.getName());
					adptDiario.notifyDataSetChanged();
				} else if (cp.getType().equals(Constants.CP_TYPE_TIENDA)) {
					adptTienda.add(cp.getName());
					adptTienda.notifyDataSetChanged();
				} else if (cp.getType().equals(Constants.CP_TYPE_TARIFA)) {
					adptTarifa.add(cp.getName());
					adptTarifa.notifyDataSetChanged();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	// fetch values from open erp server
	private void fetchValues(final String model) {
		AsyncHttpClient client = new AsyncHttpClient();
		final CompanyParamsDAO cpDAO = new CompanyParamsDAO();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(InitSaleActivity.this);
		client.setCookieStore(myCookieStore);

		JSONBuilder jsonBuilder = new JSONBuilder(prefs);
		String json = null;

		try {
			JSONArray filter = null;// Inserts deliberately null filter if no
									// conditions
			if (model.equals("account.journal") || model.equals("product.pricelist")) {
				JSONArray in;
				in = new JSONArray();
				in.put("type");
				in.put("=");
				in.put("sale");

				filter = in;
			}
			json = jsonBuilder.get(model, "searchandread", filter);
		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		Log.i("OpenERP", "## Show Spinner values -> " + json);

		RequestParams p = new RequestParams();
		p.put("json", json);

		client.post(NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_MIDDLEWARE_IP,
				Constants.OERP_MIDDLEWARE_DEFAULT)) + "/json", p,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(String response) {
						Log.i(Constants.TAG, "### Sucess loading spinner values ##RESPONSE##-> " + response);
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
							try {
								String name = item.getString("name");
								Integer id = item.getInt("id");

								ids.put(name, id);
								CompanyParams cp = new CompanyParams();
								cp.setIdOERP(id);
								cp.setName(name);
								if (model.equals("sale.shop")) {
									cp.setType(Constants.CP_TYPE_TIENDA);

								} else if (model.equals("product.pricelist")) {
									cp.setType(Constants.CP_TYPE_TARIFA);
								} else if (model.equals("account.journal")) {
									cp.setType(Constants.CP_TYPE_DIARIO);
								}
								cpDAO.insert(ctx, cp);
							} catch (JSONException e) {
								Log.e(Constants.TAG, "Json parsing exception\n" + e.getMessage());
							}

						}

					}

				});
	}

	// ========================= Menus stuff ===========================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.initsale, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_refresh:

			setSpinnerValues(Constants.SPINNER_DIARIO);
			setSpinnerValues(Constants.SPINNER_TIENDA);
			setSpinnerValues(Constants.SPINNER_TARIFA);
			fetchValuesDB();
			return true;
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadCustomActionBar() {
		// Custom typeface
		SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
				+ getString(R.string.ab_title_init_sale));
		s.setSpan(new TypefaceSpan(this, "RobotoSlab-Bold", true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);

		actionBar.setDisplayHomeAsUpEnabled(true);
	}

}

package es.guadaltech.odoo;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.NumberUtils;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.persistence.MainDatabaseHelper;

public class SettingsActivity extends Activity implements OnClickListener {

	// TODO delete all when database changes, method is yet created, just
	// implement it!

	private EditText etMiddleware, etServerIp, etServerPort;
	private Button btnRefreshDb, btnLogout;
	private SharedPreferences prefs;
	private TextView tvDatabase, tvVersion;
	private AlertDialog.Builder builderSingle;
	private Boolean res = false;
	private Context ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		loadCustomActionBar();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Setup views...
		etMiddleware = (EditText) findViewById(R.id.et_settings_middleware);
		etServerIp = (EditText) findViewById(R.id.et_settings_server_ip);
		etServerPort = (EditText) findViewById(R.id.et_settings_server_port);
		btnRefreshDb = (Button) findViewById(R.id.btn_settings_database);
		btnLogout = (Button) findViewById(R.id.btn_settings_logout);

		tvDatabase = (TextView) findViewById(R.id.tv_settings_database);
		tvVersion = (TextView) findViewById(R.id.tv_settings_version);
		// Set onClick listeners
		btnRefreshDb.setOnClickListener(this);
		btnLogout.setOnClickListener(this);
		ctx = this;

	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshViews();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_settings_database:
			// Show a dialog to fetch databases in server

			AsyncHttpClient client = new AsyncHttpClient();
			String json = "{\"hola\":\"hola\"}";

			StringEntity se = null;
			try {
				se = new StringEntity(json);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

			String dbEndPoint = "http://"
					+ NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_IP,
							Constants.OERP_SERVER_DEFAULT))
					+ ":"
					+ NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_PORT,
							Constants.OERP_SERVER_PORT_DEFAULT)) + "/web/database/get_list";

			client.post(SettingsActivity.this, dbEndPoint, se, "application/json",
					new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {

							JSONObject jObject = null;
							try {
								jObject = new JSONObject(response);
							} catch (JSONException e2) {
								e2.printStackTrace();
							}

							if (jObject == null || jObject.length() == 0)
								return;

							List<String> dbList = new ArrayList<String>();

							try {
								JSONArray dbArray = new JSONArray(new JSONObject(jObject.getString("result"))
										.getString("db_list"));

								for (int i = 0, size = dbArray.length(); i < size; i++) {
									dbList.add(dbArray.getString(i));
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}

							if (dbList.size() > 0) {
								// Databases fetched, now populate and show
								// dialog
								builderSingle = new AlertDialog.Builder(SettingsActivity.this);
								SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
										+ getString(R.string.settings_dialog_title_databases));
								s.setSpan(new TypefaceSpan(SettingsActivity.this,
										getString(R.string.common_font), true), 0, s.length(),
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								builderSingle.setTitle(s);
								final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
										SettingsActivity.this, android.R.layout.select_dialog_singlechoice);
								arrayAdapter.addAll(dbList);
								builderSingle.setNegativeButton(SettingsActivity.this
										.getString(R.string.settings_dialog_databases_cancel),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										});

								builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										String strName = arrayAdapter.getItem(which);

										showDisclaimer(strName);
										dialog.dismiss();

									}
								});
								builderSingle.show();
							} else {
								Crouton.makeText(SettingsActivity.this,
										getString(R.string.settings_dialog_databases_error), Style.ALERT)
										.show();
							}

						}

						@Override
						public void onFailure(Throwable arg0, String response) {
							Log.i(Constants.TAG, "### Failure loading databases ##RESPONSE##-> " + response);
							Crouton.makeText(SettingsActivity.this,
									getString(R.string.settings_dialog_server_error), Style.ALERT).show();
						}

						@Override
						public void onStart() {
							Log.i(Constants.TAG, "### Start fetching databases");
						}

						@Override
						public void onFinish() {
							Log.i(Constants.TAG, "### End fetching databases");
						}
					});

			break;
		case R.id.btn_settings_logout:
			PersistentCookieStore myCookieStore = new PersistentCookieStore(SettingsActivity.this);
			myCookieStore.clear(); // Fixes issue #1, login with cookie
			Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			this.finish();
			break;
		}
	}

	private Boolean showDisclaimer(final String name) {

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					Editor e = prefs.edit();
					if (NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_DATABASE, "")).equals(name))
						return;
					e.putString(Constants.PREFS_KEY_DATABASE, NumberUtils.encode(name));
					e.putLong(Constants.PREFS_KEY_PRODUCT_CREATED_FETCH_DATE, 0);
					e.putLong(Constants.PREFS_KEY_PRODUCT_WROTE_FETCH_DATE, 0);
					e.putLong(Constants.PREFS_KEY_PARTNER_CREATED_FETCH_DATE, 0);
					e.putLong(Constants.PREFS_KEY_PARTNER_WROTE_FETCH_DATE, 0);
					e.commit();
					MainDatabaseHelper.getInstance(ctx).close();
					MainDatabaseHelper.destroyInstance();
					Boolean wasDestroyed = ctx.deleteDatabase("persistence.db");
					Log.d(Constants.TAG, "destroy = " + wasDestroyed);

					refreshViews();
					res = true;
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked

					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("A ver miarma como cambias la base de datos se va to al carajo sabono?")
				.setNegativeButton("Que habla!!!", dialogClickListener)
				.setPositiveButton("Enga", dialogClickListener).show();
		return res;
	}

	// ========================= Menus stuff ===========================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings_save:
			// Save base64'd strings in preferences
			Editor e = prefs.edit();
			e.putString(Constants.PREFS_KEY_MIDDLEWARE_IP,
					NumberUtils.encode(etMiddleware.getText().toString()));
			e.putString(Constants.PREFS_KEY_SERVER_IP, NumberUtils.encode(etServerIp.getText().toString()));
			e.putString(Constants.PREFS_KEY_SERVER_PORT,
					NumberUtils.encode(etServerPort.getText().toString()));
			e.commit();

			Toast.makeText(SettingsActivity.this, getString(R.string.settings_dialog_databases_sucess),
					Toast.LENGTH_SHORT).show();
			finish();
			return true;
		case R.id.action_settings_cancel:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// ========================= Custom GUI =============================

	private void loadCustomActionBar() {
		// Custom typeface
		SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
				+ getString(R.string.ab_title_settings));
		s.setSpan(new TypefaceSpan(this, getString(R.string.common_font), true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);
	}

	private void refreshViews() {
		String middlewareIp = NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_MIDDLEWARE_IP,
				Constants.OERP_MIDDLEWARE_DEFAULT));
		String serverIp = NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_IP,
				Constants.OERP_SERVER_DEFAULT));
		String serverPort = NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_PORT,
				Constants.OERP_SERVER_PORT_DEFAULT));
		String dbName = NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_DATABASE,
				Constants.OERP_DB_DEFAULT));

		// Give values to views
		etMiddleware.setText(middlewareIp);
		etServerIp.setText(serverIp);
		etServerPort.setText(serverPort);
		tvDatabase.setText(dbName);
		new UpdateVersions().execute();
	}

	public void deleteLocalContent() {
		// Borrar datos en tablas
		// Borrar last_fetched y demás en la base de datos
		// Mostrar diálogo (de confirmación, no dará opción a que se cambie de
		// base de datos y que no se
		// borre lo existente, ya que puede crear grandes conflictos, tanto como
		// en el TPV como en el servidor
	}

	private class UpdateVersions extends AsyncTask<Void, Integer, Integer> {
		private String apkVersion;
		private Integer apkVersionCode;

		@Override
		protected Integer doInBackground(Void... params) {
			try {
				PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(),
						PackageManager.GET_META_DATA);
				apkVersion = pInfo.versionName;
				apkVersionCode = pInfo.versionCode;
			} catch (NameNotFoundException e) {
				Log.e(Constants.TAG, "Not installed?", e);
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			tvVersion.setText(apkVersion + " - " + apkVersionCode);
		}
	}

}

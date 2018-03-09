package es.guadaltech.odoo;

import java.net.ConnectException;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.NumberUtils;
import es.guadaltech.odoo.misc.view.TypefaceSpan;

/**
 * Activity which displays a login screen to the user
 */

public class LoginActivity extends Activity {
	private boolean D = true;
	// Define views
	private String mUsername;
	private String mPassword;
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private SharedPreferences prefs;

	// ERROR CODES
	public static final int ERROR_CONNECTION_TIME_OUT = 0xFF1;
	public static final int ERROR_AUTH = 0xFF2;
	public static final int ERROR_UNKNOWN = 0xFF3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		setTheme(R.style.AppTheme);
		loadCustomActionBar();

		// Set up views
		mUsernameView = (EditText) findViewById(R.id.et_login_username);
		mPasswordView = (EditText) findViewById(R.id.et_login_password);
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.tv_login_status_message);

		// Load preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		findViewById(R.id.btn_login_signin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid username, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		if (D && mUsername.equals("") && mPassword.equals("")) {
			mUsername = "admin";
			mPassword = "admin";
		}

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			try {
				login();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void login() throws JSONException {
		AsyncHttpClient client = new AsyncHttpClient();

		PersistentCookieStore myCookieStore = new PersistentCookieStore(LoginActivity.this);
		myCookieStore.clear(); // Fixes issue #1, login with cookie
		client.setCookieStore(myCookieStore);

		JSONObject json = new JSONObject();
		json.put("user", mUsername);
		json.put("pass", mPassword);
		json.put("db",
				NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_DATABASE, Constants.OERP_DB_DEFAULT)));
		json.put("url", NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_IP,
				Constants.OERP_SERVER_DEFAULT)));
		json.put("port", NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_PORT,
				Constants.OERP_SERVER_PORT_DEFAULT)));

		String jsonEncoded = json.toString();

		Log.i("OpenERP", "##LOGIN JSON REQUEST##-> " + jsonEncoded);

		RequestParams p = new RequestParams();
		p.put("json", jsonEncoded);

		String mdwLogin = NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_MIDDLEWARE_IP,
				Constants.OERP_MIDDLEWARE_DEFAULT)) + "/login";

		Log.i(Constants.TAG, "login->" + mdwLogin);

		client.post(mdwLogin, p, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Log.i(Constants.TAG, "Login success!");
			}

			@Override
			protected void handleSuccessMessage(int httpCode, String response) {
				super.handleSuccessMessage(httpCode, response);
				switch (httpCode) {
				case HttpStatus.SC_OK:
					Log.i("Login", "HTTP code OK");
					// Save username into shared preferences
					Editor editor = prefs.edit();
					editor.putString(Constants.PREFS_KEY_USER, NumberUtils.encode(mUsername));
					editor.putString(Constants.PREFS_KEY_PASSWORD, NumberUtils.encode(mPassword));
					editor.commit();

					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
					break;
				case HttpStatus.SC_UNAUTHORIZED:
					Log.e("Login", "HTTP code UNAUTHORIZED");
					loginError(ERROR_AUTH);
					break;
				default:
					Log.e("Login", "HTTP code NOT 200 NOR 401");
					loginError(ERROR_UNKNOWN);
					break;
				}
			}

			@Override
			public void onFailure(Throwable thr, String response) {
				Log.i(Constants.TAG, "Login failure! ##RESPONSE##-> " + thr.getMessage());
				if (thr instanceof ConnectException) {
					loginError(ERROR_CONNECTION_TIME_OUT);
					return;
				}
				if ("unauthorized".equalsIgnoreCase(thr.getMessage())) {
					loginError(ERROR_AUTH);
					return;
				}
				// Default error
				loginError(ERROR_UNKNOWN);
			}

			@Override
			public void onStart() {
				Log.i(Constants.TAG, "### Start login ###");
			}

			@Override
			public void onFinish() {
				Log.i(Constants.TAG, "### End login ###");
			}
		});

	}

	public void loginError(int code) {
		showProgress(false);
		if (code == ERROR_AUTH) {
			mUsernameView.setError("Usuario o contraseña incorrectos");
			mPasswordView.setText("");
			return;
		}
		if (code == ERROR_CONNECTION_TIME_OUT) {
			mPasswordView.setText("");
			Crouton.makeText(LoginActivity.this,
					"Error accediendo al servidor, ¿hay conexiones disponibles?", Style.ALERT).show();
			return;
		}
		if (code == ERROR_UNKNOWN) {
			mPasswordView.setText("");
			Crouton.makeText(LoginActivity.this,
					"Error general de la aplicación, vuelva a intentarlo más tarde", Style.ALERT).show();
			return;
		}
	}

	// ========================= Menus stuff ===========================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.action_settings:
			intent = new Intent(LoginActivity.this, SettingsActivity.class);
			intent.putExtra("from", "login");
			startActivity(intent);
			return true;
		case R.id.action_pass:
			intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
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
				+ getString(R.string.ab_title_login));
		s.setSpan(new TypefaceSpan(this, "RobotoSlab-Bold", true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		ActionBar actionBar = getActionBar();

		// Background
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background));

		// Icon
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setDisplayShowHomeEnabled(true);

		// Title
		actionBar.setTitle(s);
		actionBar.setDisplayShowTitleEnabled(true);
	}

}
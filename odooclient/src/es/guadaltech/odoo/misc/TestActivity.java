package es.guadaltech.odoo.misc;

import android.app.Activity;

//Log-in activity
public class TestActivity extends Activity {

	// private EditText etUser, etPass, etDb, etUrl, etPort, etMethod, etModel;
	// private Button btnLogin, btnTest;
	// private TextView tvConsole;
	//
	// @Override
	// protected void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// setContentView(R.layout.activity_test);
	//
	// // Give value to views
	// etUser = (EditText) findViewById(R.id.et_server_username);
	// etPass = (EditText) findViewById(R.id.et_server_password);
	// etDb = (EditText) findViewById(R.id.et_server_database);
	// etUrl = (EditText) findViewById(R.id.et_server_url);
	// etPort = (EditText) findViewById(R.id.et_server_port);
	// btnLogin = (Button) findViewById(R.id.btn_login);
	// btnTest = (Button) findViewById(R.id.btn_test);
	// etMethod = (EditText) findViewById(R.id.et_server_method);
	// etModel = (EditText) findViewById(R.id.et_server_model);
	// tvConsole = (TextView) findViewById(R.id.tv_console);
	//
	// // Set default values in text fields
	// etUser.setText(Constants.OERP_USER_DEFAULT);
	// etPass.setText(Constants.OERP_PASS_DEFAULT);
	// etDb.setText(Constants.OERP_DB_DEFAULT);
	// etUrl.setText(Constants.OERP_SERVER_DEFAULT);
	// etPort.setText(Constants.OERP_SERVER_PORT_DEFAULT);
	// etMethod.setText(Constants.OPENERP_DEFAULT_METHOD);
	// etModel.setText(Constants.OPENERP_DEFAULT_MODEL);
	//
	// // Set onClickListeners...
	// btnLogin.setOnClickListener(new View.OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// AsyncHttpClient client = new AsyncHttpClient();
	//
	// PersistentCookieStore myCookieStore = new
	// PersistentCookieStore(TestActivity.this);
	// client.setCookieStore(myCookieStore);
	//
	// JSONObject json = new JSONObject();
	// try {
	// json.put("user", etUser.getText().toString());
	// json.put("pass", etPass.getText().toString());
	// json.put("db", etDb.getText().toString());
	// json.put("url", etUrl.getText().toString());
	// json.put("port", etPort.getText().toString());
	// json.put("method", etMethod.getText().toString());
	// json.put("model", etModel.getText().toString());
	// } catch (JSONException e1) {
	// e1.printStackTrace();
	// }
	//
	// String jsonEncoded = json.toString();
	//
	// Log.i("OpenERP", "##JSON REQUEST##-> " + jsonEncoded);
	//
	// RequestParams p = new RequestParams();
	// p.put("json", jsonEncoded);
	//
	// client.post(Constants.OERP_MIDDLEWARE_DEFAULT + "/json", p, new
	// AsyncHttpResponseHandler() {
	// @Override
	// public void onSuccess(String response) {
	// Log.i("OpenERP", "Sucess! ##RESPONSE##-> " + response);
	// tvConsole.setText(response);
	// }
	//
	// @Override
	// public void onFailure(Throwable arg0, String response) {
	// Log.i("OpenERP", "Failure! ##RESPONSE##-> " + response);
	// tvConsole.setText(response);
	// try {
	// throw arg0;
	// } catch (Throwable e) {
	// e.printStackTrace();
	// }
	// }
	//
	// @Override
	// public void onStart() {
	// Log.i("OpenERP", "Start!");
	// }
	//
	// @Override
	// public void onFinish() {
	// Log.i("OpenERP", "Finish!");
	// }
	// });
	// }
	// });
	//
	// btnTest.setOnClickListener(new View.OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// AsyncHttpClient client = new AsyncHttpClient();
	//
	// client.get(Constants.OERP_MIDDLEWARE_DEFAULT + "/test", new
	// AsyncHttpResponseHandler() {
	// @Override
	// public void onSuccess(String response) {
	// Log.i("OpenERP", "###TEST### Sucess! ##RESPONSE##-> " + response);
	// tvConsole.setText("###TEST###" + response);
	// }
	//
	// @Override
	// public void onFailure(Throwable arg0, String response) {
	// Log.i("OpenERP", "###TEST### Failure! ##RESPONSE##-> " + response);
	// tvConsole.setText("###TEST###" + response);
	// }
	//
	// @Override
	// public void onStart() {
	// Log.i("OpenERP", "###TEST### Start!");
	// }
	//
	// @Override
	// public void onFinish() {
	// Log.i("OpenERP", "###TEST### Finish!");
	// }
	// });
	// }
	// });
	// }
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }

}

package es.guadaltech.odoo.misc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;

import com.google.gson.Gson;

public class JSONBuilder {

	private SharedPreferences prefs;

	public JSONBuilder(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	/**
	 * 
	 * @param model
	 *            OpenERP model
	 * @param method
	 *            OpenERP method
	 * @param params
	 *            filtering params, null for no filtering
	 * @return list of objects from server
	 * @throws JSONException
	 */
	public String get(String model, String method, JSONArray params) throws JSONException {

		JSONObject json = new JSONObject();
		json.put("user", NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_USER, "")));
		json.put("pass", NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_PASSWORD, "")));
		json.put("db",
				NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_DATABASE, Constants.OERP_DB_DEFAULT)));
		json.put("url", NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_IP,
				Constants.OERP_SERVER_DEFAULT)));
		json.put("port", NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_PORT,
				Constants.OERP_SERVER_PORT_DEFAULT)));
		json.put("model", model);

		json.put("method", method);

		JSONArray outerParams = new JSONArray();

		if (params != null) {
			outerParams.put(params);
		}

		json.put("filter", outerParams);

		return json.toString();
	}

	public String getAssociative(String model, String method, AssociativeSalesLine line) throws JSONException {

		JSONAssoc a = new JSONAssoc(NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_USER, "")),
				NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_PASSWORD, "")),
				NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_DATABASE, Constants.OERP_DB_DEFAULT)),
				NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_SERVER_IP,
						Constants.OERP_SERVER_DEFAULT)), NumberUtils.decode(prefs.getString(
						Constants.PREFS_KEY_SERVER_PORT, Constants.OERP_SERVER_PORT_DEFAULT)), model, method,
				line);
		Gson gson = new Gson();
		return gson.toJson(a);
	}

	@SuppressWarnings("unused")
	private class JSONAssoc {
		private String user;
		private String pass;
		private String db;
		private String url;
		private String port;
		private String model;
		private String method;
		private AssociativeSalesLine associative;

		public JSONAssoc(String user, String pass, String db, String url, String port, String model,
				String method, AssociativeSalesLine associative) {
			super();
			this.user = user;
			this.pass = pass;
			this.db = db;
			this.url = url;
			this.port = port;
			this.model = model;
			this.method = method;
			this.associative = associative;
		}

	}

}

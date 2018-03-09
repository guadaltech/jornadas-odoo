package es.guadaltech.odoo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import es.guadaltech.odoo.dao.OrderDAO;
import es.guadaltech.odoo.dao.OrderLineDAO;
import es.guadaltech.odoo.dao.ProductDAO;
import es.guadaltech.odoo.misc.AssociativeSalesLine;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.JSONBuilder;
import es.guadaltech.odoo.misc.NumberUtils;
import es.guadaltech.odoo.misc.RowOrderLine;
import es.guadaltech.odoo.misc.RowOrderLineAdapter;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.model.Order;
import es.guadaltech.odoo.model.OrderLine;
import es.guadaltech.odoo.model.Product;
import es.guadaltech.odoo.model.Order.STATE;

public class TPVActivity extends Activity implements OnClickListener {

	private TextView mTvTotal;
	private ListView mListView;
	private List<RowOrderLine> items;
	private RowOrderLineAdapter adapter;
	private ProductDAO pdao;
	private SharedPreferences prefs;
	private String orderReference;
	private OrderDAO orderDAO;
	private OrderLineDAO orderLineDAO;
	private String partnerID;// unused??
	private String partnerName;
	private String userName;
	private TextView tvPartner;
	private TextView tvUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tpv);
		loadCustomActionBar();

		// Set fields...
		mTvTotal = (TextView) findViewById(R.id.tv_tpv_total);
		mTvTotal.setText("0 €");
		mListView = (ListView) findViewById(R.id.lv_tpv_orderline);
		pdao = new ProductDAO();
		orderDAO = new OrderDAO();
		orderLineDAO = new OrderLineDAO();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Setup listview...
		items = new ArrayList<RowOrderLine>();
		adapter = new RowOrderLineAdapter(TPVActivity.this, items);
		mListView.setAdapter(adapter);

		partnerID = prefs.getString("partner_id", "");
		partnerName = prefs.getString("partner_name", "");
		userName = NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_USER, ""));

		tvUser = (TextView) findViewById(R.id.tv_tpv_vendedor);
		tvUser.setText(userName);
		tvPartner = (TextView) findViewById(R.id.tv_tpv_cliente);
		tvPartner.setText(partnerName);

		// Set click listeners...
		((ImageView) findViewById(R.id.btn_tpv_add)).setOnClickListener(this);
		((ImageView) findViewById(R.id.btn_tpv_add_barcode)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_tpv_facturar)).setOnClickListener(this);
		((Button) findViewById(R.id.btn_tpv_pagar)).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_tpv_add:
			// addMock();
			Intent intent = new Intent(TPVActivity.this, ShowProductListActivity.class);
			intent.putExtra(Constants.INTENT_SHOW_PRODUCT_LIST_TO_SELECT, true);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			break;
		case R.id.btn_tpv_add_barcode:
			launchScanner();
			break;
		case R.id.btn_tpv_pagar:// Temporalmente -> Crear venta
			try {
				createSale();
			} catch (JSONException e) {
				Log.e(Constants.TAG, "Error al crear venta");
				e.printStackTrace();
			}
			Toast.makeText(TPVActivity.this, "Se está creando la venta...", Toast.LENGTH_LONG).show();
			break;
		case R.id.btn_tpv_facturar:// Temporalmente -> Crear líneas
			try {
				createLines();
			} catch (JSONException e) {
				Log.e(Constants.TAG, "Error al crear líneas");
				e.printStackTrace();
			}
			Toast.makeText(TPVActivity.this, "Se están creando las líneas...", Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.i(Constants.TAG, "onNewIntent");
		String oerpid = intent.getStringExtra(Constants.INTENT_PRODUCT_FROM_LIST);
		if (oerpid != null) {
			RowOrderLine[] listProducts = new RowOrderLine[items.size()];

			items.toArray(listProducts);
			for (int i = 0; i < listProducts.length; i++) {
				if (listProducts[i].getProductId().toString().equals(oerpid)) {
					items.get(i).setProductQty(items.get(i).getProductQty() + 1);
					adapter.notifyDataSetChanged();
					updateTotalPrice();
					return;
				}
			}
			addProductByOpenERPID(oerpid);
			updateTotalPrice();
		} else {
			Log.e(Constants.TAG, "Received null openerp id");
		}
	}

	protected void createFullSale() {
		try {
			createSale();
			createLines();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void createSale() throws JSONException {
		AsyncHttpClient client = new AsyncHttpClient();

		PersistentCookieStore myCookieStore = new PersistentCookieStore(TPVActivity.this);
		client.setCookieStore(myCookieStore);

		JSONBuilder jsonBuilder = new JSONBuilder(prefs);
		String json = null;

		try {
			JSONArray metadata = null;// Inserts null deliberately
			// Create an empty order, must return order id
			json = jsonBuilder.get("pos.order", "create", metadata);
		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}

		Log.i("OpenERP", "## Create sale request -> " + json);

		RequestParams p = new RequestParams();
		p.put("json", json);

		client.post(NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_MIDDLEWARE_IP,
				Constants.OERP_MIDDLEWARE_DEFAULT)) + "/json", p,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						Log.i(Constants.TAG, "### Sucess sale create ##RESPONSE##-> " + response);
						if (response == null) {
							Log.e(Constants.TAG, "Null response");

							return;
						}

						if (!NumberUtils.isInteger(response)) {
							Log.e(Constants.TAG, "Numeric response expected");
							return;
						}

						// Add to history
						orderReference = response;

						// Save current order id into prefs, for further adding
						// lines to it
						Integer orderId = Integer.valueOf(response);
						Editor edit = prefs.edit();
						edit.putInt(Constants.PREFS_KEY_CURRENT_ORDER_ID, orderId);
						edit.commit();
					}

					@Override
					public void onFailure(Throwable arg0, String response) {
						Log.i(Constants.TAG, "### Failure creating order ##RESPONSE##-> " + response);
						Toast.makeText(TPVActivity.this, response, Toast.LENGTH_LONG).show();
					}

					@Override
					public void onStart() {
						Log.i(Constants.TAG, "### Start creating order");
						setProgressBarIndeterminateVisibility(true);
					}

					@Override
					public void onFinish() {
						Log.i(Constants.TAG, "### End creating order");
						setProgressBarIndeterminateVisibility(false);
					}
				});

	}

	protected void createLines() throws JSONException {

		// Get current order id
		int orderId = -1;
		orderId = prefs.getInt(Constants.PREFS_KEY_CURRENT_ORDER_ID, -1);
		if (orderId == -1) {
			Log.e(Constants.TAG, "Error getting current order id");
			return;
		}

		// Prepare for communication with server
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(TPVActivity.this);
		client.setCookieStore(myCookieStore);
		JSONBuilder jsonBuilder = new JSONBuilder(prefs);
		String json = null;

		// 'order_id','name','product_id','price_unit','tax_id','type','product_uom','product_uom_qty','discount'

		// Para empezar, agregamos únicamente el primer producto de la lista
		// (asegurarse de que existe alguno ;))
		RowOrderLine element = null;
		AssociativeSalesLine asl = null;
		List<OrderLine> orderLines = new ArrayList<OrderLine>();
		OrderLine newOrderLine = null;
		List<AssociativeSalesLine> asls = new ArrayList<AssociativeSalesLine>();
		for (int i = 0; i < adapter.getCount(); i++) {
			element = adapter.getItem(i);
			asl = new AssociativeSalesLine(element.getProductId(), orderId, element.getProductPrice(),
					element.getProductQty());
			asls.add(asl);
			// creating orderlines
			newOrderLine = new OrderLine();
			newOrderLine.setOrderID(orderId);
			newOrderLine.setProductID(element.getProductId());
			newOrderLine.setPriceUnit(element.getProductPrice().floatValue());
			newOrderLine.setQuantity(element.getProductQty());
			newOrderLine.setDiscount(element.getProductDiscount());

			orderLines.add(newOrderLine);

		}
		for (AssociativeSalesLine line : asls) {
			try {
				json = jsonBuilder.getAssociative("pos.order.line", "create", line);
			} catch (JSONException e) {
				Log.e(Constants.TAG, e.getMessage());
			}

			Log.i("OpenERP", "## Create order line request -> " + json);

			RequestParams p = new RequestParams();
			p.put("json", json);

			client.post(NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_MIDDLEWARE_IP,
					Constants.OERP_MIDDLEWARE_DEFAULT)) + "/json", p,
					new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							// Must return line id (integer)
							Log.i(Constants.TAG, "### Sucess order line ##RESPONSE##-> " + response);
						}

						@Override
						public void onFailure(Throwable arg0, String response) {
							Log.i(Constants.TAG, "### Failure creating order line ##RESPONSE##-> " + response);
							Toast.makeText(TPVActivity.this, response, Toast.LENGTH_LONG).show();
						}

						@Override
						public void onStart() {
							Log.i(Constants.TAG, "### Start creating order line");
							setProgressBarIndeterminateVisibility(true);
						}

						@Override
						public void onFinish() {
							Log.i(Constants.TAG, "### End creating order line");
							setProgressBarIndeterminateVisibility(false);
						}
					});
		}

		// add to database
		Order newOrder = new Order();
		newOrder.setName(orderReference);
		newOrder.setId(orderId);
		newOrder.setPartnerID(Integer.valueOf(prefs.getString("partner_id", "")));
		newOrder.setShopID(prefs.getInt(Constants.CP_TYPE_DIARIO, 0));
		newOrder.setDateOrder(System.currentTimeMillis());
		newOrder.setTotal((Float) mTvTotal.getTag());
		newOrder.setState(STATE.NUEVO);

		newOrder.setUserID(0);
		// newOrder.setUserID(NumberUtils.decode(prefs.getString(Constants.PREFS_KEY_USER,
		// "")));

		// Adding order to db
		orderDAO.insert(this, newOrder);
		// Adding orderlines to db
		for (OrderLine orderLine : orderLines) {
			orderLine.setOrderID(orderId);
			orderLineDAO.insert(this, orderLine);
		}

	}

	// ================= Barcode scanner stuff ====================

	// Scans all types of codes
	public void launchScanner() {
		if (isCameraAvailable()) {
			Intent intent = new Intent(this, ZBarScannerActivity.class);
			startActivityForResult(intent, Constants.INTENT_ZBAR_SCANNER_REQUEST);
		} else {
			Toast.makeText(this, "La cámara trasera no está disponible", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	public boolean isCameraAvailable() {
		PackageManager pm = getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case Constants.INTENT_ZBAR_SCANNER_REQUEST:
			Log.i(Constants.TAG, "onActivityResult ZBAR");
			if (resultCode == RESULT_OK) {
				// Code read
				String result = data.getStringExtra(ZBarConstants.SCAN_RESULT);
				Log.i(Constants.TAG, "Scanner result -> " + result);
				Crouton.makeText(this, result, Style.ALERT).show();
				Product p = null;
				try {
					p = pdao.searchEan13(TPVActivity.this, result).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}

				if (p != null) {
					RowOrderLine r = new RowOrderLine(null, p.getId(), p.getName(), p.getListPrice(), 0f, 1f);
					items.add(0, r);
					adapter.notifyDataSetChanged();
					updateTotalPrice();
				} else {
					Crouton.makeText(this, "No se ha encontrado ningún producto con esa numeración",
							Style.ALERT).show();
					Log.e(Constants.TAG, "No se ha encontrado un producto con esa numeración");
				}
			} else if (resultCode == RESULT_CANCELED && data != null) {
				String error = data.getStringExtra(ZBarConstants.ERROR_INFO);
				if (!TextUtils.isEmpty(error)) {
					Crouton.makeText(this, error, Style.ALERT).show();
				}
			}
			break;
		}
	}

	public void updateTotalPrice() {
		float totalPrice = 0;
		int noOfItems = adapter.getCount();
		for (int i = 0; i < noOfItems; i++) {
			totalPrice += adapter.getItem(i).getProductQty() * adapter.getItem(i).getProductPrice()
					* (1 - (adapter.getItem(i).getProductDiscount() / 100));
		}
		mTvTotal.setText(String.valueOf(totalPrice) + " €");
		mTvTotal.setTag(totalPrice);
	}

	public void addProductByOpenERPID(String oerpid) {
		Product p = null;
		try {
			p = pdao.searchByID(TPVActivity.this, oerpid).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		if (p != null) {
			RowOrderLine row = new RowOrderLine(null, p.getId(), p.getName(), p.getListPrice(), 0f, 1f);
			items.add(0, row);
			adapter.notifyDataSetChanged();
		} else {
			Log.e(Constants.TAG, "Error, trying to add null product to the list");
		}
	}

	private void loadCustomActionBar() {
		// Custom typeface
		SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
				+ getString(R.string.ab_title_tpv));
		s.setSpan(new TypefaceSpan(this, "RobotoSlab-Bold", true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

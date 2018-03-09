package es.guadaltech.odoo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import es.guadaltech.odoo.dao.OrderDAO;
import es.guadaltech.odoo.dao.OrderLineDAO;
import es.guadaltech.odoo.dao.ProductDAO;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.GenericList;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.model.Order;
import es.guadaltech.odoo.model.OrderLine;
import es.guadaltech.odoo.model.Product;

public class HistoryShowDetailsActivity extends GenericList implements OnItemClickListener {

	Order order;
	List<OrderLine> orders;
	private CustomAdapter adapter;
	private Context context;
	private TextView tvTitle;
	private TextView tvTotal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_showdetail);
		Bundle bundle = getIntent().getExtras();
		Integer code = bundle.getInt(Constants.INTENT_KEY_CODE);

		orders = new ArrayList<OrderLine>();

		try {
			getOrder(code.toString());
			getOrderLine(code.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		adapter = new CustomAdapter(this);
		setListAdapter(adapter);
		context = this;
		adapter.notifyDataSetChanged();
		tvTitle = (TextView) findViewById(R.id.tv_history_title);
		tvTitle.setText(getString(R.string.history_referencia) + order.getId());

		tvTotal = (TextView) findViewById(R.id.tv_history_total);
		tvTotal.setText(order.getTotal() + getString(R.string.common_currency));
		loadCustomActionBar();
		getListView().setOnItemClickListener(this);
		getListView().setOnScrollListener(new EndlessScrollListener());

	}

	private void getOrderLine(String code) {
		OrderLineDAO dao = new OrderLineDAO();
		try {
			orders = dao.searchById(this.getApplicationContext(), code).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void getOrder(String id) throws JSONException {
		OrderDAO dao = new OrderDAO();
		try {
			order = dao.searchByID(this.getApplicationContext(), id).get();

			// tv2.setText(p.getName());
			// tv3.setText((p.getAdresses().get(0).getEmail()));
			// tv4.setText((p.getAdresses().get(0).getMobile()));

		} catch (InterruptedException e) {
			Log.e(Constants.TAG, "Error al hacer la peticion a la DB");
		} catch (ExecutionException e) {
			Log.e(Constants.TAG, "Error al hacer la peticion a la DB");
		} catch (NullPointerException e) {
			Log.e(Constants.TAG, "No hay producto con ese ID");
		} catch (Exception e) {
			Log.e(Constants.TAG, "Me cago en los muertos del bitmap");
		}
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

	private void loadCustomActionBar() {
		// Custom typeface
		SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
				+ getString(R.string.ab_title_details));
		s.setSpan(new TypefaceSpan(this, getString(R.string.common_font), true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		// Update the action bar title with the TypefaceSpan instance
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);

		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}

	@Override
	protected void getServerItems() throws JSONException {
	}

	@Override
	protected void getOfflineItems(Integer pagination) {
	}

	public class CustomAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public CustomAdapter(Context c) {
			inflater = LayoutInflater.from(c);
		}

		public int getCount() {
			return orders.size();
		}

		public OrderLine getItem(int position) {
			return orders.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) { // if it's not recycled, initialize some attrs
				v = inflater.inflate(R.layout.row_history_order_line, null);
			} else {

			}

			OrderLine item = orders.get(position);
			if (item != null) {
				// Set text views
				Product p = null;
				try {
					p = (new ProductDAO().searchByID(context, item.getProductID().toString())).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				((TextView) v.findViewById(R.id.tv_row_history_name)).setText(p.getName());
				((TextView) v.findViewById(R.id.tv_row_history_qty)).setText(item.getQuantity().toString());
				((TextView) v.findViewById(R.id.tv_row_history_priceunit)).setText(item.getPriceUnit()
						.toString());
				((TextView) v.findViewById(R.id.tv_row_history_discount)).setText(item.getDiscount() + getString(R.string.common_percent));
				Float total = item.getPriceUnit() * item.getQuantity();
				((TextView) v.findViewById(R.id.tv_row_history_priceTotal)).setText(total.toString());

			}
			return v;
		}
	}

}

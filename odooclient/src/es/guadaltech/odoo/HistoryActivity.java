package es.guadaltech.odoo;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import es.guadaltech.odoo.dao.OrderDAO;
import es.guadaltech.odoo.dao.PartnerDAO;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.GenericList;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.model.Order;

public class HistoryActivity extends GenericList implements OnItemClickListener {
	private CustomAdapter adapter;
	private OrderDAO odao;
	private List<Order> orders;
	private Context ctx;
	private final Integer MAX_RESULTS = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadCustomActionBar();
		setContentView(R.layout.activity_showlist);
		ctx = this;
		odao = new OrderDAO();
		adapter = new CustomAdapter(this);
		orders = new ArrayList<Order>();
		getListView().setOnItemClickListener(this);
		getListView().setOnScrollListener(new EndlessScrollListener());
		setListAdapter(adapter);
		getOfflineItems(10);

	}

	@Override
	protected void getOfflineItems(Integer pagination) {

		try {
			orders = odao.getLastOrders(HistoryActivity.this, MAX_RESULTS).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		adapter.notifyDataSetChanged();
	}

	// ========================= Custom GUI =============================

	private void loadCustomActionBar() {
		// Custom typeface for action bar title
		SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
				+ getString(R.string.ab_title_history));
		s.setSpan(new TypefaceSpan(this, getString(R.string.common_font), true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		getActionBar().setTitle(s);
	}

	public class CustomAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public CustomAdapter(Context c) {
			inflater = LayoutInflater.from(c);
		}

		public int getCount() {
			return orders.size();
		}

		public Order getItem(int position) {
			return orders.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) { // if it's not recycled, initialize some attrs
				v = inflater.inflate(R.layout.row_listcellitem_history, null);
			} else {

			}

			Order item = orders.get(position);
			if (item != null) {
				// Set text views
				((TextView) v.findViewById(R.id.tv_cell_history_1)).setText(getString(R.string.history_id) + item.getId());
				// not implemented
				PartnerDAO partnerDao = new PartnerDAO();
				try {
					((TextView) v.findViewById(R.id.tv_cell_history_2)).setText(getString(R.string.history_cliente)
							+ partnerDao.searchByID(ctx, item.getPartnerID().toString()).get().getName());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.common_date_format), Locale.FRANCE);
				String fecha = sdf.format(new Date(item.getDateOrder()));
				((TextView) v.findViewById(R.id.tv_cell_history_3)).setText(fecha);
				((TextView) v.findViewById(R.id.tv_cell_history_4)).setText(getString(R.string.history_total) + item.getTotal());
			}
			return v;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Intent intent = new Intent(HistoryActivity.this, HistoryShowDetailsActivity.class);

		intent.putExtra(Constants.INTENT_KEY_CODE, orders.get(position).getId());
		startActivity(intent);

	}

	@Override
	protected void getServerItems() throws JSONException {
		// TODO Auto-generated method stub

	}

}

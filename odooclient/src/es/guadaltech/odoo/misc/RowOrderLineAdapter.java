package es.guadaltech.odoo.misc;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import es.guadaltech.odoo.R;
import es.guadaltech.odoo.TPVActivity;

public class RowOrderLineAdapter extends BaseAdapter {

	private List<RowOrderLine> data;
	private LayoutInflater inflater;
	private Context context;

	public RowOrderLineAdapter(Context context, List<RowOrderLine> data) {
		this.context = context;
		this.data = data;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public RowOrderLine getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO check this!
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final RowOrderLine item = getItem(position);

		// Initialize views
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.row_order_line, parent, false);
			holder.pDiscount = (TextView) convertView.findViewById(R.id.tv_row_orderline_discount);
			holder.pName = (TextView) convertView.findViewById(R.id.tv_row_orderline_name);
			holder.pPriceTotal = (TextView) convertView.findViewById(R.id.tv_row_orderline_priceTotal);
			holder.pPriceUnit = (TextView) convertView.findViewById(R.id.tv_row_orderline_priceunit);
			holder.pQty = (TextView) convertView.findViewById(R.id.tv_row_orderline_qty);
			holder.qtyMinus = (ImageView) convertView.findViewById(R.id.btn_row_orderline_qty_minus);
			holder.qtyPlus = (ImageView) convertView.findViewById(R.id.btn_row_orderline_qty_plus);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Give values to view
		holder.pName.setText(item.getProductName());
		holder.pPriceUnit.setText(item.getProductPrice().toString() + " €");
		if (item.getProductDiscount() != 0F) {
			holder.pDiscount.setText(item.getProductDiscount().toString() + " %");
		} else {
			holder.pDiscount.setText(context.getString(R.string.row_orderline_discount));
		}
		holder.pQty.setText(item.getProductQty().toString());
		holder.pPriceTotal
				.setText(String.valueOf((item.getProductPrice() * (1 - (item.getProductDiscount() / 100)))
						* item.getProductQty()));

		holder.qtyMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				float qty = item.getProductQty();
				if (qty > 1) {
					item.setProductQty(qty - 1);
				}
				refreshList();
			}
		});

		holder.qtyPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				float qty = item.getProductQty();
				item.setProductQty(qty + 1);
				refreshList();
			}
		});

		holder.pDiscount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder dBuilder = new AlertDialog.Builder(context);
				dBuilder.setTitle("Escoger descuento");
				final EditText input = new EditText(context);
				input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				dBuilder.setView(input);
				dBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String discount = input.getText().toString();
						if (NumberUtils.isDouble(discount)) {
							float fDiscount = Float.parseFloat(discount);
							if (fDiscount >= 0 && fDiscount <= 100) {
								item.setProductDiscount(fDiscount);
								refreshList();
							} else {
								Toast.makeText(context, "El valor debe estar entre 0 y 100",
										Toast.LENGTH_SHORT).show();
								input.setText("");
							}
						} else {
							Toast.makeText(context, "Introduzca un valor numérico", Toast.LENGTH_SHORT)
									.show();
							input.setText("");
						}
					}
				});
				dBuilder.show();
			}
		});

		holder.pPriceUnit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder pBuilder = new AlertDialog.Builder(context);
				pBuilder.setTitle("Escoger precio unitario");
				final EditText input = new EditText(context);
				input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
				pBuilder.setView(input);
				pBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strPrice = input.getText().toString();
						if (NumberUtils.isDouble(strPrice)) {
							Float uPrice = Float.parseFloat(strPrice);
							item.setProductPrice(uPrice.doubleValue());
							refreshList();
						} else {
							Toast.makeText(context, "Introduzca un valor numérico", Toast.LENGTH_SHORT)
									.show();
							input.setText("");
						}
					}
				});
				pBuilder.show();
			}
		});

		return convertView;
	}

	static class ViewHolder {
		TextView pName;
		TextView pPriceUnit;
		TextView pPriceTotal;
		TextView pQty;
		TextView pDiscount;
		ImageView qtyPlus;
		ImageView qtyMinus;
	}

	@Override
	public int getItemViewType(int position) {
		// Only one type here
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	public void refreshList() {
		this.notifyDataSetChanged();
		TPVActivity a = (TPVActivity) context;
		a.updateTotalPrice();
	}

}
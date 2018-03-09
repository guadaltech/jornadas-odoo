package es.guadaltech.odoo;

import java.util.concurrent.ExecutionException;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import es.guadaltech.odoo.dao.PartnerDAO;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.model.Partner;

public class PartnerShowDetailsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_partner_showdetails);

		loadCustomActionBar();

		Bundle bundle = getIntent().getExtras();
		String code = bundle.getString(Constants.INTENT_KEY_CODE);

		try {
			getPartner(code);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void getPartner(String id) throws JSONException {
		PartnerDAO dao = new PartnerDAO();
		try {
			final Partner p = dao.searchByID(this.getApplicationContext(), id).get();

			checkAndMakeVisibleTV(R.id.ll_partner_name, R.id.tv_partner_name, R.id.separator_partner_name,
					p.getName());
			checkAndMakeVisibleTV(R.id.ll_partner_mobile, R.id.tv_partner_mobile,
					R.id.separator_partner_mobile, p.getMobile());
			checkAndMakeVisibleTV(R.id.ll_partner_phone, R.id.tv_partner_phone, R.id.separator_partner_phone,
					p.getPhone());
			checkAndMakeVisibleTV(R.id.ll_partner_city, R.id.tv_partner_city, R.id.separator_partner_city,
					p.getCity());
			checkAndMakeVisibleTV(R.id.ll_partner_function, R.id.tv_partner_function,
					R.id.separator_partner_function, p.getFunction());
			checkAndMakeVisibleCB(R.id.ll_partner_iscustomer, R.id.cb_partner_iscustomer,
					R.id.separator_partner_iscustomer, p.isCustomer());
			checkAndMakeVisibleCB(R.id.ll_partner_issupplier, R.id.cb_partner_issupplier,
					R.id.separator_partner_issupplier, p.isSupplier());
			if (p.getEmails() != null && p.getEmails().length > 0)
				checkAndMakeVisibleTV(R.id.ll_partner_emails, R.id.tv_partner_emails,
						R.id.separator_partner_emails, p.getEmails()[0]);

			// Gives functionality to phones and mails
			if (p.getMobile() != null && !"false".equals(p.getMobile())) {
				findViewById(R.id.ll_partner_mobile).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + p.getMobile()));
						startActivity(intent);
					}
				});
			}
			if (p.getPhone() != null && !"false".equals(p.getPhone())) {
				findViewById(R.id.ll_partner_phone).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + p.getPhone()));
						startActivity(intent);
					}
				});
			}
			if (p.getEmails() != null && !"false".equals(p.getEmails()[0])) {
				findViewById(R.id.ll_partner_emails).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
								p.getEmails()[0], null));
						startActivity(Intent.createChooser(emailIntent, "Enviar correo"));
					}
				});
			}
		} catch (InterruptedException e) {
			Log.e(Constants.TAG, "Error al hacer la peticion a la DB");
			e.printStackTrace();
		} catch (ExecutionException e) {
			Log.e(Constants.TAG, "Error al hacer la peticion a la DB");
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e(Constants.TAG, "No hay producto con ese ID");
			e.printStackTrace();
		}
	}

	private void checkAndMakeVisibleTV(int viewtomakevisibleid, int textviewid, int separatorid, String text) {
		if (text != null) {
			if (!"false".equals(text)) {
				((TextView) findViewById(textviewid)).setText(text);
				findViewById(viewtomakevisibleid).setVisibility(View.VISIBLE);
				findViewById(separatorid).setVisibility(View.VISIBLE);
			}
		}
	}

	private void checkAndMakeVisibleCB(int viewtomakevisibleid, int checkboxid, int separatorid,
			Boolean checked) {
		if (checked != null) {
			((CheckBox) findViewById(checkboxid)).setChecked(checked);
			findViewById(viewtomakevisibleid).setVisibility(View.VISIBLE);
			findViewById(separatorid).setVisibility(View.VISIBLE);
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

}

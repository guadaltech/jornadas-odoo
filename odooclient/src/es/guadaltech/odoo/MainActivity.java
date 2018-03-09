package es.guadaltech.odoo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.view.TypefaceSpan;

public class MainActivity extends Activity implements OnClickListener {

	// TODO check login cookie lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		loadCustomActionBar();

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set click listeners...
		findViewById(R.id.btn_main_show_products).setOnClickListener(this);
		findViewById(R.id.btn_main_show_partners).setOnClickListener(this);
		findViewById(R.id.btn_main_init_sale).setOnClickListener(this);
		findViewById(R.id.btn_main_show_history).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btn_main_show_partners:
			intent = new Intent(MainActivity.this, ShowPartnersListActivity.class);
			break;
		case R.id.btn_main_show_products:
			intent = new Intent(MainActivity.this, ShowProductListActivity.class);
			break;
		case R.id.btn_main_init_sale:
			intent = new Intent(MainActivity.this, InitSaleActivity.class);
			break;
		case R.id.btn_main_show_history:
			intent = new Intent(MainActivity.this, HistoryActivity.class);
			startActivity(intent);
			break;
		}
		startActivity(intent);
	}

	// ========================= Menus stuff ===========================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_tpv:
			Intent intent2 = new Intent(MainActivity.this, TPVActivity.class);
			startActivity(intent2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// ========================= Custom GUI =============================

	private void loadCustomActionBar() {
		// Custom typeface
		SpannableString s = new SpannableString(Constants.ACTION_BAR_MARGIN
				+ getString(R.string.ab_title_main));
		s.setSpan(new TypefaceSpan(this, "RobotoSlab-Bold", true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);
	}
}

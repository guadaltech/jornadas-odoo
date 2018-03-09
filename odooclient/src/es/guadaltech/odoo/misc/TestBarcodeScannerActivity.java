package es.guadaltech.odoo.misc;

import net.sourceforge.zbar.Symbol;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import es.guadaltech.odoo.MainActivity;
import es.guadaltech.odoo.ProductShowDetailsActivity;

public class TestBarcodeScannerActivity extends Activity {

	private static final int ZBAR_SCANNER_REQUEST = 0;
	// private static final int ZBAR_QR_SCANNER_REQUEST = 1;
	private TextView main;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup views
		main = new TextView(this);
		main.setText("miau");

		setContentView(main);

		// Automatically open camera for QR recognizement
		launchQRScanner();
	}

	// Scans all types of codes
	public void launchScanner() {
		if (isCameraAvailable()) {
			Intent intent = new Intent(this, ZBarScannerActivity.class);
			startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
		} else {
			Toast.makeText(this, "La cámara trasera no está disponible",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	// Scans only QR codes
	public void launchQRScanner() {
		if (isCameraAvailable()) {
			Intent intent = new Intent(this, ZBarScannerActivity.class);
			intent.putExtra(ZBarConstants.SCAN_MODES, new int[] { Symbol.CODE128,
					Symbol.EAN13, Symbol.UPCA });
			startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
		} else {
			Crouton.makeText(this, "La cámara trasera no está disponible", Style.ALERT)
					.show();
		}
	}

	public boolean isCameraAvailable() {
		PackageManager pm = getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ZBAR_SCANNER_REQUEST:
			// case ZBAR_QR_SCANNER_REQUEST:

			if (resultCode == RESULT_OK) {
				// Code read
				String result = data.getStringExtra(ZBarConstants.SCAN_RESULT);
				main.setText(result);
				Intent intent = new Intent(this, ProductShowDetailsActivity.class);
				intent.putExtra(Constants.INTENT_KEY_CODE, result.substring(0, 4));
				startActivity(intent);
				finish();
			} else if (resultCode == RESULT_CANCELED && data != null) {
				String error = data.getStringExtra(ZBarConstants.ERROR_INFO);
				if (!TextUtils.isEmpty(error)) {
					Crouton.makeText(this, error, Style.ALERT).show();
				}
			}
			break;
		}
	}

	// Menus stuff
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getSupportMenuInflater().inflate(R.menu.menu_qr, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
			return true;
			// case R.id.menu_qr_qr:
			// launchQRScanner();
		}
		return super.onOptionsItemSelected(item);
	}
}
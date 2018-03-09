package es.guadaltech.odoo;

import java.util.concurrent.ExecutionException;

import org.json.JSONException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import es.guadaltech.odoo.dao.ProductDAO;
import es.guadaltech.odoo.misc.Constants;
import es.guadaltech.odoo.misc.ImageUtils;
import es.guadaltech.odoo.misc.view.TypefaceSpan;
import es.guadaltech.odoo.model.Product;

public class ProductShowDetailsActivity extends Activity {

	// TODO when models hadn't got primitive types, refactor this class
	private ImageView ivMain;
	private TextView tvName, tvReference, tvListPrice, tvCostPrice, tvRealStock, tvVirtualStock,
			tvDescription, tvDescriptionSale;
	private int mShortAnimationDuration;
	private Animator mCurrentAnimator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_showdetails);

		loadCustomActionBar();

		// Retrieve and cache the system's default "short" animation time.
		mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

		// Bind views
		ivMain = (ImageView) findViewById(R.id.iv_product_details);
		tvName = (TextView) findViewById(R.id.tv_product_details_name);
		tvReference = (TextView) findViewById(R.id.tv_product_details_reference);
		tvListPrice = (TextView) findViewById(R.id.tv_product_details_list_price);
		tvCostPrice = (TextView) findViewById(R.id.tv_product_details_cost_price);
		tvRealStock = (TextView) findViewById(R.id.tv_product_details_real_stock);
		tvVirtualStock = (TextView) findViewById(R.id.tv_product_details_virtual_stock);
		tvDescription = (TextView) findViewById(R.id.tv_product_details_general_description);
		tvDescriptionSale = (TextView) findViewById(R.id.tv_product_details_sale_description);

		Bundle bundle = getIntent().getExtras();
		String code = bundle.getString(Constants.INTENT_KEY_CODE);

		try {
			getItem(code);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void getItem(String id) throws JSONException {
		ProductDAO dao = new ProductDAO();
		try {
			Product p = dao.searchByID(this.getApplicationContext(), id).get();

			// Assume that the product have name if not, go out
			if (p.getName() == null) {
				return;
			}

			tvName.setText(p.getName());
			if (p.getDefaultCode().equals("false"))
				tvReference.setText("SIN REF");
			else
				tvReference.setText(p.getDefaultCode());
			if (p.getListPrice() != 0.0)
				tvListPrice.setText(String.format("%.2f", p.getListPrice()));
			if (p.getCostPrice() != 0.0)
				tvCostPrice.setText(String.format("%.2f", p.getCostPrice()));
			if (p.getRealStock() != 0.0)
				tvRealStock.setText(String.format("%.0f", p.getRealStock()));// OJO!!!!!!!
			if (p.getVirtualStock() != 0.0)
				tvVirtualStock.setText(String.format("%.0f", p.getVirtualStock()));// OJO!!!!!!!
			if (p.getDescription() != null)
				tvDescription.setText(p.getDescription());
			if (p.getDescriptionSale() != null)
				tvDescriptionSale.setText(p.getDescriptionSale());
			if (p.getCanBeExpense())
				((CheckBox) findViewById(R.id.cb_product_expensable)).setChecked(true);
			if (p.getCanBePurchased())
				((CheckBox) findViewById(R.id.cb_product_purchasable)).setChecked(true);
			if (p.getCanBeSold())
				((CheckBox) findViewById(R.id.cb_product_soldable)).setChecked(true);

			// Load small image
			final Bitmap image = ImageUtils.decodeSampledBitmapFromBase64(p.getProductImageSmall(), 128, 128);
			ivMain.setImageBitmap(image);

			// Load full size image
			// ////

			ivMain.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					zoomImageFromThumb(ivMain, image);
				}
			});

			findViewById(R.id.iv_product_details_zoomed).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					findViewById(R.id.container).setVisibility(View.VISIBLE);
					findViewById(R.id.iv_product_details_zoomed).setVisibility(View.GONE);
				}
			});

		} catch (InterruptedException e) {
			Log.e(Constants.TAG, "Error al hacer la peticion a la DB");
			e.printStackTrace();
		} catch (ExecutionException e) {
			Log.e(Constants.TAG, "Error al hacer la peticion a la DB");
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e(Constants.TAG, "No hay producto con ese ID");
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(Constants.TAG, "Shame on bitmap objects!!!!!!!!");
			e.printStackTrace();
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
		s.setSpan(new TypefaceSpan(this, "RobotoSlab-Bold", true), 0, s.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);

		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void zoomImageFromThumb(final View thumbView, Bitmap bImage) {
		// If there's an animation in progress, cancel it
		// immediately and proceed with this one.
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		// Load the high-resolution "zoomed-in" image.
		final ImageView expandedImageView = (ImageView) findViewById(R.id.iv_product_details_zoomed);
		expandedImageView.setImageBitmap(bImage);

		// Calculate the starting and ending bounds for the zoomed-in image.
		// This step involves lots of math. Yay, math.
		final Rect startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		// The start bounds are the global visible rectangle of the thumbnail,
		// and the final bounds are the global visible rectangle of the
		// container
		// view. Also set the container view's offset as the origin for the
		// bounds, since that's the origin for the positioning animation
		// properties (X, Y).
		thumbView.getGlobalVisibleRect(startBounds);
		findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		// Adjust the start bounds to be the same aspect ratio as the final
		// bounds using the "center crop" technique. This prevents undesirable
		// stretching during the animation. Also calculate the start scaling
		// factor (the end scaling factor is always 1.0).
		float startScale;
		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width()
				/ startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		// Hide the thumbnail and show the zoomed-in view. When the animation
		// begins, it will position the zoomed-in view in the place of the
		// thumbnail.
		thumbView.setAlpha(0f);
		expandedImageView.setVisibility(View.VISIBLE);

		// Set the pivot point for SCALE_X and SCALE_Y transformations
		// to the top-left corner of the zoomed-in view (the default
		// is the center of the view).
		expandedImageView.setPivotX(0f);
		expandedImageView.setPivotY(0f);

		// Construct and run the parallel animation of the four translation and
		// scale properties (X, Y, SCALE_X, and SCALE_Y).
		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left, finalBounds.left))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;

		// Upon clicking the zoomed-in image, it should zoom back down
		// to the original bounds and show the thumbnail instead of
		// the expanded image.
		final float startScaleFinal = startScale;
		expandedImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCurrentAnimator != null) {
					mCurrentAnimator.cancel();
				}

				// Animate the four positioning/sizing properties in parallel,
				// back to their original values.
				AnimatorSet set = new AnimatorSet();
				set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
				set.setDuration(mShortAnimationDuration);
				set.setInterpolator(new DecelerateInterpolator());
				set.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						thumbView.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						thumbView.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}
				});
				set.start();
				mCurrentAnimator = set;
			}
		});
	}
}

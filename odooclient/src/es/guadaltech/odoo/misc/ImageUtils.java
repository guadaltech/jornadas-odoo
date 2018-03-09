package es.guadaltech.odoo.misc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

public class ImageUtils {

	// Util to generate thumbnail file from a byte-array
	public static Bitmap decodeSampledBitmapFromBase64(String base64image, int reqWidth, int reqHeight) {

		if (base64image == null || base64image == "") {
			Log.e(Constants.TAG, "Attempting to sampling an invalid image");
			return null;
		}

		byte[] decodedString = null;
		try {
			decodedString = Base64.decode(base64image, Base64.DEFAULT);
		} catch (IllegalArgumentException e) {
			// Invalid input, bad base64
			e.printStackTrace();
			return null;
		}

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
}

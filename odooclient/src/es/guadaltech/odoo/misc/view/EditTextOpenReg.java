package es.guadaltech.odoo.misc.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextOpenReg extends EditText {

	public EditTextOpenReg(Context context) {
		super(context);
		useCustomTypography();
	}

	public EditTextOpenReg(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		useCustomTypography();
	}

	public EditTextOpenReg(Context context, AttributeSet attrs) {
		super(context, attrs);
		useCustomTypography();
	}

	private void useCustomTypography() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");
		setTypeface(tf, 1);
	}
}

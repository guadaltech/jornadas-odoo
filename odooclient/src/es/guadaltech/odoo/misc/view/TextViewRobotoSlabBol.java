package es.guadaltech.odoo.misc.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewRobotoSlabBol extends TextView {

	public TextViewRobotoSlabBol(Context context) {
		super(context);
		useCustomTypography();
	}

	public TextViewRobotoSlabBol(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		useCustomTypography();
	}

	public TextViewRobotoSlabBol(Context context, AttributeSet attrs) {
		super(context, attrs);
		useCustomTypography();
	}

	private void useCustomTypography() {
		Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/RobotoSlab-Bold.ttf");
		setTypeface(tf, 1);
	}
}

package com.sap.sailing.android.shared.ui.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class AutoResizeLightTextView extends AutoResizeTextView {

	public AutoResizeLightTextView(Context context) {
		this(context, null);
	}

	public AutoResizeLightTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public AutoResizeLightTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Light.ttf"));
		mTextSize = getTextSize();
	}

}

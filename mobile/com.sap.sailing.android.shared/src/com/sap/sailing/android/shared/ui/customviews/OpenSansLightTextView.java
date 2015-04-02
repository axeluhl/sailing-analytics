package com.sap.sailing.android.shared.ui.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class OpenSansLightTextView extends TextView {

	public OpenSansLightTextView(Context context) {
	    super(context);
	    init();
	}

	public OpenSansLightTextView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	}

	public OpenSansLightTextView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}

	protected void init() {
	    setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Light.ttf"));
	}

}

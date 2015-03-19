package com.sap.sailing.android.shared.ui.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

public class OpenSansButton extends Button {

	public OpenSansButton(Context context) {
		super(context);
		init();
	}

	public OpenSansButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public OpenSansButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public OpenSansButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}
	
	protected void init()
	{
		setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf"));
	}

}

package com.sap.sailing.android.tracking.app.customviews;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import com.sap.sailing.android.tracking.app.R;

public class OpenSansToolbar extends Toolbar {

	public OpenSansToolbar(Context context) {
		super(context);
	}

	public OpenSansToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OpenSansToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public void setTitle(int resId) {
		TextView titleTextView = (TextView)findViewById(R.id.toolbar_title);
		titleTextView.setText(resId);
	}
	
	@Override
	public void setSubtitle(int resId) {
		TextView subtitleTextView = (TextView)findViewById(R.id.toolbar_subtitle);
		subtitleTextView.setText(resId);
	}
	
	@Override
	public void setTitle(CharSequence arg0) {
		TextView titleTextView = (TextView)findViewById(R.id.toolbar_title);
		titleTextView.setText(arg0);
	}
	
	@Override
	public void setSubtitle(CharSequence arg0) {
		TextView subtitleTextView = (TextView)findViewById(R.id.toolbar_subtitle);
		subtitleTextView.setText(arg0);
	}
}

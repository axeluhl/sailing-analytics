package com.sap.sailing.racecommittee.app.ui.activities;

import com.sap.sailing.racecommittee.app.R;

import android.os.Bundle;
import android.view.View;

/**
 * A two-pane activity - saves the visibility state of the right pane.
 */
public class TwoPaneActivity extends BaseActivity {

	private final static String VisibilityTag = TwoPaneActivity.class.getName() + ".rightlayoutvisibility";
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
	    	if (savedInstanceState.getBoolean(VisibilityTag)) {
	    		getRightLayout().setVisibility(View.VISIBLE);
	    	}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(VisibilityTag, getRightLayout().getVisibility() == View.VISIBLE);
	}
	
	protected View getRightLayout() {
		return findViewById(R.id.rightLayout);
	}
}

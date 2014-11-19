package com.sap.sailing.android.tracking.app.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.ui.fragments.HomeFragment;
import com.sap.sailing.android.tracking.app.ui.fragments.TrackingFragment;

public class TrackingActivity extends BaseActivity {

	private final static String TAG = TrackingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Intent intent = getIntent();

        setContentView(R.layout.fragment_container);
        
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setTitle(R.string.title_activity_tracking);
        }

        replaceFragment(R.id.content_frame, new TrackingFragment());
        startTrackingService();
    }
    
    private void startTrackingService()
    {
    	Intent intent = new Intent(this, TrackingService.class);
		intent.setAction(getString(R.string.tracking_service_start));
		startService(intent);
    }
    
	@Override
	public void onBackPressed() {
		TrackingFragment trackingFragment = (TrackingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		
		trackingFragment.userTappedBackButton();
	}
}

package com.sap.sailing.android.tracking.app.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.fragments.HomeFragment;
import com.sap.sailing.android.tracking.app.valueobjects.CheckinData;

import net.hockeyapp.android.CrashManager;

public class StartActivity extends CheckinDataActivity {
    
    private final static String TAG = StartActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(getString(R.string.title_activity_start));
			getSupportActionBar().setHomeButtonEnabled(false);
        }
        
        replaceFragment(R.id.content_frame, new HomeFragment());
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	checkForCrashes();
        //checkForUpdates();
        
    	int googleServicesResultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	if (googleServicesResultCode != ConnectionResult.SUCCESS)
    	{
    		Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googleServicesResultCode, this, 0);
    		dialog.show();
    	}
    }
    
    @Override
	public void onStart() {
    	super.onStart();

    	// get url if launched via url intent-filter
       
        Intent intent = getIntent();
        Uri uri = intent.getData();

		if (uri != null) {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG, "Matched URL, handling scanned or matched URL.");
			}
			
			HomeFragment homeFragment = getHomeFragment();
			
			homeFragment.handleScannedOrUrlMatchedUri(uri);
		}
        
        intent.setData(null);
        
        if (prefs.getTrackerIsTracking())
        {
        	String checkinDigest = prefs.getTrackerIsTrackingCheckinDigest();
        	startRegatta(checkinDigest);
        }
    }

	public HomeFragment getHomeFragment() {
		HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		return homeFragment;
	}

    /**
     * Hockeyapp integration method.
     */
	private void checkForCrashes() {
		CrashManager.register(this, "060ff0c8a907638e3b31d3146091c87b");
	}

//	/**
//     * Hockeyapp integration method.
//     */
//	private void checkForUpdates() {
//		// TODO: Remove this for store builds!
//		UpdateManager.register(this, "060ff0c8a907638e3b31d3146091c87b");
//	}

    private void startRegatta(String checkinDigest) {
		Intent intent = new Intent(this, RegattaActivity.class);
		intent.putExtra(getString(R.string.checkin_digest), checkinDigest);
		startActivity(intent);
	}

    @Override
    public void onCheckinDataAvailable(CheckinData data) {
        if(data != null)
        {
            getHomeFragment().displayUserConfirmationScreen(data);
        }
    }
}

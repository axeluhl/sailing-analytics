package com.sap.sailing.android.tracking.app.ui.activities;

import net.hockeyapp.android.CrashManager;
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

public class StartActivity extends BaseActivity {
    
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
    	if (!BuildConfig.DEBUG && googleServicesResultCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(googleServicesResultCode, this, 0).show();
    	}
    }
    
    @Override
	public void onStart() {
    	super.onStart();

    	// get url if launched via url intent-filter
       
        Intent intent = getIntent();
        String urlStr = intent.getDataString();
        
		if (urlStr != null) {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG, "Matched URL, handling scanned or matched URL.");
			}
			
			Uri uri = Uri.parse(urlStr);
			
			HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
					.findFragmentById(R.id.content_frame);
			
			homeFragment.handleScannedOrUrlMatchedUri(uri);
		}
        
        intent.setData(null);
        
        if (prefs.getTrackerIsTracking())
        {
        	String checkinDigest = prefs.getTrackerIsTrackingCheckinDigest();
        	startRegatta(checkinDigest);
        }
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
}

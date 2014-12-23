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
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.utils.ServiceHelper;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;

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
    	
    	int googleServicesResultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	if (googleServicesResultCode != ConnectionResult.SUCCESS)
    	{
    		Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googleServicesResultCode, this, 0);
    		dialog.show();
    	}
    }
    
    
    @Override
    protected void onStart() {
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

        // start transmitting service that will henceforth run in the background
		ServiceHelper.getInstance().startTransmittingService(this);
        
        intent.setData(null);
        
        if (prefs.getTrackerIsTracking())
        {
        	String eventId = prefs.getTrackerIsTrackingEventId();
        	EventInfo event = DatabaseHelper.getInstance().getEventInfoWithLeaderboardAndCompetitor(this, eventId);
        	startRegatta(event.leaderboardName, eventId, event.competitorId);
        }
    }

    private void startRegatta(String leaderboardName, String eventId, String competitorId) {
		Intent intent = new Intent(this, RegattaActivity.class);
		intent.putExtra(getString(R.string.leaderboard_name), leaderboardName);
		intent.putExtra(getString(R.string.event_id), eventId);
		intent.putExtra(getString(R.string.competitor_id), competitorId);
		startActivity(intent);
	}
}

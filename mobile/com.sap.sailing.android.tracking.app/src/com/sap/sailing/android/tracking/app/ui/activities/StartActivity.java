package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TransmittingService;
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
            getSupportActionBar().setHomeButtonEnabled(false);
        }
        
        replaceFragment(R.id.content_frame, new HomeFragment());
    }
    
    
    @Override
    protected void onStart() {
    	super.onStart();

    	// get url if launched via url intent-filter
       
        Intent intent = getIntent();
        String urlStr = intent.getDataString();
        
		if (urlStr != null) {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG,
						"Matched URL, handling scanned or matched URL.");
			}
			
			Uri uri = Uri.parse(urlStr);
			
			HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
					.findFragmentById(R.id.content_frame);
			
			homeFragment.handleScannedOrUrlMatchedUri(uri);
		}

        // start transmitting service that will henceforth run in the background
        
        Intent serviceIntent = new Intent(this, TransmittingService.class);
        serviceIntent.setAction(getString(R.string.transmitting_service_start));
        this.startService(serviceIntent);
        
        intent.setData(null);
        
        if (prefs.getTrackerIsTracking())
        {
        	int eventId = prefs.getTrackerIsTrackingEventId();
        	startTrackingActivity(eventId);
        }
    }
    
    private void startTrackingActivity(int eventId) {
		Intent intent = new Intent(this, TrackingActivity.class);
		intent.putExtra(getString(R.string.tracking_activity_event_id_parameter), eventId);
		intent.putExtra(getString(R.string.tracking_activity_started_by_start_activity_parameter), true);
		startActivity(intent);
	}

}

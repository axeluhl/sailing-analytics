package com.sap.sailing.android.tracking.app.ui.activities;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQuality;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQualityListener;
import com.sap.sailing.android.tracking.app.services.TrackingService.TrackingBinder;
import com.sap.sailing.android.tracking.app.ui.fragments.TrackingFragment;

public class TrackingActivity extends BaseActivity implements GPSQualityListener {
	
	TrackingService trackingService;
	boolean trackingServiceBound;
	
	private final static String TAG = TrackingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    
    @Override
    protected void onStart() {
    	super.onStart();
    	Intent intent = new Intent(this, TrackingService.class);
        bindService(intent, trackingServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
        if (trackingServiceBound) {
        	trackingService.unregisterGPSQualityListener();
            unbindService(trackingServiceConnection);
            trackingServiceBound = false;
        }
    }
    
    @Override
    public void gpsQualityUpdated(GPSQuality quality) {
    	TrackingFragment trackingFragment = (TrackingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
    	trackingFragment.setGPSQuality(quality);
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
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection trackingServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TrackingBinder binder = (TrackingBinder) service;
            trackingService = binder.getService();
            trackingServiceBound = true;
            trackingService.registerGPSQualityListener(TrackingActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            trackingServiceBound = false;
        }
    };
}

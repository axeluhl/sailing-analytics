package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQuality;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQualityListener;
import com.sap.sailing.android.tracking.app.services.TrackingService.TrackingBinder;
import com.sap.sailing.android.tracking.app.services.TransmittingService;
import com.sap.sailing.android.tracking.app.services.TransmittingService.APIConnectivity;
import com.sap.sailing.android.tracking.app.services.TransmittingService.APIConnectivityListener;
import com.sap.sailing.android.tracking.app.services.TransmittingService.TransmittingBinder;
import com.sap.sailing.android.tracking.app.ui.fragments.TrackingFragment;

public class TrackingActivity extends BaseActivity implements GPSQualityListener, APIConnectivityListener {
	
	TrackingService trackingService;
	boolean trackingServiceBound;
	
	TransmittingService transmittingService;
	boolean transmittingServiceBound;
	
	private final static String TAG = TrackingActivity.class.getName();

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
    	Intent transmittingServiceIntent = new Intent(this, TransmittingService.class);
        bindService(transmittingServiceIntent, transmittingServiceConnection, Context.BIND_AUTO_CREATE);
    	Intent trackingServiceIntent = new Intent(this, TrackingService.class);
        bindService(trackingServiceIntent, trackingServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
        if (trackingServiceBound) {
        	trackingService.unregisterGPSQualityListener();
            unbindService(trackingServiceConnection);
            trackingServiceBound = false;
            
            if (BuildConfig.DEBUG)
            {
            	ExLog.i(this, TAG, "Unbound tracking Service");
            }
        }
        
        if (transmittingServiceBound)
        {
        	transmittingService.unregisterAPIConnectivityListener();
        	unbindService(transmittingServiceConnection);
        	
        	transmittingServiceBound = false;
        	
        	if (BuildConfig.DEBUG)
            {
            	ExLog.i(this, TAG, "Unbound transmitting Service");
            }
        }
    }
    
    
    
    @Override
    public void gpsQualityUpdated(GPSQuality quality) {
    	TrackingFragment trackingFragment = (TrackingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
    	trackingFragment.setGPSQuality(quality);
    }
    
    @Override
    public void apiConnectivityUpdated(APIConnectivity apiConnectivity) {
		TrackingFragment trackingFragment = (TrackingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		trackingFragment.setAPIConnectivityStatus(apiConnectivity);
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
    private ServiceConnection transmittingServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            TransmittingBinder binder = (TransmittingBinder) service;
            transmittingService = binder.getService();
            transmittingServiceBound = true;
            transmittingService.registerAPIConnectivityListener(TrackingActivity.this);
            if (BuildConfig.DEBUG)
            {
            	ExLog.i(TrackingActivity.this, TAG, "connected to transmitting service");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            transmittingServiceBound = false;
        }
    };
    
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
            if (BuildConfig.DEBUG)
            {
            	ExLog.i(TrackingActivity.this, TAG, "connected to tracking service");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            trackingServiceBound = false;
        }
    };
}

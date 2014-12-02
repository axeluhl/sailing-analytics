package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.LeaderboardsEventsJoined;
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
	private final static String SIS_FRAGMENT = "savedInstanceTrackingFragment";
	
	private int eventId;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        eventId = intent.getExtras().getInt(getString(R.string.tracking_activity_event_id_parameter)); 
        		
        setContentView(R.layout.fragment_container);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        
        if (getSupportActionBar() != null) {
        	
        	String leaderboardName = "";
        	String eventName = "";
        	ContentResolver cr = getContentResolver();
        	String projectionStr = "events._id,leaderboards.leaderboard_name,events.event_name";
        	String[] projection = projectionStr.split(",");
        	Cursor cursor = cr.query(LeaderboardsEventsJoined.CONTENT_URI, projection, "events._id = " + eventId, null, null);
        	if (cursor.moveToFirst())
        	{
        		eventName = cursor.getString(cursor.getColumnIndex("event_name"));
        		leaderboardName = cursor.getString(cursor.getColumnIndex("leaderboard_name"));
        	}
        	
        	cursor.close();
        	
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            getSupportActionBar().setTitle(leaderboardName);
            getSupportActionBar().setSubtitle(getString(R.string.tracking_colon) + " " + eventName);
        }
        
        TrackingFragment fragment;
        if (savedInstanceState != null)
        {
        	fragment = (TrackingFragment)getSupportFragmentManager().getFragment(savedInstanceState, SIS_FRAGMENT);
        }
        else
        {
        	fragment = new TrackingFragment();
        }
        
        replaceFragment(R.id.content_frame, fragment);
        startTrackingService(eventId);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	TrackingFragment fragment = (TrackingFragment)getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
    	getSupportFragmentManager().putFragment(outState, SIS_FRAGMENT, fragment);
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
    
    private void startTrackingService(int eventId)
    {
    	Intent intent = new Intent(this, TrackingService.class);
		intent.setAction(getString(R.string.tracking_service_start));
		intent.putExtra(getString(R.string.tracking_service_event_id_parameter), eventId);
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

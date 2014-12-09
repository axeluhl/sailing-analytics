package com.sap.sailing.android.tracking.app.ui.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.Toolbar;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.sensors.CompassManager;
import com.sap.sailing.android.tracking.app.sensors.CompassManager.MagneticHeadingListener;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQuality;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQualityListener;
import com.sap.sailing.android.tracking.app.services.TrackingService.TrackingBinder;
import com.sap.sailing.android.tracking.app.services.TransmittingService;
import com.sap.sailing.android.tracking.app.services.TransmittingService.APIConnectivity;
import com.sap.sailing.android.tracking.app.services.TransmittingService.APIConnectivityListener;
import com.sap.sailing.android.tracking.app.services.TransmittingService.TransmittingBinder;
import com.sap.sailing.android.tracking.app.ui.fragments.CompassFragment;
import com.sap.sailing.android.tracking.app.ui.fragments.TrackingFragment;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.viewpagerindicator.CirclePageIndicator;

public class TrackingActivity extends BaseActivity implements GPSQualityListener, APIConnectivityListener, MagneticHeadingListener {
	
	TrackingService trackingService;
	boolean trackingServiceBound;
	
	TransmittingService transmittingService;
	boolean transmittingServiceBound;
	
	private final static String TAG = TrackingActivity.class.getName();
	private final static String SIS_TRACKING_FRAGMENT = "savedInstanceTrackingFragment";
	private final static String SIS_COMPASS_FRAGMENT = "savedInstanceCompassFragment";
	
    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

	private String eventId;	
	private AppPreferences prefs;
	
	private TrackingFragment trackingFragment;
	private CompassFragment compassFragment;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = new AppPreferences(this);
        
        Intent intent = getIntent();
        eventId = intent.getExtras().getString(getString(R.string.tracking_activity_event_id_parameter)); 
        		
        setContentView(R.layout.fragment_hud_container);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        
        if (getSupportActionBar() != null) {
        	EventInfo eventInfo = DatabaseHelper.getInstance(this).getEventInfoWithLeaderboard(eventId);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
            toolbar.setPadding(20, 0, 0, 0);
            getSupportActionBar().setTitle(eventInfo.leaderboardName);
            getSupportActionBar().setSubtitle(getString(R.string.tracking_colon) + " " + eventInfo.name);
        }
        
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        
        if (savedInstanceState != null)
        {
        	trackingFragment = (TrackingFragment)getSupportFragmentManager().getFragment(savedInstanceState, SIS_TRACKING_FRAGMENT);
        	compassFragment = (CompassFragment)getSupportFragmentManager().getFragment(savedInstanceState, SIS_COMPASS_FRAGMENT);
        }
        else
        {
        	trackingFragment = new TrackingFragment();
        	compassFragment = new CompassFragment();
        }

        //Bind the title indicator to the adapter
        CirclePageIndicator titleIndicator = (CirclePageIndicator)findViewById(R.id.title_page_indicator);
        titleIndicator.setViewPager(mPager);
        titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        
        startTrackingService(eventId);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
//    	HudFragment hudFragment = (HudFragment) getSupportFragmentManager().findFragmentById(R.id.hud_content_frame);
//    	hudFragment.layoutOverlay();
//    	
//    	hudFragment.setSpeedOverGround(0);
//    	hudFragment.setHeading(0);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	getSupportFragmentManager().putFragment(outState, SIS_TRACKING_FRAGMENT, trackingFragment);
    	getSupportFragmentManager().putFragment(outState, SIS_COMPASS_FRAGMENT, compassFragment);
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
    protected void onPause() {
    	super.onPause();
    	CompassManager.getInstance(this).unregisterListener();
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	
		if (prefs.getHeadingFromMagneticSensorPreferred()) {
			CompassManager.getInstance(this).registerListener(this);
		}
    }

    @Override
    public void gpsQualityAndAccurracyUpdated(GPSQuality quality, float gpsAccurracy, float bearing, float speed) {
    		TrackingFragment trackingFragment = (TrackingFragment) mPagerAdapter.getItem(ScreenSlidePagerAdapter.VIEW_PAGER_FRAGMENT_TRACKING);
    		trackingFragment.setGPSQualityAndAcurracy(quality, gpsAccurracy);
    		
    		CompassFragment compassFragment = (CompassFragment) mPagerAdapter.getItem(ScreenSlidePagerAdapter.VIEW_PAGER_FRAGMENT_COMPASS);
    		compassFragment.setBearing(bearing);
    }
    
    @Override
    public void apiConnectivityUpdated(APIConnectivity apiConnectivity) {
		TrackingFragment trackingFragment = (TrackingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		if (trackingFragment != null)
		{
			trackingFragment.setAPIConnectivityStatus(apiConnectivity);	
		}
    }
    
    @Override
    public void setUnsentGPSFixesCount(int count) {
    	TrackingFragment trackingFragment = (TrackingFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
    	if (trackingFragment != null)
		{
    		trackingFragment.setUnsentGPSFixesCount(count);
		}
    }
    
    private void startTrackingService(String eventId)
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

	@Override
	public void magneticHeadingUpdated(float heading) {
    	//HudFragment hudFragment = (HudFragment) getSupportFragmentManager().findFragmentById(R.id.hud_content_frame);
    	if (prefs.getHeadingFromMagneticSensorPreferred())
    	{
    		if (mPager.getCurrentItem() == ScreenSlidePagerAdapter.VIEW_PAGER_FRAGMENT_COMPASS)
    		{
    			compassFragment.setBearing(heading);	
    		}
    	}
    	else
    	{
    		if (BuildConfig.DEBUG)
    		{
    			ExLog.i(this, TAG, "Received magnet compass update, even though prefs say get from GPS. Unregistering listener." );
    		}
    		CompassManager.getInstance(this).unregisterListener();
    	}
	}
	
	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		
		public final static int VIEW_PAGER_FRAGMENT_TRACKING = 0;
		public final static int VIEW_PAGER_FRAGMENT_COMPASS = 1;
		
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	if (position == VIEW_PAGER_FRAGMENT_TRACKING)
        	{
        		return trackingFragment;	
        	}
        	else if (position == VIEW_PAGER_FRAGMENT_COMPASS)
        	{
        		return compassFragment;
        	}
        	else
        	{
        		return null;
        	}
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}

package com.sap.sailing.android.tracking.app.ui.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;

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
import com.sap.sailing.android.tracking.app.ui.fragments.SpeedFragment;
import com.sap.sailing.android.tracking.app.ui.fragments.StopTrackingButtonFragment;
import com.sap.sailing.android.tracking.app.ui.fragments.TrackingFragment;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.utils.ServiceHelper;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.viewpagerindicator.CirclePageIndicator;

public class TrackingActivity extends BaseActivity implements GPSQualityListener,
		APIConnectivityListener, MagneticHeadingListener {

	TrackingService trackingService;
	boolean trackingServiceBound;

	TransmittingService transmittingService;
	boolean transmittingServiceBound;

	private final static String TAG = TrackingActivity.class.getName();
	private final static String SIS_TRACKING_FRAGMENT = "savedInstanceTrackingFragment";
	private final static String SIS_LAST_VIEWPAGER_ITEM = "instanceStateLastViewPagerItem";
	private final static String SIS_LAST_SPEED_TEXT = "instanceStateLastSpeedText";
	private final static String SIS_LAST_COMPASS_TEXT = "instanceStateLastCompassText";

	private ViewPager mPager;
	private ScreenSlidePagerAdapter mPagerAdapter;

	private String eventId;
	private AppPreferences prefs;

	private TrackingFragment trackingFragment;	
	private TimerRunnable timer;
	
	private int lastViewPagerItem;
	
	/**
	 * This isn't nice. The callbacks for fragments inside a 
	 * view pager are unreliable, but I want the values
	 * to be displayed immediately after device rotation.
	 * Thus they are cached here and the fragments can pick
	 * them up.
	 */
	public String lastSpeedIndicatorText = "";
	public String lastCompassIndicatorText = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = new AppPreferences(this);

		Intent intent = getIntent();
		eventId = intent.getExtras().getString(
				getString(R.string.tracking_activity_event_id_parameter));

		setContentView(R.layout.fragment_hud_container);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
			toolbar.setPadding(20, 0, 0, 0);
			toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
		}

		if (getSupportActionBar() != null) {
			EventInfo eventInfo = DatabaseHelper.getInstance().getEventInfoWithLeaderboard(this, eventId);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
			toolbar.setNavigationIcon(R.drawable.sap_logo_64_sq);
			toolbar.setPadding(20, 0, 0, 0);
			getSupportActionBar().setTitle(eventInfo.leaderboardName);
			getSupportActionBar().setSubtitle(
					getString(R.string.tracking_colon) + " " + eventInfo.name);
		}

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		if (mPagerAdapter == null)
		{
			mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		}
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(0);

		if (savedInstanceState != null) {
			Fragment tFragment = getSupportFragmentManager().getFragment(savedInstanceState, SIS_TRACKING_FRAGMENT);
			if (tFragment != null) {
				trackingFragment = (TrackingFragment) tFragment;
			} else {
				trackingFragment = new TrackingFragment();
			}
			
			lastViewPagerItem = savedInstanceState.getInt(SIS_LAST_VIEWPAGER_ITEM);
			lastSpeedIndicatorText = savedInstanceState.getString(SIS_LAST_SPEED_TEXT);
			lastCompassIndicatorText = savedInstanceState.getString(SIS_LAST_COMPASS_TEXT);
		} else {
			trackingFragment = new TrackingFragment();
		}

		// Bind the title indicator to the adapter
		CirclePageIndicator titleIndicator = (CirclePageIndicator) findViewById(R.id.title_page_indicator);
		titleIndicator.setViewPager(mPager);
		titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				lastViewPagerItem = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		replaceFragment(R.id.tracking_linear_layout, trackingFragment);
		ServiceHelper.getInstance().startTrackingService(this, eventId);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(SIS_LAST_VIEWPAGER_ITEM, lastViewPagerItem);
		outState.putString(SIS_LAST_SPEED_TEXT, lastSpeedIndicatorText);
		outState.putString(SIS_LAST_COMPASS_TEXT, lastCompassIndicatorText);
		
		if (trackingFragment.isAdded()) {
			getSupportFragmentManager().putFragment(outState, SIS_TRACKING_FRAGMENT, trackingFragment);
		}	
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent transmittingServiceIntent = new Intent(this, TransmittingService.class);
		bindService(transmittingServiceIntent, transmittingServiceConnection,
				Context.BIND_AUTO_CREATE);
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

			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG, "Unbound tracking Service");
			}
		}

		if (transmittingServiceBound) {
			transmittingService.unregisterAPIConnectivityListener();
			unbindService(transmittingServiceConnection);

			transmittingServiceBound = false;

			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG, "Unbound transmitting Service");
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		CompassManager.getInstance(this).unregisterListener();
		timer.stop();
		
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(null);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (prefs.getHeadingFromMagneticSensorPreferred()) {
			CompassManager.getInstance(this).registerListener(this);
		}

		timer = new TimerRunnable();
		timer.start();
		
		if (mPagerAdapter == null)
		{
			mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		}

		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(lastViewPagerItem);
	}

	@Override
	public void gpsQualityAndAccurracyUpdated(GPSQuality quality, float gpsAccurracy, float bearing, float speed) {		
		if (trackingFragment.isAdded()) {
			trackingFragment.setGPSQualityAndAcurracy(quality, gpsAccurracy);
		}
		
		ScreenSlidePagerAdapter viewPagerAdapter = getViewPagerAdapter();
		
		if (viewPagerAdapter != null) {
			SpeedFragment speedFragment = viewPagerAdapter.getSpeedFragment();
			if (speedFragment != null && speedFragment.isAdded()) {
				speedFragment.setSpeed(speed);
			}

			CompassFragment compassFragment = viewPagerAdapter.getCompassFragment();
			if (compassFragment != null && compassFragment.isAdded()) {
				compassFragment.setBearing(bearing);
			}
		}
	}

	@Override
	public void apiConnectivityUpdated(APIConnectivity apiConnectivity) {
		if (trackingFragment.isAdded()) {
			trackingFragment.setAPIConnectivityStatus(apiConnectivity);
		}
	}

	@Override
	public void setUnsentGPSFixesCount(int count) {
		trackingFragment.setUnsentGPSFixesCount(count);
	}

	@Override
	public void onBackPressed() {
		trackingFragment.userTappedBackButton();
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection transmittingServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			TransmittingBinder binder = (TransmittingBinder) service;
			transmittingService = binder.getService();
			transmittingServiceBound = true;
			transmittingService.registerAPIConnectivityListener(TrackingActivity.this);
			if (BuildConfig.DEBUG) {
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
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			TrackingBinder binder = (TrackingBinder) service;
			trackingService = binder.getService();
			trackingServiceBound = true;
			trackingService.registerGPSQualityListener(TrackingActivity.this);
			if (BuildConfig.DEBUG) {
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
		if (prefs.getHeadingFromMagneticSensorPreferred()) {
			if (mPager.getCurrentItem() == ScreenSlidePagerAdapter.VIEW_PAGER_FRAGMENT_COMPASS) {
				
				ScreenSlidePagerAdapter viewPagerAdapter = getViewPagerAdapter();
				if (viewPagerAdapter != null) {
					CompassFragment compassFragment = viewPagerAdapter.getCompassFragment();
					if (compassFragment != null && compassFragment.isAdded()) {
						compassFragment.setBearing(heading);
					}
				}
			}
		} else {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG,
						"Received magnet compass update, even though prefs say get from GPS. Unregistering listener.");
			}
			CompassManager.getInstance(this).unregisterListener();
		}
	}
	
	private ScreenSlidePagerAdapter getViewPagerAdapter() {
		return (ScreenSlidePagerAdapter) mPager.getAdapter();
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

		public final static int VIEW_PAGER_FRAGMENT_STOP_BUTTON = 0;
		public final static int VIEW_PAGER_FRAGMENT_COMPASS = 1;
		public final static int VIEW_PAGER_FRAGMENT_SPEED = 2;
		
		private StopTrackingButtonFragment stbFragment;
		private CompassFragment cFragment;
		private SpeedFragment sFragment;

		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == VIEW_PAGER_FRAGMENT_STOP_BUTTON) {
				stbFragment = new StopTrackingButtonFragment();
				return stbFragment;
			} else if (position == VIEW_PAGER_FRAGMENT_COMPASS) {
				cFragment = new CompassFragment();
				return cFragment;
			} else if (position == VIEW_PAGER_FRAGMENT_SPEED) {
				sFragment = new SpeedFragment();
				return sFragment;
			} else {
				return null;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
		
		public CompassFragment getCompassFragment() {
			if (cFragment != null && cFragment.isAdded())
			{
				return cFragment;
			}
			else return null;
		}
		
		public SpeedFragment getSpeedFragment() {
			if (sFragment != null && sFragment.isAdded())
			{
				return sFragment;
			}
			else return null;
		}
	}

	public void showStopTrackingConfirmationDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.please_confirm)
				.setMessage(R.string.do_you_really_want_to_stop_tracking)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) {
						stopTracking();
					}
				}).setNegativeButton(android.R.string.no, null).create();

		dialog.show();
	}

	public void stopTracking() {
		prefs.setTrackingTimerStarted(0);
		ServiceHelper.getInstance().stopTrackingService(this);
		finish();
	}

	/**
	 * Update UI with a string containing the time since tracking started, e.g.
	 * 01:22:45
	 */
	public void updateTimer() {
		long diff = System.currentTimeMillis() - prefs.getTrackingTimerStarted();
		TextView textView = (TextView) findViewById(R.id.tracking_time_label);
		if (textView != null) {
			textView.setText(getTimeFormatString(diff));
		}
	}
	
	private String getTimeFormatString(long milliseconds) {
		int seconds = (int) (milliseconds / 1000) % 60 ;
		int minutes = (int) ((milliseconds / (1000*60)) % 60);
		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		
		return String.format(getResources().getConfiguration().locale, "%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	private class TimerRunnable implements Runnable {

		public Thread t;
		public volatile boolean running = true;

		public void start() {
			running = true;
			if (t == null) {
				t = new Thread(this);
				t.start();
			}
		}

		@Override
		public void run() {
			while (running) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						trackingFragment.checkLastGPSReceived();
						updateTimer();
					}
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void stop() {
			running = false;
		}
	}
}

package com.sap.sailing.android.tracking.app.ui.fragments;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TrackingService.GPSQuality;
import com.sap.sailing.android.tracking.app.services.TransmittingService.APIConnectivity;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.views.AutoResizeTextView;
import com.sap.sailing.android.tracking.app.views.SignalQualityIndicatorView;

public class TrackingFragment extends BaseFragment implements OnClickListener {

	static final String SIS_MODE = "instanceStateMode";
	static final String SIS_STATUS = "instanceStateStatus";
	static final String SIS_GPS_QUALITY = "instanceStateGpsQuality";
	static final String SIS_GPS_ACCURACY = "instanceStateGpsAccuracy";
	static final String SIS_GPS_UNSENT_FIXES = "instanceStateGpsUnsentFixes";
	
	private TimerRunnable timer;
	private AppPreferences prefs;
	private long lastGPSQualityUpdate;
	
	private String TAG = TrackingFragment.class.getName(); 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.fragment_tracking, container, false);
		
		Button stopTracking = (Button)view.findViewById(R.id.stop_tracking);
		stopTracking.setOnClickListener(this);
	
		prefs = new AppPreferences(getActivity());
		if (prefs.getTrackingTimerStarted() == 0)
		{
			prefs.setTrackingTimerStarted(System.currentTimeMillis());	
		}
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// so it initally updates to "battery-saving" etc.
		setAPIConnectivityStatus(APIConnectivity.noAttempt);
		timer = new TimerRunnable();
		timer.start();
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.stop_tracking:
			showStopTrackingConfirmationDialog();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		timer.stop();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null)
		{
			System.out.println("NOT NULL");
			TextView modeText = (TextView)getActivity().findViewById(R.id.mode);
			TextView statusText = (TextView)getActivity().findViewById(R.id.tracking_status);
			SignalQualityIndicatorView qualityIndicator = (SignalQualityIndicatorView)getActivity().findViewById(R.id.gps_quality_indicator);
			TextView accuracyText = (TextView)getActivity().findViewById(R.id.gps_accuracy_label);
			TextView unsentFixesText = (TextView)getActivity().findViewById(R.id.tracking_unsent_fixes);
					
			modeText.setText(savedInstanceState.getString(SIS_MODE));
			statusText.setText(savedInstanceState.getString(SIS_STATUS));
			qualityIndicator.setSignalQuality(savedInstanceState.getInt(SIS_GPS_QUALITY));
			accuracyText.setText(savedInstanceState.getString(SIS_GPS_ACCURACY));
			unsentFixesText.setText(savedInstanceState.getString(SIS_GPS_UNSENT_FIXES));
		}
		else
		{
			System.out.println("NULL");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		System.out.println("ON SAVE INSTANCE STATE");
		super.onSaveInstanceState(outState);
		
		TextView modeText = (TextView)getActivity().findViewById(R.id.mode);
		TextView statusText = (TextView)getActivity().findViewById(R.id.tracking_status);
		SignalQualityIndicatorView qualityIndicator = (SignalQualityIndicatorView)getActivity().findViewById(R.id.gps_quality_indicator);
		TextView accuracyText = (TextView)getActivity().findViewById(R.id.gps_accuracy_label);
		TextView unsentFixesText = (TextView)getActivity().findViewById(R.id.tracking_unsent_fixes);
		
		outState.putString(SIS_MODE, modeText.getText().toString());
		outState.putString(SIS_STATUS, statusText.getText().toString());
		outState.putInt(SIS_GPS_QUALITY, qualityIndicator.getSignalQuality());
		outState.putString(SIS_GPS_ACCURACY, accuracyText.getText().toString());
		outState.putString(SIS_GPS_UNSENT_FIXES, unsentFixesText.getText().toString());
	}
	
	/**
	 * Update UI with a string containing the time since tracking started, e.g. 01:22:45
	 */
	public void updateTimer()
	{
		long diff = System.currentTimeMillis() - prefs.getTrackingTimerStarted();
		AutoResizeTextView textView = (AutoResizeTextView) getActivity().findViewById(R.id.tracking_time_label);
		textView.setText(getTimeFormatString(diff));
	}
	
	/**
	 * If last GPS update is too long ago, let's assume there's no signal and set quality to .noSignal
	 */
	public void checkLastGPSReceived()
	{
		if (System.currentTimeMillis() - lastGPSQualityUpdate > 3000 && !isLocationEnabled(getActivity()))
		{
			if (BuildConfig.DEBUG)
			{
				ExLog.i(getActivity(), TAG, "Setting GPS Quality to 0 because timeout occurred and location is reported as disabled.");
			}
			setGPSQualityAndAcurracy(GPSQuality.noSignal, 0);
		}
	}
	
	/**
	 * Update tracking status string in UI
	 * @param quality
	 */
	public void updateTrackingStatus( GPSQuality quality )
	{
		TextView textView = (TextView)getActivity().findViewById(R.id.tracking_status);
		
		if (quality == GPSQuality.noSignal)
		{
			textView.setText(getString(R.string.tracking_status_no_gps_signal));
			textView.setTextColor(Color.parseColor(getString(R.color.sap_red)));
		}
		else
		{
			textView.setText(getString(R.string.tracking_status_tracking));
			textView.setTextColor(Color.parseColor(getString(R.color.sap_green)));
		}
	}
	
	/**
	 * Update UI and tell user if app is caching or sending fixes to api
	 * @param apiIsReachable
	 */
	public void setAPIConnectivityStatus(final APIConnectivity apiConnectivity)
	{
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView textView = (TextView)getActivity().findViewById(R.id.mode);
				
				if (apiConnectivity == APIConnectivity.reachableTransmissionSuccess)
				{
					if (prefs.getEnergySavingEnabledByUser())
					{
						textView.setText(getString(R.string.tracking_mode_battery_saving));
						textView.setTextColor(Color.parseColor(getString(R.color.sap_yellow)));
					}
					else
					{
						textView.setText(getString(R.string.tracking_mode_live));
						textView.setTextColor(Color.parseColor(getString(R.color.sap_green)));	
					}
					
				}
				else if (apiConnectivity == APIConnectivity.noAttempt)
				{
					textView.setText(getString(R.string.tracking_mode_offline));
					textView.setTextColor(Color.parseColor(getString(R.color.sap_green)));
				}
				else if  (apiConnectivity == APIConnectivity.reachableTransmissionError)
				{
					textView.setText(getString(R.string.tracking_mode_api_error));
					textView.setTextColor(Color.parseColor(getString(R.color.sap_red)));
				}
				else
				{
					textView.setText(getString(R.string.tracking_mode_caching));
					textView.setTextColor(Color.parseColor(getString(R.color.sap_green)));
				}
			}
		});
	}
	
	/**
	 * Returns if location is enabled on the device
	 * @param context
	 * @return true if location is enabled, false otherwise
	 */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("deprecation")
    private boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    } 
    
	public void userTappedBackButton()
	{
		showStopTrackingConfirmationDialog();
	}
	
	private void stopTracking() {
		prefs.setTrackingTimerStarted(0);
		
		Intent intent = new Intent(getActivity(), TrackingService.class);
		intent.setAction(getString(R.string.tracking_service_stop));
		getActivity().startService(intent);
		getActivity().finish();
	}

	private void showStopTrackingConfirmationDialog() {
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.please_confirm)
				.setMessage(R.string.do_you_really_want_to_stop_tracking)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								stopTracking();
							}
						}).setNegativeButton(android.R.string.no, null).create();
		
		dialog.show();
	}
	
	private String getTimeFormatString(long milliseconds) {
		int seconds = (int) (milliseconds / 1000) % 60 ;
		int minutes = (int) ((milliseconds / (1000*60)) % 60);
		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		
		return String.format(getResources().getConfiguration().locale, "%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	public void setGPSQualityAndAcurracy(GPSQuality quality, float gpsAccurracy)
	{
		SignalQualityIndicatorView indicatorView = (SignalQualityIndicatorView) getActivity().findViewById(R.id.gps_quality_indicator);
		indicatorView.setSignalQuality( quality.toInt() );
		
		TextView accuracyTextView = (TextView)getActivity().findViewById(R.id.gps_accuracy_label);
		accuracyTextView.setText("~ " + String.valueOf(Math.round(gpsAccurracy)) + " m");
		
		updateTrackingStatus(quality);
		
		lastGPSQualityUpdate = System.currentTimeMillis();
	}
	
	public void setUnsentGPSFixesCount(final int count)
	{
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView unsentGpsFixesTextView = (TextView)getActivity().findViewById(R.id.tracking_unsent_fixes);
				unsentGpsFixesTextView.setText(String.valueOf(count));		
			}});
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
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						checkLastGPSReceived();
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
		
		public void stop()
		{
			running = false;
		}
	}
}

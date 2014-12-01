package com.sap.sailing.racecommittee.app.ui.fragments;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.R.color;
import android.app.Fragment;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;

public class WindFragment extends LoggableFragment implements CompassDirectionListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

	private final static String TAG = WindFragment.class.getName();
    private final static int FIVE_SEC = 5000;
    private final static int EVERY_POSITION_CHANGE = 0;

    private CompassView compassView;
    private EditText windBearingEditText;
    private EditText windSpeedEditText;
    private SeekBar windSpeedSeekBar;
    private Button sendButton;
    private TextView waitingForGpsTextView;
    
    private RelativeLayout rl_gpsOverlay;
    
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private DecimalFormat speedFormat;
    private DecimalFormat bearingFormat;
	
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View windFragmentView = inflater.inflate(R.layout.wind_view, container, false);
    	
    	compassView = (CompassView) windFragmentView.findViewById(R.id.compassView);
        windBearingEditText = (EditText) windFragmentView.findViewById(R.id.editTextWindDirection);
        windSpeedEditText = (EditText) windFragmentView.findViewById(R.id.editTextWindSpeed);
        windSpeedSeekBar = (SeekBar) windFragmentView.findViewById(R.id.seekbar_wind_speed);
        sendButton = (Button) windFragmentView.findViewById(R.id.btn_wind_send);
        waitingForGpsTextView = (TextView) windFragmentView.findViewById(R.id.textWaitingForGPS);
        rl_gpsOverlay = (RelativeLayout) windFragmentView.findViewById(R.id.rl_gpsOverlay);
        
        return windFragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // http://developer.android.com/training/location/receive-location-updates.html
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(FIVE_SEC);
        locationRequest.setFastestInterval(EVERY_POSITION_CHANGE);

        locationClient = new LocationClient(getActivity(), this, this);

        windSpeedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = round(progress / 10.0f, 1);
                int displayedValue = Integer.getInteger(windSpeedEditText.getText().toString(), 0).intValue();
                int progressValue = Float.valueOf(speed).intValue();
                if (displayedValue != progressValue) {
                    windSpeedEditText.setText(speedFormat.format(speed));
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        
        sendButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try {
                    Wind wind = getResultingWindFix();
                    if (wind == null) {
                        Toast.makeText(getActivity(), R.string.wind_location_or_fields_not_valid, Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    saveEntriesInPreferences(wind);
                    ((RacingActivity)getActivity()).onWindEntered(wind);

                    
                    
                } catch (NumberFormatException nfe) {
                    Toast.makeText(getActivity(), R.string.wind_speed_direction_not_a_valid_number, Toast.LENGTH_LONG).show();
                    ExLog.i(getActivity(), this.getClass().getCanonicalName(), nfe.getMessage());
                }
            }

        });
        sendButton.setEnabled(false);

        windBearingEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String windBearingText = windBearingEditText.getText().toString();
                    if (windBearingText.length() > 0) {
                        compassView.setDirection(Float.valueOf(windBearingText));
                    }
                    
                }
            }
        });
        windSpeedEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String windSpeedText = windSpeedEditText.getText().toString();
                    if (windSpeedText.length() > 0) {
                        windSpeedSeekBar.setProgress(Double.valueOf(windSpeedText).intValue() * 10);
                    }
                }
            }
        });
        speedFormat = new DecimalFormat("#0.0", new DecimalFormatSymbols(Locale.US));
        bearingFormat = new DecimalFormat("###", new DecimalFormatSymbols(Locale.US));

        AppPreferences preferences = AppPreferences.on(getActivity().getApplicationContext());
        double enteredWindSpeed = preferences.getWindSpeed();
        windSpeedSeekBar.setProgress(Double.valueOf(enteredWindSpeed).intValue() * 10);
        windSpeedEditText.setText(speedFormat.format(enteredWindSpeed));
        double enteredWindBearingFrom = preferences.getWindBearingFromDirection();
        compassView.setDirection((float)enteredWindBearingFrom);
        windBearingEditText.setText(bearingFormat.format(enteredWindBearingFrom));
    }
    
    
    
    @Override
    public void onStart() {
        super.onStart();

        locationClient.connect();
        compassView.setDirectionListener(this);
    }
    
    @Override
    public void onPause() {
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
        locationClient.disconnect();
        
        super.onPause();
    }
    
    @Override
    public void onDestroyView() 
    {
       super.onDestroyView(); 
       Fragment fragment = (getFragmentManager().findFragmentById(R.id.windMap));  
       FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
       ft.remove(fragment);
       ft.commit();
   }
    
	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		waitingForGpsTextView.setTextColor(Color.GRAY);
        waitingForGpsTextView.setText(R.string.found_gps_position);
        rl_gpsOverlay.setVisibility(View.INVISIBLE);
        sendButton.setEnabled(true);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		ExLog.e(getActivity(), TAG, "Failed to connect to Google Play Services for location updates");
	}

	@Override
	public void onConnected(Bundle arg0) {
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	@Override
	public void onDisconnected() {
		ExLog.i(getActivity(), TAG, "LocationClient was disconnected");
	}

	@Override
	public void onDirectionChanged(float degree) {
		float direction = round(degree, 0);
        windBearingEditText.setText(bearingFormat.format(direction));
	}

	
	private Wind getResultingWindFix() throws NumberFormatException {
        if (currentLocation == null || windSpeedEditText.getText().toString().isEmpty() || windBearingEditText.getText().toString().isEmpty()) {
            return null;
        }

        Position currentPosition = new DegreePosition(currentLocation.getLatitude(), currentLocation.getLongitude());
        double windSpeed = Double.valueOf(windSpeedEditText.getText().toString().replace(",", "."));
        double windBearing = Double.valueOf(windBearingEditText.getText().toString());
        Bearing bearing_from = new DegreeBearingImpl(windBearing);
        // this is not a standard bearing but the direction where the wind comes from, needs to be converted
        // to match the assumption that a bearing is always the direction the wind flows to
        SpeedWithBearing speedBearing = new KnotSpeedWithBearingImpl(windSpeed, bearing_from.reverse());
        return new WindImpl(currentPosition, MillisecondsTimePoint.now(), speedBearing);
    }
	
	
	// HELPERS
	

	
	protected static float round(float unrounded, int precision) {
        BigDecimal decimal = new BigDecimal(unrounded);
        BigDecimal round = decimal.setScale(precision, BigDecimal.ROUND_UP);
        return round.floatValue();
    }
	
	protected void saveEntriesInPreferences(Wind wind) {
        // Wind.getBearing() returns a value that assumes that the wind flows in that direction
        // But for this app we need to display the direction the wind is coming from
        AppPreferences preferences = AppPreferences.on(getActivity().getApplicationContext());
        preferences.setWindBearingFromDirection(wind.getBearing().reverse().getDegrees());
        preferences.setWindSpeed(wind.getKnots());
    }
}

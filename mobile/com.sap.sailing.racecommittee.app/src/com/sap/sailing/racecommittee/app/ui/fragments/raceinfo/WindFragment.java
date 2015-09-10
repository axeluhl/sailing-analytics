package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.services.polling.RacePositionsPoller;
import com.sap.sailing.racecommittee.app.ui.utils.OnRaceUpdatedListener;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;
import com.sap.sailing.racecommittee.app.utils.RangeInputFilter;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sailing.racecommittee.app.utils.WindHelper;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WindFragment extends BaseFragment
    implements CompassDirectionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
    OnRaceUpdatedListener {

    private final static String TAG = WindFragment.class.getName();
    private final static String START_MODE = "startMode";
    private final static long FIVE_SEC = 5000;
    private final static long EVERY_POSITION_CHANGE = 1000;
    private final static int MIN_KTS = 3;
    private final static int MAX_KTS = 30;
    private final static float MAX_LOCATION_DRIFT_IN_METER = 25f; // 25 meter
    private final static long MAX_LOCATION_DRIFT_IN_MILLIS = 8 * 60 * 60 * 1000; // 8 hours

    private View mHeaderLayout;
    private View mContentLayout;
    private View mMapLayout;

    private TextView mHeaderText;
    private TextView mHeaderWindSensor;
//    private View mWindOn;
//    private View mWindOff;
    private Button mSetData;
    private CompassView mCompassView;
    private NumberPicker mWindSpeed;
    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mAccuracy;
    private TextView mAccuracyTimestamp;
    private EditText mWindInputDirection;
    private EditText mWindInputSpeed;
    private Button mContentMapShow;
    private WebView mMapWebView;
    private Button mMapHide;

    private GoogleApiClient apiClient;
    private LocationRequest locationRequest;
    private Location mCurrentLocation;

    private RacePositionsPoller positionPoller;

    public static WindFragment newInstance(int startMode) {
        WindFragment fragment = new WindFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * creates the string array that represents the numbers in the wind speed number picker
     *
     * @return numbers for wind speed number picker
     */
    private String[] generateNumbers() {
        ArrayList<String> numbers = new ArrayList<>();
        for (float i = MIN_KTS; i <= MAX_KTS; i += .5f) {
            numbers.add(String.format("%.1f %s", i, getString(R.string.wind_kn)));
        }
        return numbers.toArray(new String[numbers.size()]);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the googleApiClient for location requests
        apiClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API).build();

        //register receiver to be notified if race is tracked
        IntentFilter filter = new IntentFilter(AppConstants.INTENT_ACTION_IS_TRACKING);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new IsTrackedReceiver(), filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.wind_view, container, false);

        mHeaderLayout = ViewHelper.get(layout, R.id.header_layout);
        mContentLayout = ViewHelper.get(layout, R.id.content_layout);
        mMapLayout = ViewHelper.get(layout, R.id.map_layout);

        mHeaderText = ViewHelper.get(layout, R.id.header_text);
        mHeaderWindSensor = ViewHelper.get(layout, R.id.wind_sensor);
        // disabled, because of bug #2871
        // mWindOff = ViewHelper.get(layout, R.id.wind_off);
        // mWindOn = ViewHelper.get(layout, R.id.wind_on);
        mSetData = ViewHelper.get(layout, R.id.set_data);
        mCompassView = ViewHelper.get(layout, R.id.compass_view);
        mWindSpeed = ViewHelper.get(layout, R.id.wind_speed);
        mLatitude = ViewHelper.get(layout, R.id.latitude_value);
        mLongitude = ViewHelper.get(layout, R.id.longitude_value);
        mAccuracy = ViewHelper.get(layout, R.id.accuracy_value);
        mAccuracyTimestamp = ViewHelper.get(layout, R.id.accuracy_timestamp);
        mWindInputDirection = ViewHelper.get(layout, R.id.wind_input_direction);
        if (mWindInputDirection != null) {
            mWindInputDirection.setFilters(new InputFilter[] { new RangeInputFilter(0, 360) });
        }
        mWindInputSpeed = ViewHelper.get(layout, R.id.wind_input_speed);
        mContentMapShow = ViewHelper.get(layout, R.id.position_show);
        mMapWebView = ViewHelper.get(layout, R.id.web_view);
        mMapHide = ViewHelper.get(layout, R.id.position_hide);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mHeaderWindSensor != null && getRace() != null && getRaceState() != null && getRaceState().getWindFix() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);
            Wind wind = getRaceState().getWindFix();
            mHeaderWindSensor
                .setText(getString(R.string.wind_sensor, dateFormat.format(wind.getTimePoint().asDate()), wind.getFrom().getDegrees(), wind
                    .getKnots()));
        }

        setupButtons();
        setupWindSpeedPicker();
        setupLayouts(false);

        refreshUI();
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        refreshUI();
    }

    /**
     * refresh location data labels and highlight missing or inaccuracy gps data
     * disable mSetData button if gps data is missing or inaccurate
     */
    private void refreshUI() {
        int whiteColor = ThemeHelper.getColor(getActivity(), R.attr.white);
        int redColor = getActivity().getResources().getColor(R.color.sap_red);
        setTextAndColor(mLatitude, getString(R.string.not_available), redColor);
        setTextAndColor(mLongitude, getString(R.string.not_available), redColor);
        setTextAndColor(mAccuracy, getString(R.string.not_available), redColor);
        setTextAndColor(mAccuracyTimestamp, null, whiteColor);

        mSetData.setEnabled(false);
        if (mCurrentLocation != null) {
            double latitude = mCurrentLocation.getLatitude();
            double longitude = mCurrentLocation.getLongitude();
            float accuracy = mCurrentLocation.getAccuracy();
            long timeDifference = System.currentTimeMillis() - mCurrentLocation.getTime();
            setTextAndColor(mLatitude, getString(R.string.latitude_value, latitude), whiteColor);
            setTextAndColor(mLongitude, getString(R.string.longitude_value, longitude), whiteColor);
            setTextAndColor(mAccuracy, getString(R.string.accuracy_value, mCurrentLocation.getAccuracy()), whiteColor);
            setTextAndColor(mAccuracyTimestamp, getString(R.string.accuracy_timestamp, TimeUtils
                .formatTimeAgo(getActivity(), timeDifference)), whiteColor);

            mSetData.setEnabled(timeDifference <= MAX_LOCATION_DRIFT_IN_MILLIS && accuracy <= MAX_LOCATION_DRIFT_IN_METER);

            // highlight accuracy problem if location is invalid
            if (mAccuracy != null) {
                mAccuracy.setTextColor(mSetData.isEnabled() ? whiteColor : Color.RED);
            }
        }
    }

    private void setTextAndColor(TextView textView, String text, @ColorRes int color) {
        if (textView != null) {
            textView.setText(text);
            textView.setTextColor(color);
        }
    }

    /**
     * starts the googleApiClient to get location updates
     */
    private void resumeApiClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(FIVE_SEC);
        locationRequest.setFastestInterval(EVERY_POSITION_CHANGE);
        apiClient.registerConnectionCallbacks(this);
        apiClient.registerConnectionFailedListener(this);
        apiClient.connect();
    }

    private void pauseApiClient() {
        apiClient.unregisterConnectionFailedListener(this);
        apiClient.unregisterConnectionCallbacks(this);
        apiClient.disconnect();
    }

    /**
     * adds the polling for buoy data to the polled races, also registers a callback
     */
    private void resumePositionPoller() {
        positionPoller = new RacePositionsPoller(getActivity());
        positionPoller.register(getRace(), this);
        ExLog.i(getActivity(), TAG, "registering race " + getRace().getRaceName());
    }

    private void pausePositionPoller() {
        positionPoller.unregisterAllAndStop();
    }

    /**
     * configures all the buttons in the view
     */
    public void setupButtons() {
        if (mHeaderText != null) {
            mHeaderText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMainScheduleFragment();
                }
            });
        }
        if (mSetData != null) {
            mSetData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSendClick();
                }
            });
        }
        if (mContentMapShow != null) {
            // disable functionality at first. will be enabled after contacting the server if race is tracked
            mContentMapShow.setEnabled(false);
            mContentMapShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupLayouts(true);
                }
            });
        }
        if (mMapHide != null) {
            mMapHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupLayouts(false);
                }
            });
        }
    }

    /**
     * configures the wind speed picker views and attaches all relevant listener functions to them
     */
    public void setupWindSpeedPicker() {
        double enteredWindSpeed = preferences.getWindSpeed();
        double enteredWindBearingFrom = preferences.getWindBearingFromDirection();
        if (getRace() != null && getRaceState() != null) {
            Wind enteredWind = getRaceState().getWindFix();
            if (enteredWind != null) {
                enteredWindSpeed = enteredWind.getKnots();
                enteredWindBearingFrom = enteredWind.getFrom().getDegrees();
            }
        }

        if (mWindSpeed != null && mCompassView != null) {
            String numbers[] = generateNumbers();
            ViewHelper.disableSave(mWindSpeed);
            ThemeHelper.setPickerColor(getActivity(), mWindSpeed, ThemeHelper.getColor(getActivity(), R.attr.white), ThemeHelper
                .getColor(getActivity(), R.attr.sap_yellow_1));
            mWindSpeed.setMaxValue(numbers.length - 1);
            mWindSpeed.setMinValue(0);
            mWindSpeed.setWrapSelectorWheel(false);
            mWindSpeed.setDisplayedValues(numbers);
            mWindSpeed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            mCompassView.setDirection((float) enteredWindBearingFrom);
            mWindSpeed.setValue(((int) ((enteredWindSpeed - MIN_KTS) * 2)));
        } else if (mWindInputDirection != null && mWindInputSpeed != null) {
            mWindInputDirection.setText(String.valueOf(enteredWindBearingFrom));
            mWindInputSpeed.setText(String.valueOf(enteredWindSpeed));
        }
    }

    // the map view needs java script
    @SuppressLint("SetJavaScriptEnabled")
    private void setupLayouts(boolean showMap) {
        if (mHeaderLayout != null) {
            if (getArguments() != null && getArguments().getInt(START_MODE, 0) == 1) {
                mHeaderLayout.setVisibility(View.GONE);
            } else {
                mHeaderLayout.setVisibility(showMap ? View.GONE : View.VISIBLE);
            }
        }
        if (mContentLayout != null) {
            mContentLayout.setVisibility(showMap ? View.GONE : View.VISIBLE);
        }
        if (mMapLayout != null) {
            WebSettings settings = mMapWebView.getSettings();
            if (showMap) {
                settings.setJavaScriptEnabled(true);
                loadRaceMap(true, false, false, true);
                mMapLayout.setVisibility(View.VISIBLE);
            } else {
                settings.setJavaScriptEnabled(false);
                mMapLayout.setVisibility(View.GONE);
            }
        }
    }

    private boolean loadRaceMap(boolean showWindCharts, boolean showStreamlets, boolean showSimulation, boolean showMapControls) {
        ManagedRace race = getRace();
        if (race != null) {
            // build complete race map url
            String mapUrl = WindHelper.generateMapURL(getActivity(), race, showWindCharts, showStreamlets, showSimulation, showMapControls);
            mMapWebView.loadUrl(mapUrl);
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mCompassView != null) {
            mCompassView.setDirectionListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // disconnect googleApiClient and unregister position poller
        pauseApiClient();
        pausePositionPoller();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    @Override
    public void onResume() {
        super.onResume();

        // connect googleApiClient and register position poller
        resumeApiClient();
        resumePositionPoller();

        // Contact server and ask if race is tracked and map is allowed to show.
        WindHelper.isTrackedRace(getActivity(), getRace());

        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    /**
     * sends the entered windData to the server
     */
    private void onSendClick() {
        Wind wind = getResultingWindFix();
        getRaceState().setWindFix(MillisecondsTimePoint.now(), wind, /* isMagnetic */ true);
        saveEntriesInPreferences(wind);
        switch (getArguments().getInt(START_MODE, 0)) {
            case 1:
//            sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
                break;

            default:
                openMainScheduleFragment();
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        refreshUI();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        ExLog.e(getActivity(), TAG, "Failed to connect to Google Play Services for location updates");
    }

    @Override
    public void onConnected(Bundle arg0) {
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        refreshUI();
    }

    @Override
    public void onConnectionSuspended(int i) {
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
    }

    @Override
    public void onDirectionChanged(float degree) {
    }

    @Override
    public void OnRaceUpdated(ManagedRace race) {
    }

    private Wind getResultingWindFix() throws NumberFormatException {
        double windSpeed = 0;
        double windBearing = 0;
        Position currentPosition = new DegreePosition(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        if (mCompassView != null && mWindSpeed != null) {
            windBearing = mCompassView.getDirection();
            windSpeed = mWindSpeed.getValue() / 2 + MIN_KTS;
        } else if (mWindInputDirection != null && mWindInputSpeed != null) {
            windBearing = Double.parseDouble(mWindInputDirection.getText().toString());
            windSpeed = Double.parseDouble(mWindInputSpeed.getText().toString());
        }
        Bearing bearing_from = new DegreeBearingImpl(windBearing);
        SpeedWithBearing speedBearing = new KnotSpeedWithBearingImpl(windSpeed, bearing_from.reverse());
        return new WindImpl(currentPosition, MillisecondsTimePoint.now(), speedBearing);
    }

    /**
     * saves the last entered wind in the preferences, so next time wind has to be entered
     * those saved presets get loaded ( unless there was a wind entered for that race already )
     *
     * @param wind the wind to save
     */
    protected void saveEntriesInPreferences(Wind wind) {
        preferences.setWindBearingFromDirection(wind.getBearing().reverse().getDegrees());
        preferences.setWindSpeed(wind.getKnots());
    }

    private class IsTrackedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mContentMapShow != null) {
                mContentMapShow.setEnabled(intent.getBooleanExtra(AppConstants.INTENT_ACTION_IS_TRACKING_EXTRA, false));
            }
        }
    }
}
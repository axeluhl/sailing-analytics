package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sap.sailing.android.shared.logging.ExLog;
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
import com.sap.sailing.racecommittee.app.services.polling.RacePositionsPoller;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class WindFragment extends BaseFragment
    implements CompassDirectionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static String TAG = WindFragment.class.getName();
    private final static String START_MODE = "startMode";
    private final static int FIVE_SEC = 5000;
    private final static int EVERY_POSITION_CHANGE = 0;
    private final static int MIN_KTS = 3;
    private final static int MAX_KTS = 30;

    private View mHeaderLayout;
    private View mContentLayout;
    private View mFooterLayout;

    private View mHeader;
//    private View mWindOn;
//    private View mWindOff;
    private View mSetData;
    private CompassView mCompassView;
    private NumberPicker mWindSpeed;
    private TextView mWindSensor;
    private TextView mPositionLatitude;
    private TextView mPositionLongitude;
    private TextView mPositionAccuracy;
    private Button mPositionShow;
    private Button mPositionHide;
    private WebView mWebView;

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

        // Initialize the googleApiClient for location requests
        apiClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.wind_view, container, false);

        // The fragment is divided into three layouts
        // Header layout is optional and is used for back navigation
        // Content layout holds wind direction, wind speed and location controls
        // Footer layout holds a lazy web view for map presentation
        mHeaderLayout = layout.findViewById(R.id.header_layout);
        mContentLayout = layout.findViewById(R.id.content_layout);
        mFooterLayout = layout.findViewById(R.id.footer_layout);

        mHeader = layout.findViewById(R.id.header_text);
        // disabled, because of bug #2871
        // mWindOff = layout.findViewById(R.id.wind_off);
        // mWindOn = layout.findViewById(R.id.wind_on);
        mSetData = layout.findViewById(R.id.set_data);
        mCompassView = (CompassView) layout.findViewById(R.id.compassView);
        mWindSpeed = (NumberPicker) layout.findViewById(R.id.wind_speed);
        mWindSensor = (TextView) layout.findViewById(R.id.wind_sensor);
        mPositionLatitude = (TextView) layout.findViewById(R.id.position_latitude);
        mPositionLongitude = (TextView) layout.findViewById(R.id.position_longitude);
        mPositionAccuracy = (TextView) layout.findViewById(R.id.position_accuracy);
        mPositionShow = (Button) layout.findViewById(R.id.position_show);
        mPositionHide = (Button) layout.findViewById(R.id.position_hide);
        mWebView = (WebView) layout.findViewById(R.id.web_view);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        if (getArguments() != null) {
//            switch (getArguments().getInt(START_MODE, 0)) {
//            case 1:
//                if (getView() != null) {
//                    View header = getView().findViewById(R.id.header_layout);
//                    header.setVisibility(View.GONE);
//                }
//                break;
//
//            default:
//                break;
//            }
//        }

        if (mWindSensor != null && getRace() != null && getRaceState() != null && getRaceState().getWindFix() != null) {
            String sensorData = getString(R.string.wind_sensor);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);
            Wind wind = getRaceState().getWindFix();
            sensorData = sensorData.replace("#AT#", dateFormat.format(wind.getTimePoint().asDate()));
            sensorData = sensorData.replace("#FROM#", String.format("%.0f", wind.getFrom().getDegrees()));
            sensorData = sensorData.replace("#SPEED#", String.format("%.1f", wind.getKnots()));
            mWindSensor.setText(sensorData);
        }

        setupButtons();
        setupWindSpeedPicker();
        setupLayouts(false);

        setInstanceState(savedInstanceState);
    }

    /**
     * starts the googleApiClient to get location updates
     */
    public void setupLocationClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(FIVE_SEC);
        locationRequest.setFastestInterval(EVERY_POSITION_CHANGE);
        apiClient.connect();
    }

    /**
     * configures all the buttons in the view
     */
    public void setupButtons() {
        if (mHeader != null) {
            mHeader.setOnClickListener(new View.OnClickListener() {
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
        if (mPositionShow != null) {
            mPositionShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupLayouts(true);
                }
            });
        }
        if (mPositionHide != null) {
            mPositionHide.setOnClickListener(new View.OnClickListener() {
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
        String nums[] = generateNumbers();
        ThemeHelper.setPickerTextColor(getActivity(), mWindSpeed, ThemeHelper.getColor(getActivity(), R.attr.white));
        mWindSpeed.setMaxValue(nums.length - 1);
        mWindSpeed.setMinValue(0);
        mWindSpeed.setWrapSelectorWheel(false);
        mWindSpeed.setDisplayedValues(nums);
        mWindSpeed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        double enteredWindSpeed = preferences.getWindSpeed();
        double enteredWindBearingFrom = preferences.getWindBearingFromDirection();

        if (getRace() != null && getRaceState() != null) {
            Wind enteredWind = getRaceState().getWindFix();
            if (enteredWind != null) {
                enteredWindSpeed = enteredWind.getKnots();
                enteredWindBearingFrom = enteredWind.getFrom().getDegrees();
            }
        }

        mCompassView.setDirection((float) enteredWindBearingFrom);
        mWindSpeed.setValue(((int) ((enteredWindSpeed - MIN_KTS) * 2)));
    }

    /**
     * adds the polling for buoy data to the polled races, also registers a callback
     */
    public void setupPositionPoller() {
        positionPoller = new RacePositionsPoller(getActivity());
//        positionPoller.register(getRace(), this);
        ExLog.i(getActivity(), TAG, "registering race " + getRace().getRaceName());
    }

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
        if (mFooterLayout != null) {
            WebSettings settings = mWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            mWebView.loadUrl("http://kielerwoche2015.sapsailing.com/gwt/RaceBoard.html?eventId=a9d6c5d5-cac3-47f2-9b5c-506e441819a1&leaderboardName=KW%202015%20Olympic%20-%20Finn&raceName=R1%20%28Finn%29&viewShowMapControls=false&viewShowNavigationPanel=false&regattaName=KW%202015%20Olympic%20-%20Finn");
            mFooterLayout.setVisibility(showMap ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * function to restore the instanceState via savedInstanceState bundle
     *
     * @param savedInstanceState said bundle
     */
    private void setInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onStart() {
        super.onStart();

        mCompassView.setDirectionListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Disconnect googleApiClient
        if (apiClient.isConnected()) {
            apiClient.unregisterConnectionFailedListener(this);
            apiClient.unregisterConnectionCallbacks(this);
        }
        apiClient.disconnect();

        // Unregister position poller
        positionPoller.unregisterAllAndStop();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    @Override
    public void onResume() {
        super.onResume();

        setupPositionPoller();
        setupLocationClient();

        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        outState.putBoolean("bigMap", bigMap);
//        if (mWindMap != null) {
//            if (mGoogleMap != null) {
//                outState.putDouble("lat", mGoogleMap.getCameraPosition().target.latitude);
//                outState.putDouble("lng", mGoogleMap.getCameraPosition().target.longitude);
//                outState.putFloat("zoom", mGoogleMap.getCameraPosition().zoom);
//            }
//            if (mWindMap.windMarker != null) {
//                outState.putDouble("markerLat", mWindMap.windMarker.getPosition().latitude);
//                outState.putDouble("markerLng", mWindMap.windMarker.getPosition().longitude);
//            }
//        }
//    }

    /**
     * sends the entered windData to the server
     */
    private void onSendClick() {
        Wind wind = getResultingWindFix();
        getRaceState().setWindFix(MillisecondsTimePoint.now(), wind);
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

//    private void setWind(boolean on) {
//        if (mWindOn != null) {
//            mWindOn.setVisibility(on ? View.VISIBLE : View.GONE);
//        }
//        if (mWindOff != null) {
//            mWindOff.setVisibility(!on ? View.VISIBLE : View.GONE);
//        }
//    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mPositionLatitude.setText(String.format("%s %.5f", "Lat: ", location.getLatitude()));
        mPositionLongitude.setText(String.format("%s %.5f", "Lon: ", location.getLongitude()));
        mPositionAccuracy.setText(String.format("%s ~ %.0f m (%s)", "Acc: ", location.getLatitude(), location.getTime()));
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        ExLog.e(getActivity(), TAG, "Failed to connect to Google Play Services for location updates");
    }

    @Override
    public void onConnected(Bundle arg0) {
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onDirectionChanged(float degree) {
    }

    private Wind getResultingWindFix() throws NumberFormatException {
        Position currentPosition = new DegreePosition(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        double windSpeed = mWindSpeed.getValue() / 2 + MIN_KTS;
        double windBearing = mCompassView.getDirection();
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

}
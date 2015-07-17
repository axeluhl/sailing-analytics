package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.impl.SimpleRaceLogIdentifierImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.data.DataManager;
import com.sap.sailing.racecommittee.app.data.DataStore;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.impl.FleetIdentifierImpl;
import com.sap.sailing.racecommittee.app.services.polling.RacePositionsPoller;
import com.sap.sailing.racecommittee.app.ui.utils.OnRaceUpdatedListener;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class WindFragment extends BaseFragment
    implements CompassDirectionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnRaceUpdatedListener {

    private final static String TAG = WindFragment.class.getName();
    private final static String START_MODE = "startMode";
    private final static String GWT_MAP_AND_WIND_CHART_HTML = "/gwt/EmbeddedMapAndWindChart.html";
    private final static long ONE_SEC = 1000;
    private final static long FIVE_SEC = 5000;
    private final static long EVERY_POSITION_CHANGE = 0;
    private final static int MIN_KTS = 3;
    private final static int MAX_KTS = 30;
    private final static float MAX_LOCATION_DRIFT_IN_METER = 25f; // 25 meter
    private final static long MAX_LOCATION_DRIFT_IN_MILLIS = 8 * 60 * 1000; // 8 hours

    private View mHeaderLayout;
    private View mContentLayout;
    private View mMapLayout;

    private TextView mHeaderText;
    private TextView mHeaderWindSensor;
//    private View mWindOn;
//    private View mWindOff;
    private Button mContentSetData;
    private CompassView mContentCompassView;
    private NumberPicker mContentWindSpeed;
    private TextView mContentLatitude;
    private TextView mContentLongitude;
    private TextView mContentAccuracy;
    private Button mContentMapShow;
    private WebView mMapWebView;
    private Button mMapHide;

    private GoogleApiClient apiClient;
    private LocationRequest locationRequest;
    private Location mCurrentLocation;

    private Handler mRefreshUIHandler;
    private Runnable mRefreshUIRunnable;

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

        // refresh ui
        mRefreshUIRunnable = createUIRefreshRunnable();
    }

    private Runnable createUIRefreshRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                refreshUI();
                mRefreshUIHandler.postDelayed(this, ONE_SEC);
            }
        };
    }

    /**
     * refresh location information
     */
    private void refreshUI() {
        if (mCurrentLocation != null) {
            double latitude = mCurrentLocation.getLatitude();
            double longitude = mCurrentLocation.getLongitude();
            float accuracy = mCurrentLocation.getAccuracy();
            long timeDifference = System.currentTimeMillis() - mCurrentLocation.getTime();
            mContentLatitude.setText(String.format("%s %.5f", "Lat: ", latitude));
            mContentLatitude.setTextColor(Color.BLACK);
            mContentLongitude.setText(String.format("%s %.5f", "Lon: ", longitude));
            mContentLongitude.setTextColor(Color.BLACK);
            Date time = new Date(timeDifference);
            DateFormat timeFormatter = new SimpleDateFormat("HH'h'mm'´´'ss'´'", Locale.getDefault());
            timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String timeFormatted = timeFormatter.format(time);
            mContentAccuracy.setText(String.format("%s ~ %.0f m (%s ago)", "Acc: ", mCurrentLocation.getAccuracy(), timeFormatted));
            mContentSetData.setEnabled(timeDifference <= MAX_LOCATION_DRIFT_IN_MILLIS && accuracy <= MAX_LOCATION_DRIFT_IN_METER);

            // highlight accuracy problem if location is invalid
            mContentAccuracy.setTextColor(mContentSetData.isEnabled() ? Color.BLACK : Color.RED);
        } else {
            mContentSetData.setEnabled(false);

            // highlight location problem if no location is available
            mContentLatitude.setTextColor(Color.RED);
            mContentLongitude.setTextColor(Color.RED);
            mContentAccuracy.setTextColor(Color.RED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.wind_view, container, false);

        // the fragment is divided into three layouts
        // header layout is optional and is used for back navigation
        // content layout holds wind direction, wind speed and location controls
        // map layout holds a lazy web view for map presentation
        mHeaderLayout = layout.findViewById(R.id.header_layout);
        mContentLayout = layout.findViewById(R.id.content_layout);
        mMapLayout = layout.findViewById(R.id.map_layout);

        mHeaderText = (TextView) layout.findViewById(R.id.header_text);
        mHeaderWindSensor = (TextView) layout.findViewById(R.id.wind_sensor);
        // disabled, because of bug #2871
        // mWindOff = layout.findViewById(R.id.wind_off);
        // mWindOn = layout.findViewById(R.id.wind_on);
        mContentSetData = (Button) layout.findViewById(R.id.set_data);
        mContentCompassView = (CompassView) layout.findViewById(R.id.compass_view);
        mContentWindSpeed = (NumberPicker) layout.findViewById(R.id.wind_speed);
        mContentLatitude = (TextView) layout.findViewById(R.id.position_latitude);
        mContentLongitude = (TextView) layout.findViewById(R.id.position_longitude);
        mContentAccuracy = (TextView) layout.findViewById(R.id.position_accuracy);
        mContentMapShow = (Button) layout.findViewById(R.id.position_show);
        mMapWebView = (WebView) layout.findViewById(R.id.web_view);
        mMapHide = (Button) layout.findViewById(R.id.position_hide);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mHeaderWindSensor != null && getRace() != null && getRaceState() != null && getRaceState().getWindFix() != null) {
            String sensorData = getString(R.string.wind_sensor);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", getResources().getConfiguration().locale);
            Wind wind = getRaceState().getWindFix();
            sensorData = sensorData.replace("#AT#", dateFormat.format(wind.getTimePoint().asDate()));
            sensorData = sensorData.replace("#FROM#", String.format("%.0f", wind.getFrom().getDegrees()));
            sensorData = sensorData.replace("#SPEED#", String.format("%.1f", wind.getKnots()));
            mHeaderWindSensor.setText(sensorData);
        }

        setupButtons();
        setupWindSpeedPicker();
        setupLayouts(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mRefreshUIHandler = new Handler();
    }

    /**
     * starts the googleApiClient to get location updates
     */
    private void tearUpApiClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(FIVE_SEC);
        locationRequest.setFastestInterval(EVERY_POSITION_CHANGE);
        apiClient.registerConnectionCallbacks(this);
        apiClient.registerConnectionFailedListener(this);
        apiClient.connect();
    }

    private void tearDownApiClient() {
        apiClient.unregisterConnectionFailedListener(this);
        apiClient.unregisterConnectionCallbacks(this);
        apiClient.disconnect();
    }

    /**
     * adds the polling for buoy data to the polled races, also registers a callback
     */
    private void tearUpPositionPoller() {
        positionPoller = new RacePositionsPoller(getActivity());
        positionPoller.register(getRace(), this);
        ExLog.i(getActivity(), TAG, "registering race " + getRace().getRaceName());
    }

    private void tearDownPositionPoller() {
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
        if (mContentSetData != null) {
            mContentSetData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSendClick();
                }
            });
        }
        if (mContentMapShow != null) {
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
        String nums[] = generateNumbers();
        ThemeHelper.setPickerTextColor(getActivity(), mContentWindSpeed, ThemeHelper.getColor(getActivity(), R.attr.white));
        mContentWindSpeed.setMaxValue(nums.length - 1);
        mContentWindSpeed.setMinValue(0);
        mContentWindSpeed.setWrapSelectorWheel(false);
        mContentWindSpeed.setDisplayedValues(nums);
        mContentWindSpeed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        double enteredWindSpeed = preferences.getWindSpeed();
        double enteredWindBearingFrom = preferences.getWindBearingFromDirection();

        if (getRace() != null && getRaceState() != null) {
            Wind enteredWind = getRaceState().getWindFix();
            if (enteredWind != null) {
                enteredWindSpeed = enteredWind.getKnots();
                enteredWindBearingFrom = enteredWind.getFrom().getDegrees();
            }
        }

        mContentCompassView.setDirection((float) enteredWindBearingFrom);
        mContentWindSpeed.setValue(((int) ((enteredWindSpeed - MIN_KTS) * 2)));
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

            // get data store for event id
            DataStore dataStore = DataManager.create(getActivity()).getDataStore();

            // get server base url
            String serverBaseURL = AppPreferences.on(getActivity()).getServerBaseURL();

            // get simple race log identifier
            Util.Triple<String, String, String> triple = FleetIdentifierImpl.unescape(race.getId());
            SimpleRaceLogIdentifier identifier = new SimpleRaceLogIdentifierImpl(triple.getA(), triple.getB(), triple.getC());

            // build complete race map url
            mMapWebView.loadUrl(serverBaseURL + GWT_MAP_AND_WIND_CHART_HTML +
                    "?regattaLikeName=" + identifier.getRegattaLikeParentName() +
                    "&raceColumnName=" + identifier.getRaceColumnName() +
                    "&fleetName=" + identifier.getFleetName() +
                    "&eventId=" + dataStore.getEventUUID() +
                    "&viewShowWindChart=" + showWindCharts +
                    "&viewShowStreamlets=" + showStreamlets +
                    "&viewShowSimulation=" + showSimulation +
                    "&viewShowMapControls=" + showMapControls);
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        mContentCompassView.setDirectionListener(this);

        // start ui refreshing
        if (mRefreshUIHandler != null) {
            mRefreshUIHandler.postDelayed(mRefreshUIRunnable, ONE_SEC);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // disconnect googleApiClient and unregister position poller
        tearDownApiClient();
        tearDownPositionPoller();

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    @Override
    public void onResume() {
        super.onResume();

        // connect googleApiClient and register position poller
        tearUpApiClient();
        tearUpPositionPoller();

        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    @Override
    public void onStop() {
        super.onStop();

        // stop refreshing ui
        if (mRefreshUIHandler != null) {
            mRefreshUIHandler.removeCallbacks(mRefreshUIRunnable);
        }
    }

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
        refreshUI();
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

    @Override
    public void OnRaceUpdated(ManagedRace race) {
    }

    private Wind getResultingWindFix() throws NumberFormatException {
        Position currentPosition = new DegreePosition(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        double windSpeed = mContentWindSpeed.getValue() / 2 + MIN_KTS;
        double windBearing = mContentCompassView.getDirection();
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
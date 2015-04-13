package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.services.polling.RacePositionsPoller;
import com.sap.sailing.racecommittee.app.ui.fragments.maps.WindMap;
import com.sap.sailing.racecommittee.app.ui.utils.OnRaceUpdatedListener;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class WindFragment extends BaseFragment implements CompassDirectionListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, OnClickListener, OnMarkerDragListener,
    OnMapClickListener, OnRaceUpdatedListener, TextView.OnEditorActionListener {

    private final static String TAG = WindFragment.class.getName();
    private final static String START_MODE = "startMode";
    private final static int FIVE_SEC = 5000;
    private final static int EVERY_POSITION_CHANGE = 0;
    private final static int MIN_KTS = 3;
    private final static int MAX_KTS = 30;

    private View mHeader;
    private View mLayoutDirection;
    private View mLayoutSpeed;
    private View mLayoutAddress;
    private View mPositionHeader;
    private View mDarkLayer;
    private View mWindOn;
    private View mWindOff;
    private View mAddressSearch;
    private View mSetData;
    private View mSetPosition;

    private CompassView mCompassView;
    private NumberPicker mWindSpeed;
    private Button mEnterPosition;
    private EditText mAddressInput;
    private TextView mWindSensor;

    private WindMap windMap;

    private GoogleApiClient apiClient;
    private LocationRequest locationRequest;
    private Location mCurrentLocation;

    // stuff to save during rotation/ ect
    private boolean bigMap = false;

    public WindFragment() {
    }

    public static WindFragment newInstance(int startMode) {
        WindFragment fragment = new WindFragment();
        Bundle args = new Bundle();
        args.putInt(START_MODE, startMode);
        fragment.setArguments(args);
        return fragment;
    }

    protected static float round(float number, int precision) {
        BigDecimal decimal = new BigDecimal(number);
        BigDecimal round = decimal.setScale(precision, BigDecimal.ROUND_UP);
        return round.floatValue();
    }

    /**
     * creates the string array that represents the numbers in the windspeed numberpicker
     *
     * @return
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

        apiClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.wind_view, container, false);

        mHeader = layout.findViewById(R.id.header_text);
        mDarkLayer = layout.findViewById(R.id.dark_layer);
        mLayoutDirection = layout.findViewById(R.id.layout_direction);
        mLayoutSpeed = layout.findViewById(R.id.layout_speed);
        mLayoutAddress = layout.findViewById(R.id.address);
        mPositionHeader = layout.findViewById(R.id.position_header);
        mWindOff = layout.findViewById(R.id.wind_off);
        mWindOn = layout.findViewById(R.id.wind_on);
        mAddressSearch = layout.findViewById(R.id.address_search);
        mSetData = layout.findViewById(R.id.set_data);
        mSetPosition = layout.findViewById(R.id.set_position);

        mCompassView = (CompassView) layout.findViewById(R.id.compassView);
        mWindSpeed = (NumberPicker) layout.findViewById(R.id.wind_speed);
        mAddressInput = (EditText) layout.findViewById(R.id.address_input);
        mWindSensor = (TextView) layout.findViewById(R.id.wind_sensor);

        mEnterPosition = (Button) layout.findViewById(R.id.enter_position);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            switch (getArguments().getInt(START_MODE, 0)) {
            case 1:
                if (getView() != null) {
                    View header = getView().findViewById(R.id.header);
                    header.setVisibility(View.GONE);
                }
                break;

            default:
                break;
            }
        }

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
        setupPositionPoller();
        setupMap();
        showElements(true);

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
            mHeader.setOnClickListener(this);
        }
        if (mEnterPosition != null) {
            mEnterPosition.setOnClickListener(this);
        }
        if (mAddressInput != null) {
            mAddressInput.setOnEditorActionListener(this);
        }
        if (mAddressSearch != null) {
            mAddressSearch.setOnClickListener(this);
        }
        if (mSetData != null) {
            mSetData.setOnClickListener(this);
        }
        if (mSetPosition != null) {
            mSetPosition.setOnClickListener(this);
        }
    }

    /**
     * configures the windspeedpicker views and attaches all relevant listener functions to them
     */
    public void setupWindSpeedPicker() {
        String nums[] = generateNumbers();
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
        RacePositionsPoller positionPoller = new RacePositionsPoller(getActivity());
        positionPoller.register(getRace(), this);
        ExLog.i(getActivity(), TAG, "registering race " + getRace().getRaceName());
    }

    /**
     * configures the WindMap-View, including the input field for the position of ones own boat
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setupMap() {
        FragmentManager fragmentManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragmentManager = getChildFragmentManager();
        } else {
            fragmentManager = getFragmentManager();
        }
        windMap = (WindMap) fragmentManager.findFragmentById(R.id.windMap);

        windMap.getMap().setOnMapClickListener(this);
        windMap.getMap().setOnMarkerDragListener(this);
        UiSettings uiSettings = windMap.getMap().getUiSettings();
        uiSettings.setAllGesturesEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        // windMap.setData(mapItems);

        // center the map
        LatLng enteredWindLocation = preferences.getWindPosition();
        if (enteredWindLocation.latitude != 0 && enteredWindLocation.longitude != 0) {
            windMap.movePositionMarker(enteredWindLocation);
            windMap.centerMap(enteredWindLocation);
        }
    }

    /**
     * function to restore the instanceState via savedInstanceState bundle
     *
     * @param savedInstanceState said bundle
     */
    public void setInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            bigMap = savedInstanceState.getBoolean("bigMap", false);
            double markerLat = savedInstanceState.getDouble("markerLat", -1);

            if (bigMap) {
                showBigMap();
                if (savedInstanceState.getDouble("lat", -1) != -1) {
                    windMap.centerMap(savedInstanceState.getDouble("lat"), savedInstanceState.getDouble("lng"),
                        savedInstanceState.getFloat("zoom"));
                    if (markerLat != -1) {
                        windMap.movePositionMarker(new LatLng(markerLat, savedInstanceState.getDouble("markerLng")));
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mCompassView.setDirectionListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (apiClient.isConnected()) {
            apiClient.unregisterConnectionFailedListener(this);
            apiClient.unregisterConnectionCallbacks(this);
        }
        apiClient.disconnect();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.windMap);

        if (fragment != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
        }

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    @Override
    public void onResume() {
        super.onResume();

        setupLocationClient();
        sendIntent(AppConstants.INTENT_ACTION_TIME_HIDE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("bigMap", bigMap);
        if (windMap != null) {
            if (windMap.getMap() != null) {
                outState.putDouble("lat", windMap.getMap().getCameraPosition().target.latitude);
                outState.putDouble("lng", windMap.getMap().getCameraPosition().target.longitude);
                outState.putFloat("zoom", windMap.getMap().getCameraPosition().zoom);
            }
            if (windMap.windMarker != null) {
                outState.putDouble("markerLat", windMap.windMarker.getPosition().latitude);
                outState.putDouble("markerLng", windMap.windMarker.getPosition().longitude);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.header_text:
            if (bigMap) {
                showElements(true);
                bigMap = false;
            } else {
                openMainScheduleFragment();
            }
            break;

        case R.id.enter_position:
            showBigMap();
            break;

        case R.id.address_search:
            if (mAddressInput != null) {
                new GeoCodeTask().execute("" + mAddressInput.getText());
            }
            break;

        case R.id.set_data: {
            onSendClick();
            break;
        }
        case R.id.set_position: {
            onPositionSetClick();
            break;
        }
        default: {
            break;
        }
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
            sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
            break;

        default:
            openMainScheduleFragment();
            break;
        }
    }

    /**
     * saves the boat position for later sendage of the entered windData
     */
    private void onPositionSetClick() {
        bigMap = false;

        windMap.getMap().getUiSettings().setAllGesturesEnabled(false);
        mCurrentLocation = new Location("manual");
        mCurrentLocation.setLatitude(windMap.windMarker.getPosition().latitude);
        mCurrentLocation.setLongitude(windMap.windMarker.getPosition().longitude);
        preferences.setWindPosition(windMap.windMarker.getPosition());
        windMap.getMap().getUiSettings().setAllGesturesEnabled(false);
        windMap.windMarker.setDraggable(false);
        windMap.centerMap(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        showElements(true);
    }

    /**
     * expands the windMap , hides views where the map is supposed to go
     */
    private void showBigMap() {
        showElements(false);
        windMap.getMap().getUiSettings().setAllGesturesEnabled(true);
        if (windMap.windMarker != null) {
            windMap.windMarker.setDraggable(true);
        }
        bigMap = true;
    }

    private void showElements(boolean show) {
        if (mSetData != null) {
            mSetData.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mSetData.setEnabled(
                windMap != null && windMap.windMarker != null && windMap.windMarker.getPosition() != null
                    && mCurrentLocation != null);
        }
        if (mSetPosition != null) {
            mSetPosition.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
        if (mLayoutDirection != null) {
            mLayoutDirection.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (mLayoutSpeed != null) {
            mLayoutSpeed.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (mPositionHeader != null) {
            mPositionHeader.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (mDarkLayer != null) {
            mDarkLayer.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (mLayoutAddress != null) {
            mLayoutAddress.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void setWind(boolean on) {
        if (mWindOn != null) {
            mWindOn.setVisibility(on ? View.VISIBLE : View.GONE);
        }
        if (mWindOff != null) {
            mWindOff.setVisibility(!on ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        windMap.centerMap(location.getLatitude(), location.getLongitude());

        GoogleMap map = windMap.getMap();
        if (map != null) {
            UiSettings uiSettings = map.getUiSettings();
            if (uiSettings != null) {
                uiSettings.setAllGesturesEnabled(false);
            }
        }

        windMap.addAccuracyCircle(location);
        if (windMap.windMarker != null) {
            windMap.windMarker.setDraggable(false);
        }

        if (mDarkLayer != null) {
            mDarkLayer.setVisibility(View.GONE);
        }

        setWind(true);
        showElements(true);
        if (mDarkLayer != null) {
            mDarkLayer.setVisibility(View.GONE);
        }
    }

    /**
     * function gets called after inputting the location where you want to the map to zoom to
     *
     * @param v the view where the location was entered
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
        case EditorInfo.IME_ACTION_DONE:
            new GeoCodeTask().execute("" + v.getText());
            return true;

        default:
            return false;
        }
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mCurrentLocation = new Location("manual");
        mCurrentLocation.setLatitude(marker.getPosition().latitude);
        mCurrentLocation.setLongitude(marker.getPosition().longitude);
        preferences.setWindPosition(marker.getPosition());
    }

    @Override
    public void onMarkerDrag(Marker arg0) {
    }

    @Override
    public void onMapClick(LatLng arg0) {
        if (bigMap) {
            windMap.movePositionMarker(arg0);
        }
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

    @Override
    public void OnRaceUpdated(ManagedRace race) {
        windMap.onMapDataUpdated(race.getMapMarkers());
    }

    /**
     * Async call to googles geocoder web api
     */
    private class GeoCodeTask extends AsyncTask<String, String, JSONObject> {
        String location = "";

        protected JSONObject doInBackground(String... urls) {
            location = urls[0];
            StringBuilder responseBody = new StringBuilder();
            JSONObject locJSON = null;
            try {
                URL website = new URL(
                    getString(R.string.url_google_geocoder) + URLEncoder.encode(location, "UTF-8") + getString(
                        R.string.urlpart_google_geocoder_sensor));
                ExLog.i(getActivity(), TAG, website.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) website.openConnection();
                BufferedReader in;
                try {
                    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        responseBody.append(line);
                    }
                    JSONObject jObject = new JSONObject(responseBody + "");
                    locJSON = jObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location");
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                ExLog.ex(WindFragment.this.getActivity(), TAG, e);
            }

            return (locJSON);
        }

        protected void onPostExecute(JSONObject locJSON) {
            if (locJSON == null) {
                ExLog.i(getActivity(), TAG, "No Location found for " + location);
                Toast.makeText(getActivity(), R.string.no_location_found, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                ExLog.i(getActivity(), TAG,
                    "Location found for " + location + ": " + locJSON.getDouble("lat") + "," + locJSON
                        .getDouble("lng"));
                windMap.centerMap(locJSON.getDouble("lat"), locJSON.getDouble("lng"));
            } catch (JSONException e) {
                ExLog.ex(WindFragment.this.getActivity(), TAG, e);
            }
        }
    }
}
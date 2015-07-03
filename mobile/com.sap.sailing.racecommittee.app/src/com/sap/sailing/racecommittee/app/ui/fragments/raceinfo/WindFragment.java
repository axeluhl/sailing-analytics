package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.sap.sailing.racecommittee.app.ui.fragments.maps.WindMap;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

    private Context mContext;

    private View mHeaderLayout;
    private View mContentLayout;
    private View mFooterLayout;

    private View mHeader;
    private View mLayoutDirection;
    private View mLayoutSpeed;
    private View mPositionHeader;
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

        mContext = inflater.getContext();

        mHeaderLayout = layout.findViewById(R.id.header_layout);
        mContentLayout = layout.findViewById(R.id.content_layout);
        mFooterLayout = layout.findViewById(R.id.footer_layout);

        mHeader = layout.findViewById(R.id.header_text);
        mLayoutDirection = layout.findViewById(R.id.layout_direction);
        mLayoutSpeed = layout.findViewById(R.id.layout_speed);
        mPositionHeader = layout.findViewById(R.id.position_header);
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

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            switch (getArguments().getInt(START_MODE, 0)) {
            case 1:
                if (getView() != null) {
                    View header = getView().findViewById(R.id.header_layout);
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
            mHeader.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMainScheduleFragment();
                }
            });
        }
        if (mSetData != null) {
            mSetData.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSendClick();
                }
            });
        }
        if (mPositionShow != null) {
            mPositionShow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMap(true);
                }
            });
        }
        if (mPositionHide != null) {
            mPositionHide.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMap(false);
                }
            });
        }
    }

    /**
     * configures the windspeedpicker views and attaches all relevant listener functions to them
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

    /**
     * function to restore the instanceState via savedInstanceState bundle
     *
     * @param savedInstanceState said bundle
     */
    public void setInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
//            bigMap = savedInstanceState.getBoolean("bigMap", false);
//            double markerLat = savedInstanceState.getDouble("markerLat", -1);
//
//            if (bigMap) {
//                showBigMap();
//                if (mWindMap != null && savedInstanceState.getDouble("lat", -1) != -1) {
//                    mWindMap.centerMap(savedInstanceState.getDouble("lat"), savedInstanceState.getDouble("lng"), savedInstanceState.getFloat("zoom"));
//                    if (markerLat != -1) {
//                        mWindMap.movePositionMarker(new LatLng(markerLat, savedInstanceState.getDouble("markerLng")));
//                    }
//                }
//            }
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

        positionPoller.unregisterAllAndStop();

//        Fragment fragment = getFragmentManager().findFragmentById(R.id.windMap);
//        if (fragment != null) {
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//            transaction.remove(fragment);
//            transaction.commit();
//        }

        sendIntent(AppConstants.INTENT_ACTION_TIME_SHOW);
    }

    @Override
    public void onResume() {
        super.onResume();

        setupPositionPoller();
        setupLocationClient();
//        initialMapFragment();
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

//    /**
//     * saves the boat position for later sendage of the entered windData
//     */
//    private void onPositionSetClick() {
//        bigMap = false;
//
//        if (mWindMap != null && mWindMap.windMarker != null) { // && mGoogleMap != null) {
//            mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
//            mCurrentLocation = new Location("manual");
//            mCurrentLocation.setLatitude(mWindMap.windMarker.getPosition().latitude);
//            mCurrentLocation.setLongitude(mWindMap.windMarker.getPosition().longitude);
//            preferences.setWindPosition(mWindMap.windMarker.getPosition());
//            mWindMap.windMarker.setDraggable(false);
//            mWindMap.centerMap(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//        }
//        showElements(true);
//    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//    private void initialMapFragment() {
//        mWindMap = WindMap.newInstance(mContext, this);
//        FragmentManager manager;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            manager = getChildFragmentManager();
//        } else {
//            manager = getFragmentManager();
//        }
//        if (manager.findFragmentByTag("googleMap") == null) {
//            FragmentTransaction ft = manager.beginTransaction();
//            ft.replace(R.id.windMap, mWindMap, "googleMap").commit();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                manager.executePendingTransactions();
//                mWindMap.getMapAsync(this);
//            }
//        }
//    }

//    /**
//     * expands the windMap , hides views where the map is supposed to go
//     */
//    private void showBigMap() {
//        showElements(false);
//        if (mWindMap != null && mGoogleMap != null) {
//            mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
//            if (mWindMap.windMarker != null) {
//                mWindMap.windMarker.setDraggable(true);
//            }
//        }
//        bigMap = true;
//    }

    private void showMap(boolean show) {
        if (mHeaderLayout != null) {
            mHeaderLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (mContentLayout != null) {
            mContentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (mFooterLayout != null) {
            mFooterLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        }

//        if (mSetData != null) {
//            mSetData.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
//            // TODO check location
//            mSetData
//                .setEnabled(mWindMap != null && mWindMap.windMarker != null && mWindMap.windMarker.getPosition() != null && mCurrentLocation != null);
//        }
//        if (mLayoutDirection != null) {
//            mLayoutDirection.setVisibility(show ? View.VISIBLE : View.GONE);
//        }
//        if (mLayoutSpeed != null) {
//            mLayoutSpeed.setVisibility(show ? View.VISIBLE : View.GONE);
//        }
//        if (mPositionHeader != null) {
//            mPositionHeader.setVisibility(show ? View.VISIBLE : View.GONE);
//        }
//        if (mDarkLayer != null) {
//            mDarkLayer.setVisibility(show ? View.VISIBLE : View.GONE);
//        }
//        if (mLayoutAddress != null) {
//            mLayoutAddress.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
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
//        showElements(true);
//        if (mDarkLayer != null) {
//            mDarkLayer.setVisibility(View.GONE);
//        }
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
                URL website = new URL(getString(R.string.url_google_geocoder) + URLEncoder.encode(location, "UTF-8")
                    + getString(R.string.urlpart_google_geocoder_sensor));
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
                    locJSON = jObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
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
                ExLog.i(getActivity(), TAG, "Location found for " + location + ": " + locJSON.getDouble("lat") + "," + locJSON.getDouble("lng"));
//                if (mWindMap != null) {
//                    mWindMap.centerMap(locJSON.getDouble("lat"), locJSON.getDouble("lng"));
//                }
            } catch (JSONException e) {
                ExLog.ex(WindFragment.this.getActivity(), TAG, e);
            }
        }
    }
}
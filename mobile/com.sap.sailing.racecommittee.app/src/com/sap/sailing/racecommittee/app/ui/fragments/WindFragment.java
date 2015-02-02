package com.sap.sailing.racecommittee.app.ui.fragments;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.NumberPicker.OnValueChangeListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.services.polling.RacePositionsPoller;
import com.sap.sailing.racecommittee.app.ui.activities.RacingActivity;
import com.sap.sailing.racecommittee.app.ui.fragments.maps.WindMap;
import com.sap.sailing.racecommittee.app.ui.utils.OnRaceUpdatedListener;
import com.sap.sailing.racecommittee.app.ui.views.CompassView;
import com.sap.sailing.racecommittee.app.ui.views.CompassView.CompassDirectionListener;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class WindFragment extends LoggableFragment
        implements CompassDirectionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnClickListener, OnMarkerDragListener, OnMapClickListener, OnRaceUpdatedListener,
        TextView.OnEditorActionListener, OnFocusChangeListener {

    private final static String TAG = WindFragment.class.getName();
    private final static int FIVE_SEC = 5000;
    private final static int EVERY_POSITION_CHANGE = 0;
    private final static int MAX_KTS = 50;

    private RacingActivity activity;
    private CompassView compassView;
    private EditText windBearingEditText;
    private EditText windSpeedEditText;
    private NumberPicker np_windSpeed;
    private Button btn_sendButton;
    private Button btn_position_set;
    private Button btn_set_manual_position;
    private TextView txt_waitingForGPS;

    private LinearLayout ll_topContainer;

    private WindMap windMap;
    private RelativeLayout rl_gpsOverlay;
    private EditText et_location;

    private GoogleApiClient apiClient;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private DecimalFormat bearingFormat;

    // stuff to save during rotation/ ect
    private boolean bigMap = false;
    private ManagedRace race;

    public WindFragment() {
    }

    public WindFragment(ManagedRace race) {
        this.race = race;
    }

    protected static float round(float unrounded, int precision) {
        BigDecimal decimal = new BigDecimal(unrounded);
        BigDecimal round = decimal.setScale(precision, BigDecimal.ROUND_UP);
        return round.floatValue();
    }

    /**
     * creates the string array that represents the numbers in the windspeed numberpicker
     *
     * @return
     */
    private static String[] generateNumbers() {
        String nums[] = new String[MAX_KTS * 2 + 1];

        for (int i = 0; i < MAX_KTS * 2 + 1; i = i + 2) {
            if (i == 0) {
                nums[i] = i + "";
            } else {
                nums[i] = i / 2 + "";
            }

            if (i < MAX_KTS * 2)
                nums[i + 1] = i / 2 + ",5";
        }

        return nums;
    }

    /**
     * set the color of the text of a numberpicker
     *
     * @param numberPicker
     * @param color
     * @return boolean if successful
     */
    public static boolean setNumberPickerTextColor(NumberPicker numberPicker,
                                                   int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass().getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    // ExLog.w("setNumberPickerTextColor",this.getClass().getCanonicalName(),
                    // e);
                }
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.wind_view, container, false);
        compassView = (CompassView) view.findViewById(R.id.compassView);
        windBearingEditText = (EditText) view.findViewById(R.id.editTextWindDirection);
        windSpeedEditText = (EditText) view.findViewById(R.id.editTextWindSpeed);
        np_windSpeed = (NumberPicker) view.findViewById(R.id.np_windSpeed);
        btn_sendButton = (Button) view.findViewById(R.id.btn_wind_send);
        txt_waitingForGPS = (TextView) view.findViewById(R.id.txt_waitingForGPS);
        ll_topContainer = (LinearLayout) view.findViewById(R.id.ll_topContainer);
        rl_gpsOverlay = (RelativeLayout) view.findViewById(R.id.rl_gpsOverlay);
        et_location = (EditText) view.findViewById(R.id.et_location);

        btn_set_manual_position = (Button) view.findViewById(R.id.btn_set_manual_position);
        btn_position_set = (Button) view.findViewById(R.id.btn_position_set);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = (RacingActivity) getActivity();

        setupButtons();
        setupWindSpeedPicker();

        if (race != null) {
            //ExLog.i(getActivity(),TAG, "race != null :)");
            setupPositionPoller();
        }

        setupMap();

        setInstanceState(savedInstanceState);

        if (activity != null) {
            activity.setRightPanelVisibility(View.GONE);
        }
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
        btn_sendButton.setOnClickListener(this);
        btn_sendButton.setEnabled(false);
        btn_set_manual_position.setOnClickListener(this);
        btn_position_set.setEnabled(false);
        btn_position_set.setOnClickListener(this);
    }

    /**
     * configures the windspeedpicker views and attaches all relevant listener functions to them
     */
    public void setupWindSpeedPicker() {
        AppPreferences preferences = AppPreferences.on(activity.getApplicationContext());
        String nums[] = generateNumbers();
        np_windSpeed.setMaxValue(nums.length - 1);
        np_windSpeed.setMinValue(0);
        np_windSpeed.setWrapSelectorWheel(false);
        np_windSpeed.setDisplayedValues(nums);
        np_windSpeed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        np_windSpeed.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal,
                                      int newVal) {
                String text = (float) newVal / 2f + "";
                windSpeedEditText.setText(text);
            }
        });
        windBearingEditText.setOnFocusChangeListener(this);
        setNumberPickerTextColor(np_windSpeed, Color.BLACK);
        DecimalFormat speedFormat = new DecimalFormat("#0.0", new DecimalFormatSymbols(Locale.US));
        bearingFormat = new DecimalFormat("###", new DecimalFormatSymbols(Locale.US));

        double enteredWindSpeed = preferences.getWindSpeed();
        double enteredWindBearingFrom = preferences.getWindBearingFromDirection();

        if (race != null) {
            Wind enteredWind = race.getState().getWindFix();
            if (enteredWind != null) {
                enteredWindSpeed = enteredWind.getKnots();
                enteredWindBearingFrom = enteredWind.getFrom().getDegrees();
            }
        }

        windSpeedEditText.setText(speedFormat.format(enteredWindSpeed));

        compassView.setDirection((float) enteredWindBearingFrom);
        windBearingEditText.setText(bearingFormat.format(enteredWindBearingFrom));
        np_windSpeed.setValue(((int) (enteredWindSpeed * 2)));
    }

    /**
     * adds the polling for buoy data to the polled races, also registers a callback
     */
    public void setupPositionPoller() {
        RacePositionsPoller positionPoller = new RacePositionsPoller(activity.getApplicationContext());
        positionPoller.register(race, this);
        ExLog.i(activity, TAG, "registering race " + race.getRaceName());
    }

    /**
     * configures the WindMap-View, including the input field for the position of ones own boat
     */
    public void setupMap() {
        AppPreferences preferences = AppPreferences.on(activity.getApplicationContext());
        // map location search input
        et_location.setOnEditorActionListener(this);

        FragmentManager fragmentManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragmentManager = getChildFragmentManager();
        } else {
            fragmentManager = activity.getFragmentManager();
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
            btn_position_set.setEnabled(true);
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
                    windMap.centerMap(savedInstanceState.getDouble("lat"), savedInstanceState.getDouble("lng"), savedInstanceState.getFloat("zoom"));
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
        compassView.setDirectionListener(this);
    }

    @Override
    public void onPause() {
        if (apiClient.isConnected()) {
            apiClient.unregisterConnectionFailedListener(this);
            apiClient.unregisterConnectionCallbacks(this);
        }
        apiClient.disconnect();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.windMap);

        if (fragment != null) {
            FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupLocationClient();
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
            case R.id.btn_wind_send: {
                onSendClick();
                break;
            }
            case R.id.btn_set_manual_position: {
                onSetPositionClick();
                break;
            }
            case R.id.btn_position_set: {
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
        try {
            Wind wind = getResultingWindFix();
            if (wind == null) {
                Toast.makeText(activity, R.string.wind_location_or_fields_not_valid, Toast.LENGTH_LONG).show();
                return;
            }
            saveEntriesInPreferences(wind);
            activity.onWindEntered(wind);
        } catch (NumberFormatException nfe) {
            Toast.makeText(activity, R.string.wind_speed_direction_not_a_valid_number, Toast.LENGTH_LONG).show();
            ExLog.i(activity, this.getClass().getCanonicalName(),
                    nfe.getMessage());
        }
    }

    /**
     * switches interface for entry of positional data entry and setups the map
     */
    private void onSetPositionClick() {
        int shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);
        bigMap = true;

        txt_waitingForGPS.setText(R.string.manual_entry);
        txt_waitingForGPS.setTextColor(Color.GRAY);

        hideView(ll_topContainer, shortAnimationDuration);
        hideView(rl_gpsOverlay, shortAnimationDuration);
        showView(et_location, shortAnimationDuration);
        btn_sendButton.setVisibility(View.GONE);
        btn_position_set.setVisibility(View.VISIBLE);
        windMap.getMap().getUiSettings().setAllGesturesEnabled(true);
        if (windMap.windMarker != null) {
            windMap.windMarker.setDraggable(true);
        }
    }

    /**
     * saves the boat position for later sendage of the entered windData
     */
    private void onPositionSetClick() {
        int shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_mediumAnimTime);
        bigMap = false;

        windMap.getMap().getUiSettings().setAllGesturesEnabled(false);
        currentLocation = new Location("set");
        currentLocation.setLatitude(windMap.windMarker.getPosition().latitude);
        currentLocation.setLongitude(windMap.windMarker.getPosition().longitude);
        AppPreferences preferences = AppPreferences.on(activity.getApplicationContext());
        preferences.setWindPosition(windMap.windMarker.getPosition());
        txt_waitingForGPS.setTextColor(Color.GRAY);
        btn_sendButton.setEnabled(true);

        showView(ll_topContainer, shortAnimationDuration);
        showView(rl_gpsOverlay, shortAnimationDuration);
        hideView(et_location, shortAnimationDuration);
        btn_sendButton.setVisibility(View.VISIBLE);
        btn_position_set.setVisibility(View.GONE);
        windMap.getMap().getUiSettings().setAllGesturesEnabled(false);
        windMap.windMarker.setDraggable(false);
        windMap.centerMap(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    /**
     * expands the windMap , hides views where the map is supposed to go
     */
    private void showBigMap() {
        ll_topContainer.setVisibility(View.GONE);
        rl_gpsOverlay.setVisibility(View.GONE);
        et_location.setVisibility(View.VISIBLE);
        et_location.setAlpha(1f);
        btn_sendButton.setVisibility(View.GONE);
        btn_position_set.setVisibility(View.VISIBLE);
        windMap.getMap().getUiSettings().setAllGesturesEnabled(true);
        if (windMap.windMarker != null) {
            windMap.windMarker.setDraggable(true);
        }
    }

    /**
     * animates a view out of its existance ( fade out )
     *
     * @param v                 the view to fade
     * @param animationDuration the duration of the fade
     */
    private void hideView(final View v, int animationDuration) {
        v.animate().alpha(0f).setDuration(animationDuration)
                .setListener(new AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                });
    }

    /**
     * animates a view back into existance ( fade in )
     *
     * @param v                 the view to fade
     * @param animationDuration the duration of the fade
     */
    private void showView(final View v, int animationDuration) {
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1f).setDuration(animationDuration).setListener(null);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        windMap.centerMap(location.getLatitude(), location.getLongitude());
        windMap.getMap().getUiSettings().setAllGesturesEnabled(false);
        windMap.addAccuracyCircle(location);
        txt_waitingForGPS.setTextColor(Color.GRAY);
        txt_waitingForGPS.setText(R.string.found_gps_position);
        rl_gpsOverlay.setVisibility(View.GONE);
        btn_sendButton.setEnabled(true);
        if (windMap.windMarker != null) {
            windMap.windMarker.setDraggable(false);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String windBearingText = windBearingEditText.getText().toString();
            if (windBearingText.length() > 0) {
                compassView.setDirection(Float.valueOf(windBearingText));
            }
        }
    }

    /**
     * function gets called after inputting the location where you want to the map to zoom to
     *
     * @param v the view where the location was entered
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            new GeoCodeTask().execute("" + v.getText());
        }
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        currentLocation = new Location("set");
        currentLocation.setLatitude(marker.getPosition().latitude);
        currentLocation.setLongitude(marker.getPosition().longitude);
        AppPreferences preferences = AppPreferences.on(activity.getApplicationContext());
        preferences.setWindPosition(marker.getPosition());

    }

    @Override
    public void onMarkerDrag(Marker arg0) {
    }

    @Override
    public void onMapClick(LatLng arg0) {
        if (bigMap)
            windMap.movePositionMarker(arg0);
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        ExLog.e(activity, TAG, "Failed to connect to Google Play Services for location updates");
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
        float direction = round(degree, 0);
        windBearingEditText.setText(bearingFormat.format(direction));
    }

    private Wind getResultingWindFix() throws NumberFormatException {
        if (currentLocation == null || TextUtils.isEmpty(windSpeedEditText.getText())
                || TextUtils.isEmpty(windBearingEditText.getText())) {
            return null;
        }

        Position currentPosition = new DegreePosition(currentLocation.getLatitude(), currentLocation.getLongitude());
        double windSpeed = Double.valueOf(windSpeedEditText.getText().toString().replace(",", "."));
        double windBearing = Double.valueOf(windBearingEditText.getText().toString());
        Bearing bearing_from = new DegreeBearingImpl(windBearing);
        // this is not a standard bearing but the direction where the wind comes
        // from, needs to be converted
        // to match the assumption that a bearing is always the direction the
        // wind flows to
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
        // Wind.getBearing() returns a value that assumes that the wind flows in
        // that direction
        // But for this app we need to display the direction the wind is coming
        // from
        AppPreferences preferences = AppPreferences.on(activity.getApplicationContext());
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
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                URI website = new URI(getString(R.string.url_google_geocoder)
                        + URLEncoder.encode(location, "UTF-8")
                        + getString(R.string.urlpart_google_geocoder_sensor));
                ExLog.i(activity, 1, website.toString());
                request.setURI(website);
                HttpResponse response = httpclient.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = in.readLine()) != null) {
                    responseBody.append(line);
                }
                JSONObject jObject = new JSONObject(responseBody + "");
                locJSON = jObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return (locJSON);
        }

        protected void onPostExecute(JSONObject locJSON) {
            if (locJSON == null) {
                ExLog.i(activity, this.getClass().getCanonicalName(),
                        "No Location found for " + location);
                Toast.makeText(activity, R.string.no_location_found,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                ExLog.i(activity, this.getClass().getCanonicalName(), "Location found for " + location + ": " + locJSON.getDouble("lat") + "," + locJSON.getDouble("lng"));
                windMap.centerMap(locJSON.getDouble("lat"), locJSON.getDouble("lng"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
package com.sap.sailing.racecommittee.app.ui.fragments;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class WindFragment extends LoggableFragment implements CompassDirectionListener, ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, OnClickListener, OnMarkerDragListener, OnMapClickListener,
        TextView.OnEditorActionListener, OnFocusChangeListener {

    private final static String TAG = WindFragment.class.getName();
    private final static int FIVE_SEC = 5000;
    private final static int EVERY_POSITION_CHANGE = 0;
    private final static int MAX_KTS = 50;

    private CompassView compassView;
    private EditText windBearingEditText;
    private EditText windSpeedEditText;
    // private SeekBar windSpeedSeekBar;
    private NumberPicker np_windSpeed;
    private Button sendButton;
    private Button btn_position_set;
    private Button btn_set_manual_position;
    private TextView txt_waitingForGPS;

    private LinearLayout ll_topContainer;
    private GoogleMap windMap;
    @SuppressWarnings("unused")
    private RelativeLayout rl_bottomContainer;
    private RelativeLayout rl_gpsOverlay;
    private EditText et_location;

    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private DecimalFormat speedFormat;
    private DecimalFormat bearingFormat;

    // stuff to save during rotation/ ect
    private boolean bigMap = false;
    private Marker windMarker;
    private Circle windCircle;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View windFragmentView = inflater.inflate(R.layout.wind_view, container, false);

        compassView = (CompassView) windFragmentView.findViewById(R.id.compassView);
        windBearingEditText = (EditText) windFragmentView.findViewById(R.id.editTextWindDirection);
        windSpeedEditText = (EditText) windFragmentView.findViewById(R.id.editTextWindSpeed);
        // windSpeedSeekBar = (SeekBar) windFragmentView
        // .findViewById(R.id.seekbar_wind_speed);
        np_windSpeed = (NumberPicker) windFragmentView.findViewById(R.id.np_windSpeed);
        sendButton = (Button) windFragmentView.findViewById(R.id.btn_wind_send);
        txt_waitingForGPS = (TextView) windFragmentView.findViewById(R.id.txt_waitingForGPS);
        ll_topContainer = (LinearLayout) windFragmentView.findViewById(R.id.ll_topContainer);
        rl_bottomContainer = (RelativeLayout) windFragmentView.findViewById(R.id.rl_bottomContainer);
        rl_gpsOverlay = (RelativeLayout) windFragmentView.findViewById(R.id.rl_gpsOverlay);
        et_location = (EditText) windFragmentView.findViewById(R.id.et_location);

        FragmentManager fragmentManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            fragmentManager = getChildFragmentManager();
        } else {
            fragmentManager = getActivity().getFragmentManager();
        }
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.windMap);
        windMap = mapFragment.getMap();
        windMap.getUiSettings().setAllGesturesEnabled(false);

        btn_set_manual_position = (Button) windFragmentView.findViewById(R.id.btn_set_manual_position);
        btn_position_set = (Button) windFragmentView.findViewById(R.id.btn_position_set);

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

        // buttons
        sendButton.setOnClickListener(this);
        sendButton.setEnabled(false);
        btn_set_manual_position.setOnClickListener(this);
        btn_position_set.setEnabled(false);
        btn_position_set.setOnClickListener(this);

        // windspeed picker
        String nums[] = generateNumbers();
        np_windSpeed.setMaxValue(nums.length - 1);
        np_windSpeed.setMinValue(0);
        np_windSpeed.setWrapSelectorWheel(false);
        np_windSpeed.setDisplayedValues(nums);
        np_windSpeed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        np_windSpeed.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                String text = (float) newVal / 2f + "";
                windSpeedEditText.setText(text);
            }
        });

        // map suche
        et_location.setOnEditorActionListener(this);

        windBearingEditText.setOnFocusChangeListener(this);

        speedFormat = new DecimalFormat("#0.0", new DecimalFormatSymbols(Locale.US));
        bearingFormat = new DecimalFormat("###", new DecimalFormatSymbols(Locale.US));

        AppPreferences preferences = AppPreferences.on(getActivity().getApplicationContext());
        double enteredWindSpeed = preferences.getWindSpeed();

        windSpeedEditText.setText(speedFormat.format(enteredWindSpeed));

        double enteredWindBearingFrom = preferences.getWindBearingFromDirection();
        compassView.setDirection((float) enteredWindBearingFrom);
        windBearingEditText.setText(bearingFormat.format(enteredWindBearingFrom));
        np_windSpeed.setValue(((int) (Double.valueOf(enteredWindSpeed) * 2)));
        LatLng enteredWindLocation = preferences.getWindPosition();
        if (enteredWindLocation.latitude != 0 && enteredWindLocation.longitude != 0) {
            moveMarker(enteredWindLocation);
            centerMap(enteredWindLocation);
        }

        windMap.setOnMapClickListener(this);
        windMap.setOnMarkerDragListener(this);

        double markerLat = -1;
        if (savedInstanceState != null) {
            bigMap = savedInstanceState.getBoolean("bigMap", false);
            markerLat = savedInstanceState.getDouble("markerLat", -1);
        }

        if (bigMap) {
            showBigMap();
            centerMap(savedInstanceState.getDouble("lat"), savedInstanceState.getDouble("lng"),
                    savedInstanceState.getFloat("zoom"));
            if (markerLat != -1) {
                moveMarker(new LatLng(markerLat, savedInstanceState.getDouble("markerLng")));
            }
        } else if (windMarker != null) {
            windMarker.setDraggable(false);
        }
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

        Fragment fragment = (getFragmentManager().findFragmentById(R.id.windMap));

        if (fragment instanceof Fragment) {
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();
        }

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("bigMap", bigMap);
        outState.putDouble("lat", windMap.getCameraPosition().target.latitude);
        outState.putDouble("lng", windMap.getCameraPosition().target.longitude);
        outState.putFloat("zoom", windMap.getCameraPosition().zoom);
        if (windMarker != null) {
            outState.putDouble("markerLat", windMarker.getPosition().latitude);
            outState.putDouble("markerLng", windMarker.getPosition().longitude);
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

    private void onSendClick() {
        try {
            Wind wind = getResultingWindFix();
            if (wind == null) {
                Toast.makeText(getActivity(), R.string.wind_location_or_fields_not_valid, Toast.LENGTH_LONG).show();
                return;
            }
            saveEntriesInPreferences(wind);
            ((RacingActivity) getActivity()).onWindEntered(wind);
        } catch (NumberFormatException nfe) {
            Toast.makeText(getActivity(), R.string.wind_speed_direction_not_a_valid_number, Toast.LENGTH_LONG).show();
            ExLog.i(getActivity(), this.getClass().getCanonicalName(), nfe.getMessage());
        }
    }

    private void onSetPositionClick() {
        int shortAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        bigMap = true;

        txt_waitingForGPS.setText(R.string.manual_entry);
        txt_waitingForGPS.setTextColor(Color.GRAY);

        hideView(ll_topContainer, shortAnimationDuration);
        hideView(rl_gpsOverlay, shortAnimationDuration);
        showView(et_location, shortAnimationDuration);
        sendButton.setVisibility(View.GONE);
        btn_position_set.setVisibility(View.VISIBLE);
        windMap.getUiSettings().setAllGesturesEnabled(true);
        if (windMarker != null) {
            windMarker.setDraggable(true);
        }
    }

    private void onPositionSetClick() {
        int shortAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        bigMap = false;

        windMap.getUiSettings().setAllGesturesEnabled(false);
        currentLocation = new Location("set");
        currentLocation.setLatitude(windMarker.getPosition().latitude);
        currentLocation.setLongitude(windMarker.getPosition().longitude);
        AppPreferences preferences = AppPreferences.on(getActivity().getApplicationContext());
        preferences.setWindPosition(windMarker.getPosition());
        txt_waitingForGPS.setTextColor(Color.GRAY);
        sendButton.setEnabled(true);

        showView(ll_topContainer, shortAnimationDuration);
        showView(rl_gpsOverlay, shortAnimationDuration);
        hideView(et_location, shortAnimationDuration);
        sendButton.setVisibility(View.VISIBLE);
        btn_position_set.setVisibility(View.GONE);
        windMap.getUiSettings().setAllGesturesEnabled(false);
        windMarker.setDraggable(false);
        centerMap(currentLocation.getLatitude(), currentLocation.getLongitude());
    }

    private class GeoCodeTask extends AsyncTask<String, String, JSONObject> {
        String location = "";

        protected JSONObject doInBackground(String... urls) {
            location = urls[0];
            StringBuilder responseBody = new StringBuilder();
            JSONObject locJSON = null;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                URI website = new URI(getString(R.string.url_google_geocoder) + URLEncoder.encode(location, "UTF-8")
                        + getString(R.string.urlpart_google_geocoder_sensor));
                ExLog.i(getActivity(), 1, website.toString());
                request.setURI(website);
                HttpResponse response = httpclient.execute(request);
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = in.readLine()) != null) {
                    responseBody.append(line);
                }
                JSONObject jObject = new JSONObject(responseBody + "");
                locJSON = jObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return (locJSON);
        }

        protected void onPostExecute(JSONObject locJSON) {
            if (locJSON == null) {
                ExLog.i(getActivity(), this.getClass().getCanonicalName(), "No Location found for " + location);
                Toast.makeText(getActivity(), R.string.no_location_found, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                ExLog.i(getActivity(), this.getClass().getCanonicalName(), "Location found for " + location + ": "
                        + locJSON.getDouble("lat") + "," + locJSON.getDouble("lng"));
                centerMap(locJSON.getDouble("lat"), locJSON.getDouble("lng"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void centerMap(double lat, double lng, float zoom) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
        CameraUpdate czoom = CameraUpdateFactory.zoomTo(zoom);
        windMap.moveCamera(center);
        windMap.animateCamera(czoom);
        moveMarker(new LatLng(lat, lng));
    }

    private void centerMap(double lat, double lng) {
        centerMap(lat, lng, 13);
    }

    private void centerMap(LatLng latLng) {
        centerMap(latLng.latitude, latLng.longitude, 13);
    }

    private void moveMarker(LatLng latlng) {
        if (windMarker != null) {
            windMarker.remove();
        }

        windMarker = windMap.addMarker(new MarkerOptions().position(latlng).draggable(true));
        AppPreferences preferences = AppPreferences.on(getActivity().getApplicationContext());
        preferences.setWindPosition(latlng);

        btn_position_set.setEnabled(true);
    }

    private void showBigMap() {
        ll_topContainer.setVisibility(View.GONE);
        rl_gpsOverlay.setVisibility(View.GONE);
        et_location.setVisibility(View.VISIBLE);
        et_location.setAlpha(1f);
        sendButton.setVisibility(View.GONE);
        btn_position_set.setVisibility(View.VISIBLE);
        windMap.getUiSettings().setAllGesturesEnabled(true);
        if (windMarker != null) {
            windMarker.setDraggable(true);
        }
    }

    private void hideView(final View v, int animationDuration) {
        v.animate().alpha(0f).setDuration(animationDuration).setListener(new AnimatorListener() {
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

    private void showView(final View v, int animationDuration) {
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1f).setDuration(animationDuration).setListener(null);
    }

    private void addAccuracyCircle(Location location) {
        if (windCircle != null) {
            windCircle.remove();
        }
        CircleOptions co = new CircleOptions().center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(location.getAccuracy()).fillColor(Color.parseColor("#33ff0000")).strokeWidth(1)
                .strokeColor(Color.RED);
        windCircle = windMap.addCircle(co);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        centerMap(location.getLatitude(), location.getLongitude());
        windMap.getUiSettings().setAllGesturesEnabled(false);
        addAccuracyCircle(location);
        txt_waitingForGPS.setTextColor(Color.GRAY);
        txt_waitingForGPS.setText(R.string.found_gps_position);
        rl_gpsOverlay.setVisibility(View.GONE);
        sendButton.setEnabled(true);
        if (windMarker != null) {
            windMarker.setDraggable(false);
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

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            new GeoCodeTask().execute(v.getText() + "");
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
        AppPreferences preferences = AppPreferences.on(getActivity().getApplicationContext());
        preferences.setWindPosition(marker.getPosition());

    }

    @Override
    public void onMarkerDrag(Marker arg0) {
    }

    @Override
    public void onMapClick(LatLng arg0) {
        if (bigMap)
            moveMarker(arg0);
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
        if (currentLocation == null || windSpeedEditText.getText().toString().isEmpty()
                || windBearingEditText.getText().toString().isEmpty()) {
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

    // HELPERS
    protected static float round(float unrounded, int precision) {
        BigDecimal decimal = new BigDecimal(unrounded);
        BigDecimal round = decimal.setScale(precision, BigDecimal.ROUND_UP);
        return round.floatValue();
    }

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

    protected void saveEntriesInPreferences(Wind wind) {
        // Wind.getBearing() returns a value that assumes that the wind flows in
        // that direction
        // But for this app we need to display the direction the wind is coming
        // from
        AppPreferences preferences = AppPreferences.on(getActivity().getApplicationContext());
        preferences.setWindBearingFromDirection(wind.getBearing().reverse().getDegrees());
        preferences.setWindSpeed(wind.getKnots());
    }
}

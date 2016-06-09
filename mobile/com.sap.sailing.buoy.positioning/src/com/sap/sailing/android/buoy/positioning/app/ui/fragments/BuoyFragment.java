package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.ui.activities.PositioningActivity;
import com.sap.sailing.android.buoy.positioning.app.util.PingHelper;
import com.sap.sailing.android.buoy.positioning.app.util.PingServerReplyCallback;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.ui.customviews.SignalQualityIndicatorView;
import com.sap.sailing.android.shared.util.LocationHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.android.ui.fragments.BaseFragment;

public class BuoyFragment extends BaseFragment implements LocationListener {
    private static final String TAG = BuoyFragment.class.getName();
    private static final int GPS_MIN_DISTANCE = 1;
    private static final int GPS_MIN_TIME = 1000;
    private static final String N_A = "n/a";
    private TextView accuracyTextView;
    private TextView distanceTextView;
    private Button setPositionButton;
    private MapFragment mapFragment;
    private Location lastKnownLocation;
    private LatLng savedPosition;
    private LocationManager locationManager;
    private pingListener pingListener;
    private SignalQualityIndicatorView signalQualityIndicatorView;
    private boolean initialLocationUpdate;
    private IntentReceiver mReceiver;
    private PositioningActivity positioningActivity;
    private LocalBroadcastManager mBroadcastManager;
    private GPSListener mGpsListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_buoy_postion_detail, container, false);

        accuracyTextView = ViewHelper.get(layout, R.id.marker_gps_accuracy);
        distanceTextView = ViewHelper.get(layout, R.id.marker_gps_distance);
        ClickListener clickListener = new ClickListener();

        setUpSetPositionButton(layout, clickListener);

        signalQualityIndicatorView = ViewHelper.get(layout, R.id.signal_quality_indicator);
        signalQualityIndicatorView.setSignalQuality(GPSQuality.noSignal.toInt());

        mReceiver = new IntentReceiver();
        mGpsListener = new GPSListener();
        mBroadcastManager = LocalBroadcastManager.getInstance(inflater.getContext());
        initMapFragment();

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        disablePositionButton();
        positioningActivity = (PositioningActivity) getActivity();
        mapFragment.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        initialLocationUpdate = true;
        initLocationProvider();
        MarkInfo mark = positioningActivity.getMarkInfo();
        signalQualityIndicatorView.setSignalQuality(GPSQuality.noSignal.toInt());
        if (mark != null) {
            setUpTextUI(lastKnownLocation);
            GoogleMap map = mapFragment.getMap();
            configureMap(map);
            updateMap();
            if (savedPosition != null) {
                initialLocationUpdate = false;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(savedPosition, 15));
            }
        }
        initMarkerReceiver();
        checkGPS();
    }

    private void initMapFragment() {
        mapFragment = new MapFragment();
        FragmentManager manager = getActivity().getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.map, mapFragment);
        transaction.commit();
    }

    private void setUpSetPositionButton(View layout, ClickListener clickListener) {
        setPositionButton = ViewHelper.get(layout, R.id.marker_set_position_button);
        disablePositionButton();
        setPositionButton.setOnClickListener(clickListener);
    }

    private void disablePositionButton() {
        setPositionButton.setEnabled(false);
        boolean gpsEnabled = LocationHelper.isGPSEnabled(getActivity());
        int buttonTextId = gpsEnabled ? R.string.set_position_no_gps_yet : R.string.set_position_disabled_gps;
        String text = getString(buttonTextId);
        SpannableString disabledButtonText = new SpannableString(text);
        AbsoluteSizeSpan absoluteSizeSpan = new AbsoluteSizeSpan(10, true);
        disabledButtonText.setSpan(absoluteSizeSpan, text.indexOf("\n"), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setPositionButton.setText(disabledButtonText);
    }

    private void checkGPS() {
        if (lastKnownLocation == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.error_message_no_position)
                .setPositiveButton(android.R.string.ok, null);
            if (!LocationHelper.isGPSEnabled(getActivity())) {
                builder.setNegativeButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LocationHelper.openLocationSettings(getActivity());
                    }
                });
            }
            builder.show();
        }
    }

    private void initMarkerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.database_changed));
        filter.addAction(getString(R.string.ping_reached_server));
        mBroadcastManager.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unsubscribe from location updates for power saving
        locationManager.removeUpdates(this);
        locationManager.removeGpsStatusListener(mGpsListener);
        mBroadcastManager.unregisterReceiver(mReceiver);
    }

    public void setUpTextUI(Location location) {
        String accuracyText = N_A;
        String distanceText = N_A;
        DecimalFormat accuracyFormatter = new DecimalFormat("#.##");
        String accuracyString = getString(R.string.buoy_detail_accuracy_ca);
        String distanceString = getString(R.string.buoy_detail_distance);
        if (location != null) {
            accuracyText = String.format(accuracyString, accuracyFormatter.format(location.getAccuracy()));
            MarkPingInfo markPing = positioningActivity.getMarkPing();
            if (markPing != null) {
                double savedLatitude = Double.parseDouble(markPing.getLatitude());
                double savedLongitude = Double.parseDouble(markPing.getLongitude());
                savedPosition = new LatLng(savedLatitude, savedLongitude);
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), savedLatitude, savedLongitude,
                        results);
                float distance = results[0];
                distanceText = String.format(distanceString, accuracyFormatter.format(distance));
            }
        }

        ExLog.w(getActivity(), getTag(), "Setting accuracy to: " + accuracyText);
        ExLog.w(getActivity(), getTag(), "Setting distance to: " + distanceText);

        accuracyTextView.setText(accuracyText);
        distanceTextView.setText(distanceText);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getActivity() instanceof PositioningActivity) {
            mapFragment.getMap().setMyLocationEnabled(true);
            lastKnownLocation = location;
            reportGPSQuality(lastKnownLocation.getAccuracy());
            setPositionButton.setEnabled(true);
            setPositionButton.setAllCaps(true);
            setPositionButton.setText(R.string.set_position);
            setUpTextUI(lastKnownLocation);
            updateMap();
        }

        if (initialLocationUpdate && lastKnownLocation != null) {
            LatLng lastKnownLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            GoogleMap map = mapFragment.getMap();
            configureMap(map);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 15));
            initialLocationUpdate = false;
        }

    }

    private void configureMap(GoogleMap map) {
        if (map != null) {
            map.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (getActivity() instanceof PositioningActivity) {
            initLocationProvider();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (getActivity() instanceof PositioningActivity) {
            initLocationProvider();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (getActivity() instanceof PositioningActivity) {
            initLocationProvider();
        }
    }

    private void initLocationProvider() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
        locationManager.removeGpsStatusListener(mGpsListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, this);
        locationManager.addGpsStatusListener(mGpsListener);
        
        Location initialLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if (initialLocation != null) {
            onLocationChanged(initialLocation);
        }
    }

    public void updateMap() {
        GoogleMap map = mapFragment.getMap();
        map.clear();

        if (savedPosition != null) {
            MarkerOptions savedLocactionOptions = new MarkerOptions();
            savedLocactionOptions.position(savedPosition);
            savedLocactionOptions.visible(true);
            map.addMarker(savedLocactionOptions);
        }

    }

    public void reportGPSQuality(float gpsAccuracy) {
        GPSQuality quality = GPSQuality.noSignal;

        if (gpsAccuracy > 48) {
            quality = GPSQuality.poor;
        } else if (gpsAccuracy > 10) {
            quality = GPSQuality.good;
        } else if (gpsAccuracy <= 10) {
            quality = GPSQuality.great;
        }
        signalQualityIndicatorView.setSignalQuality(quality.toInt());

    }

    public void handleSuccessfulResponse(){
        Toast.makeText(getActivity(), getString(R.string.position_set), Toast.LENGTH_SHORT).show();
    }

    public void setPingListener(pingListener listener) {
        pingListener = listener;
    }

    public enum GPSQuality {
        noSignal(0), poor(2), good(3), great(4);

        private final int gpsQuality;

        GPSQuality(int quality) {
            this.gpsQuality = quality;
        }

        public int toInt() {
            return this.gpsQuality;
        }
    }

    public interface pingListener {
        void updatePing();
    }

    private class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.marker_set_position_button) {
                PingHelper helper = new PingHelper();
                if (lastKnownLocation != null) {
                    MarkInfo mark = positioningActivity.getMarkInfo();
                    LeaderboardInfo leaderBoard = positioningActivity.getLeaderBoard();
                    helper.storePingInDatabase(getActivity(), lastKnownLocation, mark);
                    helper.sendPingToServer(getActivity(), lastKnownLocation, leaderBoard, mark, PingServerReplyCallback.class);
                    ((PositioningActivity) getActivity()).updatePing();
                    savedPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    pingListener.updatePing();
                    setUpTextUI(lastKnownLocation);
                    updateMap();
                } else {
                    Toast.makeText(getActivity(), "Location is not available yet", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // Broadcast receiver to update ui on data changed
    private class IntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action: " + action);
            if (action.equals(getString(R.string.database_changed)) && isAdded()) {
                positioningActivity.loadDataFromDatabase();
                setUpTextUI(lastKnownLocation);
                updateMap();
            }
            if (action.equals(getString(R.string.ping_reached_server))){
                Log.d(TAG, "Response reached Buoy Fragment");
                handleSuccessfulResponse();
            }
        }
    }

    private class GPSListener implements GpsStatus.Listener {

        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STOPPED: {
                    if (isAdded()) {
                        disablePositionButton();
                        distanceTextView.setText(N_A);
                        accuracyTextView.setText(N_A);
                        mapFragment.getMap().setMyLocationEnabled(false);
                        reportGPSQuality(0);
                        break;
                    }
                }
            }
        }
    }
}

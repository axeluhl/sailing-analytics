package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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

public class BuoyFragment extends BaseFragment
    implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BuoyFragment.class.getName();

    private static final int GPS_MIN_TIME = 1000;
    private static final String N_A = "n/a";
    private static final int REQUEST_CHECK_SETTINGS = 1 << 16;

    private TextView accuracyTextView;
    private TextView distanceTextView;
    private Button setPositionButton;
    private MapFragment mapFragment;
    private Location lastKnownLocation;
    private LatLng savedPosition;
    private pingListener pingListener;
    private SignalQualityIndicatorView signalQualityIndicatorView;
    private boolean initialLocationUpdate;
    private IntentReceiver mReceiver;
    private PositioningActivity positioningActivity;
    private LocalBroadcastManager mBroadcastManager;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mSettingsRequest;

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
        mBroadcastManager = LocalBroadcastManager.getInstance(inflater.getContext());
        initMapFragment();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(GPS_MIN_TIME);
        mLocationRequest.setFastestInterval(GPS_MIN_TIME);

        mSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest).build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build();

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        disablePositionButton();
        positioningActivity = (PositioningActivity) getActivity();
        mapFragment.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        initialLocationUpdate = true;
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
        int resId = LocationHelper.isGPSEnabled(getActivity()) ? R.string.set_position_no_gps_yet : R.string.set_position_disabled_gps;
        setPositionButton.setText(resId);
    }

    private void checkGPS() {
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, mSettingsRequest);
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult settingsResult) {
                final Status status = settingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        startLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int code = (requestCode + 1) << 16;
        switch (code) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        mGoogleApiClient.connect();
                        break;

                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;

                    default:
                        break;
                }
                break;
        }
    }

    private void initMarkerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.database_changed));
        filter.addAction(getString(R.string.ping_reached_server));
        mBroadcastManager.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        mGoogleApiClient.disconnect();

        mBroadcastManager.unregisterReceiver(mReceiver);
    }

    private void setUpTextUI(Location location) {
        String accuracyText = N_A;
        String distanceText = N_A;
        if (location != null) {
            accuracyText = getString(R.string.buoy_detail_accuracy_ca, location.getAccuracy());
            MarkPingInfo markPing = positioningActivity.getMarkPing();
            if (markPing != null) {
                double savedLatitude = Double.parseDouble(markPing.getLatitude());
                double savedLongitude = Double.parseDouble(markPing.getLongitude());
                savedPosition = new LatLng(savedLatitude, savedLongitude);
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), savedLatitude, savedLongitude, results);
                float distance = results[0];
                distanceText = getString(R.string.buoy_detail_distance, distance);
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

    private void updateMap() {
        GoogleMap map = mapFragment.getMap();
        map.clear();

        if (savedPosition != null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(savedPosition);
            markerOptions.visible(true);
            map.addMarker(markerOptions);
        }
    }

    private void reportGPSQuality(float gpsAccuracy) {
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

    private void handleSuccessfulResponse() {
        Toast.makeText(getActivity(), getString(R.string.position_set), Toast.LENGTH_SHORT).show();
    }

    public void setPingListener(pingListener listener) {
        pingListener = listener;
    }

    @Override
    public void onConnected(Bundle bundle) {
        checkGPS();
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // no-op
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        ExLog.e(getActivity(), TAG, "Failed to connect to Google Play Services for location updates");
    }

    private enum GPSQuality {
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
            if (action.equals(getString(R.string.ping_reached_server))) {
                Log.d(TAG, "Response reached Buoy Fragment");
                handleSuccessfulResponse();
            }
        }
    }
}

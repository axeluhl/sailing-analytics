package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
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
import com.sap.sailing.android.shared.ui.customviews.GPSQuality;
import com.sap.sailing.android.shared.ui.customviews.SignalQualityIndicatorView;
import com.sap.sailing.android.shared.util.LocationHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.android.ui.fragments.BaseFragment;

public class BuoyFragment extends BaseFragment
    implements android.location.LocationListener {
    
    private static final String TAG = BuoyFragment.class.getName();

    private static final int GPS_MIN_TIME = 1000;
    private float minLocationUpdateDistanceInMeters = 0f;

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

    protected LocationManager locationManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_buoy_postion_detail, container, false);

        accuracyTextView = ViewHelper.get(layout, R.id.marker_gps_accuracy);
        distanceTextView = ViewHelper.get(layout, R.id.marker_gps_distance);
        ClickListener clickListener = new ClickListener();

        setUpSetPositionButton(layout, clickListener);

        signalQualityIndicatorView = ViewHelper.get(layout, R.id.signal_quality_indicator);
        signalQualityIndicatorView.setSignalQuality(GPSQuality.noSignal);

        mReceiver = new IntentReceiver();
        mBroadcastManager = LocalBroadcastManager.getInstance(inflater.getContext());
        initMapFragment();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            /**
             * prior to JellyBean, the minimum time for location updates parameter MIGHT be ignored,
             * so providing a minimum distance value greater than 0 is recommended
             */
            minLocationUpdateDistanceInMeters = .5f;
        }
        
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
        signalQualityIndicatorView.setSignalQuality(GPSQuality.noSignal);
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

    private void initMarkerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.database_changed));
        filter.addAction(getString(R.string.ping_reached_server));
        mBroadcastManager.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, minLocationUpdateDistanceInMeters , this);
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        
        mBroadcastManager.unregisterReceiver(mReceiver);
    }

    private void setUpTextUI(Location location) {
        String accuracyText = this.getString(R.string.unknown);
        String distanceText = this.getString(R.string.unknown);
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

    
    /**
     * Methods implemented through LocationManager
     */ 
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
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      //no-op
    }

    @Override
    public void onProviderDisabled(String provider) {
        //provider (GPS) disabled by the user while tracking
        disablePositionButton();
        setUpTextUI(null);
        signalQualityIndicatorView.setSignalQuality(GPSQuality.noSignal);
        
        LocationHelper.showNoGPSError(getActivity(), getString(R.string.enable_gps));
    }

    @Override
    public void onProviderEnabled(String provider) {
        //provider (GPS) (re)enabled by the user while tracking
        //call disablePositionButton() so that its text says "searching for GPS" again
        disablePositionButton();
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
        signalQualityIndicatorView.setSignalQuality(quality);
    }

    private void handleSuccessfulResponse() {
        Toast.makeText(getActivity(), getString(R.string.position_set), Toast.LENGTH_SHORT).show();
    }

    public void setPingListener(pingListener listener) {
        pingListener = listener;
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

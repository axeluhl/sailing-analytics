package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.sap.sailing.android.shared.ui.customviews.OpenSansButton;
import com.sap.sailing.android.shared.ui.customviews.OpenSansTextView;
import com.sap.sailing.android.shared.ui.customviews.SignalQualityIndicatorView;
import com.sap.sailing.android.shared.util.LocationHelper;
import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.android.ui.fragments.BaseFragment;

public class BuoyFragment extends BaseFragment implements LocationListener {
    private static final String TAG = BuoyFragment.class.getName();
    private static final int GPS_MIN_DISTANCE = 1;
    private static final int GPS_MIN_TIME = 1000;
    private OpenSansTextView markHeaderTextView;
    private OpenSansTextView latitudeTextView;
    private OpenSansTextView longitudeTextView;
    private OpenSansTextView accuracyTextView;
    private OpenSansButton setPositionButton;
    private OpenSansButton resetPositionButton;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_buoy_postion_detail, container, false);

        markHeaderTextView = ViewHelper.get(layout, R.id.mark_header);
        latitudeTextView = ViewHelper.get(layout, R.id.marker_gps_latitude);
        longitudeTextView = ViewHelper.get(layout, R.id.marker_gps_longitude);
        accuracyTextView = ViewHelper.get(layout, R.id.marker_gps_accuracy);
        ClickListener clickListener = new ClickListener();

        setPositionButton = ViewHelper.get(layout, R.id.marker_set_position_button);
        setPositionButton.setVisibility(View.GONE);
        setPositionButton.setOnClickListener(clickListener);

        resetPositionButton = ViewHelper.get(layout, R.id.marker_reset_position_button);
        resetPositionButton.setOnClickListener(clickListener);
        resetPositionButton.setVisibility(View.GONE);

        signalQualityIndicatorView = ViewHelper.get(layout, R.id.signal_quality_indicator);
        signalQualityIndicatorView.setSignalQuality(GPSQuality.noSignal.toInt());

        mReceiver = new IntentReceiver();
        mBroadcastManager = LocalBroadcastManager.getInstance(inflater.getContext());
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        positioningActivity = (PositioningActivity) getActivity();
        mapFragment = (MapFragment) positioningActivity.getFragmentManager().findFragmentById(R.id.map);
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
        // Unsubscribe location updates for power saving
        locationManager.removeUpdates(this);
        mBroadcastManager.unregisterReceiver(mReceiver);
    }

    public void setUpTextUI(Location location) {
        MarkInfo mark = positioningActivity.getMarkInfo();
        markHeaderTextView.setText(mark.getName());
        String longitudeText = "";
        String latitudeText = "";
        String accuracyText = "";
        DecimalFormat latlngFormatter = new DecimalFormat("#.######");
        DecimalFormat accuracyFormatter = new DecimalFormat("#.##");
        String accuracyString = getString(R.string.buoy_detail_accuracy_ca);
        if (location != null) {
            latitudeText += latlngFormatter.format(location.getLatitude());
            longitudeText += latlngFormatter.format(location.getLongitude());
            accuracyText += String.format(accuracyString, accuracyFormatter.format(location.getAccuracy()));
        } else {
            latitudeText += "n/a";
            longitudeText += "n/a";
            accuracyText += "n/a";
        }
        MarkPingInfo markPing = positioningActivity.getMarkPing();
        if (markPing != null) {
            double savedLatitude = Double.parseDouble(markPing.getLatitude());
            double savedLongitude = Double.parseDouble(markPing.getLongitude());
            savedPosition = new LatLng(savedLatitude, savedLongitude);
            latitudeText += " (" + latlngFormatter.format(savedLatitude) + ")";
            longitudeText += " (" + latlngFormatter.format(savedLongitude) + ")";
            accuracyText += " (" + String.format(accuracyString, accuracyFormatter.format(markPing.getAccuracy()))
                    + ")";
        }
        
          ExLog.w(getActivity(), getTag(), "Setting latitude to: "+latitudeText);
          ExLog.w(getActivity(), getTag(), "Setting longitude to: "+longitudeText);
          ExLog.w(getActivity(), getTag(), "Setting accuracy to: "+accuracyText);
        
        latitudeTextView.setText(latitudeText);
        longitudeTextView.setText(longitudeText);
        accuracyTextView.setText(accuracyText);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getActivity() instanceof PositioningActivity) {
            lastKnownLocation = location;
            reportGPSQuality(lastKnownLocation.getAccuracy());
            setPositionButton.setVisibility(View.VISIBLE);
            setUpTextUI(lastKnownLocation);
            updateMap();
        }

        if (initialLocationUpdate) {
            LatLng lastKnownLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            GoogleMap map = mapFragment.getMap();
            configureMap(map);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 15));
            initialLocationUpdate = false;
        }

    }

    private void configureMap(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setPadding(0, 50, 0, 0);
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_TIME, GPS_MIN_DISTANCE, this);
        
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
        public void updatePing();
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
            } else if (id == R.id.marker_reset_position_button) {
                // Reset position
            }
        }
    }

    // Broadcast receiver to update ui on data changed
    private class IntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Action: " + action);
            if(action.equals(getString(R.string.database_changed))) {
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
}

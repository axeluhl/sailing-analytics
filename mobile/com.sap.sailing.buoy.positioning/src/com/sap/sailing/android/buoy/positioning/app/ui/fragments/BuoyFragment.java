package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.sap.sailing.android.buoy.positioning.app.util.DatabaseHelper.GeneralDatabaseHelperException;
import com.sap.sailing.android.buoy.positioning.app.util.PingHelper;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkInfo;
import com.sap.sailing.android.buoy.positioning.app.valueobjects.MarkPingInfo;
import com.sap.sailing.android.shared.data.LeaderboardInfo;
import com.sap.sailing.android.shared.ui.customviews.OpenSansButton;
import com.sap.sailing.android.shared.ui.customviews.OpenSansTextView;
import com.sap.sailing.android.shared.ui.customviews.SignalQualityIndicatorView;
import com.sap.sailing.android.ui.fragments.BaseFragment;

import java.text.DecimalFormat;

public class BuoyFragment extends BaseFragment implements LocationListener {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_buoy_postion_detail,
                container, false);

        markHeaderTextView = (OpenSansTextView) view
                .findViewById(R.id.mark_header);
        latitudeTextView = (OpenSansTextView) view
                .findViewById(R.id.marker_gps_latitude);
        longitudeTextView = (OpenSansTextView) view
                .findViewById(R.id.marker_gps_longitude);
        accuracyTextView = (OpenSansTextView) view
                .findViewById(R.id.marker_gps_accuracy);
        ClickListener clickListener = new ClickListener();

        setPositionButton = (OpenSansButton) view
                .findViewById(R.id.marker_set_position_button);
        setPositionButton.setVisibility(View.GONE);
        setPositionButton.setOnClickListener(clickListener);

        resetPositionButton = (OpenSansButton) view
                .findViewById(R.id.marker_reset_position_button);
        resetPositionButton.setOnClickListener(clickListener);
        resetPositionButton.setVisibility(View.GONE);

        signalQualityIndicatorView = (SignalQualityIndicatorView) view.findViewById(R.id.signal_quality_indicator);
        signalQualityIndicatorView.setSignalQuality(GPSQuality.noSignal.toInt());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initialLocationUpdate = true;
        initLocationProvider();
        MarkInfo mark = ((PositioningActivity) getActivity()).getMarkInfo();
        signalQualityIndicatorView.setSignalQuality(GPSQuality.noSignal.toInt());
        if (mark != null) {
            markHeaderTextView.setText(mark.getName());
            setUpTextUI(null);
            setUpMap();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unsubscribe location updates for power saving
        locationManager.removeUpdates(this);
    }

    public void setUpTextUI(Location location) {
        String longitudeText = "";
        String latitudeText = "";
        String accuracyText = "";
        DecimalFormat formatter = new DecimalFormat("#.######");
        if (location != null) {
            latitudeText += formatter.format(location.getLatitude());
            longitudeText += formatter.format(location.getLongitude());
            accuracyText += "~" + location.getAccuracy();
        } else {
            latitudeText += "n/a";
            longitudeText += "n/a";
            accuracyText += "n/a";
        }
        MarkPingInfo markPing = ((PositioningActivity) getActivity())
                .getMarkPing();
        if (markPing != null) {
            double savedLatitude = Double.parseDouble(markPing.getLatitude());
            double savedLongitude = Double.parseDouble(markPing.getLongitude());
            savedPosition = new LatLng(savedLatitude, savedLongitude);
            latitudeText += " (" + formatter.format(savedLatitude) + ")";
            longitudeText += " (" + formatter.format(savedLongitude) + ")";
            accuracyText += " (" + "~" + markPing.getAccuracy() + ")";
        }
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
            setUpMap();
        }

        if(initialLocationUpdate){
            LatLng lastKnownLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng, 15));
            mapFragment.getMap().setMyLocationEnabled(true);
            initialLocationUpdate = false;
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
        locationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
    }

    public void setUpMap() {
        mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        GoogleMap map = mapFragment.getMap();
        map.clear();
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setPadding(0, 50, 0, 0);

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
                try {
                    if (lastKnownLocation != null) {
                        MarkInfo mark = ((PositioningActivity) getActivity())
                                .getMarkInfo();
                        LeaderboardInfo leaderBoard = ((PositioningActivity) getActivity())
                                .getLeaderBoard();
                        helper.storePingInDatabase(getActivity(),
                                lastKnownLocation, mark);
                        helper.sendPingToServer(getActivity(),
                                lastKnownLocation, leaderBoard, mark);
                        ((PositioningActivity) getActivity()).updatePing();
                        savedPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        pingListener.updatePing();
                        setUpTextUI(lastKnownLocation);
                        setUpMap();
                    } else {
                        Toast.makeText(getActivity(), "Location is not available yet", Toast.LENGTH_LONG).show();
                    }
                } catch (GeneralDatabaseHelperException e) {
                    e.printStackTrace();
                }
            } else if (id == R.id.marker_reset_position_button) {
                // Reset position
            }

        }

    }

}

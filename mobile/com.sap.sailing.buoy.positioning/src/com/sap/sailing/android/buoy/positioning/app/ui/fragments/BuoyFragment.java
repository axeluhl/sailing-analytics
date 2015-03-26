package com.sap.sailing.android.buoy.positioning.app.ui.fragments;

import android.content.Context;
import android.location.Criteria;
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
import com.google.android.gms.maps.MapView;
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
import com.sap.sailing.android.ui.fragments.BaseFragment;

public class BuoyFragment extends BaseFragment implements LocationListener {
    private OpenSansTextView markHeaderTextView;
    private OpenSansTextView latitudeTextView;
    private OpenSansTextView longitudeTextView;
    private OpenSansTextView accuracyTextView;
    private OpenSansButton setPositionButton;
    private OpenSansButton resetPositionButton;
    private MapView mapView;
    private Location lastKnownLocation;
    private LatLng savedPosition;
    private LocationManager locationManager;
    private String locationProvider;
    private pingListener pingListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_buoy_postion_detail,
                container, false);

        mapView = (MapView) view.findViewById(R.id.map);

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
        setPositionButton.setOnClickListener(clickListener);

        resetPositionButton = (OpenSansButton) view
                .findViewById(R.id.marker_reset_position_button);
        resetPositionButton.setOnClickListener(clickListener);
        resetPositionButton.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initLocationProvider();
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        MarkInfo mark = ((PositioningActivity) getActivity()).getMarkInfo();
        if (mark != null) {
            markHeaderTextView.setText(mark.getName());
            setUpPingUI();
            //setUpMap();
        }
        if(lastKnownLocation != null) {
            //mapView.getMap().animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
        }
    }

    public void setUpPingUI() {
        MarkPingInfo markPing = ((PositioningActivity) getActivity())
                .getMarkPing();
        if (markPing != null) {
            double savedLatitude = Double.parseDouble(markPing.getLattitude());
            double savedLongitude = Double.parseDouble(markPing.getLongitude());
            savedPosition = new LatLng(savedLatitude, savedLongitude);
            latitudeTextView.setText(markPing.getLattitude());
            longitudeTextView.setText(markPing.getLongitude());
            accuracyTextView.setText("~" + markPing.getAccuracy());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getActivity() instanceof PositioningActivity) {
            lastKnownLocation = location;
            //setUpMap();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if(getActivity() instanceof PositioningActivity) {
            initLocationProvider();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if(getActivity() instanceof PositioningActivity) {
            initLocationProvider();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if(getActivity() instanceof PositioningActivity) {
            initLocationProvider();
        }
    }

    private void initLocationProvider() {
        locationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,500.0f, this);
    }

    public void setUpMap() {
        GoogleMap map = mapView.getMap();
        map.clear();
        if (savedPosition != null) {
            MarkerOptions savedLocactionOptions = new MarkerOptions();
            savedLocactionOptions.position(savedPosition);
            // mySelfOptions.visible(true);
            map.addMarker(savedLocactionOptions);
        }
        if(lastKnownLocation != null)
        {
            MarkerOptions mySelfOptions = new MarkerOptions();
            LatLng lastKnownLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            mySelfOptions.position(lastKnownLatLng);
            // mySelfOptions.visible(true);
            map.addMarker(mySelfOptions);
        }

    }

    public void setPingListener(pingListener listener) {
        pingListener = listener;
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
                        savedPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        pingListener.updatePing();
                        setUpPingUI();
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

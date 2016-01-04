package com.sap.sailing.racecommittee.app.ui.fragments.maps;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.MapMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class extending MapFragment, including some functions for animation.
 *
 * @author frank
 */
public class WindMap extends MapFragment {

    private final static float DEFAULT_ZOOM = 15;
    public Marker windMarker;
    public Circle windCircle;

    private Context mContext;
    private OnResumeCallback mListener;
    private GoogleMap mMap;
    private List<MapMarker> mapItems;

    public static WindMap newInstance(Context context, OnResumeCallback listener) {
        WindMap map = new WindMap();
        map.mContext = context;
        map.mListener = listener;
        return map;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mListener != null) {
            mListener.afterResumed();
        }
    }

    /**
     * adds a cirlce below the map pin displaying the uncertainty radius of the GPS location
     *
     * @param location Location: given by the gps service
     */
    public void addAccuracyCircle(Location location) {
        if (mMap != null) {
            if (windCircle != null) {
                windCircle.remove();
            }
            CircleOptions co = new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(location.getAccuracy())
                .fillColor(Color.parseColor("#33ff0000"))
                .strokeWidth(1)
                .strokeColor(Color.RED);
            windCircle = mMap.addCircle(co);
        }
    }

    /**
     * animates the map pin to move to a different position
     *
     * @param marker     Marker: the marker to move
     * @param toPosition LatLng: the position where it should go
     * @param hideMarker boolean: if the marker should be hidden
     */
    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    /**
     * centers the map around the given coordinates
     *
     * @param lat  double: latitude
     * @param lng  double: longitude
     * @param zoom float: zoom, usually a number between 1 and 14, the bigger the number the closer
     */
    public void centerMap(double lat, double lng, float zoom) {
        if (mMap != null) {
            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
            CameraUpdate czoom = CameraUpdateFactory.zoomTo(zoom);
            mMap.moveCamera(center);
            mMap.animateCamera(czoom);
            movePositionMarker(new LatLng(lat, lng));
        }
    }

    /**
     * centers the map around the given coordinates
     *
     * @param lat double: latitude
     * @param lng double: longitude
     */
    public void centerMap(double lat, double lng) {
        centerMap(lat, lng, DEFAULT_ZOOM);
    }

    /**
     * centers the map around the given coordinates
     *
     * @param latLng LatLng: coordinates to center around
     */
    public void centerMap(LatLng latLng) {
        centerMap(latLng.latitude, latLng.longitude, DEFAULT_ZOOM);
    }

    /**
     * moves the pin to a provided position
     *
     * @param latlng LatLng: position to move the pin to
     */
    public void movePositionMarker(LatLng latlng) {
        if (windMarker != null) {
            windMarker.remove();
        }

        windMarker = mMap.addMarker(new MarkerOptions().position(latlng).draggable(true));
        AppPreferences preferences = AppPreferences.on(mContext);
        preferences.setWindPosition(latlng);
    }

    /**
     * callback that updates the buoy markers
     *
     * @param markers List<MapMarkers> markers
     */
    public void onMapDataUpdated(List<MapMarker> markers) {
        ExLog.i(this.getActivity(), this.getTag(), "MapData is getting updated");
        // remove old markers
        if (mapItems != null && mapItems.size() > 0) {
            for (MapMarker item : mapItems) {
                if (item.getMarker() != null) {
                    item.getMarker().remove();
                }
            }
        }

        // add new markers
        mapItems = markers;
        for (MapMarker item : mapItems) {
            ExLog.i(this.getActivity(), this.getTag(), "adding " + item.getName());
            ExLog.i(this.getActivity(), this.getTag(), "tracksize " + item.getTrack().size());
            ArrayList<MapMarker.TrackMeasurement> track = item.getTrack();
            if (track != null && track.size() > 0) {
                LatLng pos = track.get(track.size() - 1).position;
                item.setMarker(mMap
                    .addMarker(new MarkerOptions().position(pos).title(item.getName()).draggable(false).icon(item.getMarkerIcon(mContext))));

                ExLog.i(this.getActivity(), this.getTag(),
                    "Showing marker for " + item.getName() + " at (" + pos.latitude + ", " + pos.longitude + ")");
            }
        }
    }

    public interface OnResumeCallback {
        void afterResumed();
    }
}

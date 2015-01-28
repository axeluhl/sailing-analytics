package com.sap.sailing.racecommittee.app.domain;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;

import java.util.ArrayList;
import java.util.UUID;

public interface MapMarker {
    public String getName();
    public ArrayList<TrackMeasurement> getTrack();
    public UUID getID();
    public MarkType getType();
    public Color getColor();
    public Marker getMarker();
    public void setMarker(Marker m);
    public BitmapDescriptor getMarkerIcon(Context context);

    public class TrackMeasurement {
        public double timepoint;
        public LatLng position;
    }
}

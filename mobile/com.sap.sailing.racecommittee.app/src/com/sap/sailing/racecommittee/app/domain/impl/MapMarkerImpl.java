package com.sap.sailing.racecommittee.app.domain.impl;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.MapMarker;
import com.sap.sailing.racecommittee.app.ui.utils.BuoyHelper;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sse.common.Color;

public class MapMarkerImpl implements MapMarker {

    private String name;
    private UUID id;
    private ArrayList<TrackMeasurement> track;
    private Marker marker;

    public MapMarkerImpl(String name, UUID id, ArrayList<TrackMeasurement> track) {
        this.name = name;
        this.id = id;
        this.track = track;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ArrayList<TrackMeasurement> getTrack() {
        return track;
    }

    @Override
    public MarkType getType() {
        return MarkType.BUOY;
    }

    @Override
    public UUID getID() {
        return id;
    }

    @Override
    public Color getColor(Context context) {
        String name = this.name.toLowerCase(Locale.US);
        if (name.startsWith("white")) {
            return Color.WHITE;
        } else if (name.startsWith("gray")) {
            return Color.GRAY;
        } else if (name.startsWith("black")) {
            return Color.BLACK;
        } else if (name.startsWith("yellow")) {
            return Color.YELLOW;
        } else if (this.name.startsWith("pink")) {
            return Color.PINK;
        } else if (this.name.startsWith("orange")) {
            return Color.ORANGE;
        } else if (this.name.startsWith("green")) {
            return Color.GREEN;
        } else if (this.name.startsWith("magenta")) {
            return Color.MAGENTA;
        } else if (this.name.startsWith("cyan")) {
            return Color.CYAN;
        } else {
            return Color.ORANGE;
        }
    }

    @Override
    public Marker getMarker() {
        return marker;
    }

    @Override
    public void setMarker(Marker m) {
        this.marker = m;
    }

    @Override
    public BitmapDescriptor getMarkerIcon(Context context) {
        String name = this.name.toLowerCase(Locale.US);
        // buoys
        if (name.startsWith("white")) {
            return BitmapDescriptorFactory.fromBitmap(BitmapHelper.toBitmap(BuoyHelper.getBuoy(context, MarkType.BUOY, "white", null, null)));
        } else if (name.startsWith("gray")) {
            return BitmapDescriptorFactory.fromBitmap(BitmapHelper.toBitmap(BuoyHelper.getBuoy(context, MarkType.BUOY, "grey", null, null)));
        } else if (name.startsWith("black cone")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_black_cone);
        } else if (name.startsWith("black cyl")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_black_cyl);
        } else if (name.startsWith("black")) {
            return BitmapDescriptorFactory.fromBitmap(BitmapHelper.toBitmap(BuoyHelper.getBuoy(context, MarkType.BUOY, "black", null, null)));
        } else if (name.startsWith("yellow")) {
            return BitmapDescriptorFactory.fromBitmap(BitmapHelper.toBitmap(BuoyHelper.getBuoy(context, MarkType.BUOY, "yellow", null, null)));
        } else if (name.startsWith("orange")) {
            return BitmapDescriptorFactory.fromBitmap(BitmapHelper.toBitmap(BuoyHelper.getBuoy(context, MarkType.BUOY, "orange", null, null)));
        } else if (name.startsWith("green")) {
            return BitmapDescriptorFactory.fromBitmap(BitmapHelper.toBitmap(BuoyHelper.getBuoy(context, MarkType.BUOY, "green", null, null)));
        } else if (name.startsWith("red")) {
            return BitmapDescriptorFactory.fromBitmap(BitmapHelper.toBitmap(BuoyHelper.getBuoy(context, MarkType.BUOY, "red", null, null)));
        } else if (name.startsWith("finish")) {
            return BitmapDescriptorFactory.fromResource(R.drawable.buoy_finish_flag);
        } else {
            return BitmapDescriptorFactory.fromBitmap(BitmapHelper.toBitmap(BuoyHelper.getBuoy(context, MarkType.BUOY, "undefined", null, null)));
        }
    }
}

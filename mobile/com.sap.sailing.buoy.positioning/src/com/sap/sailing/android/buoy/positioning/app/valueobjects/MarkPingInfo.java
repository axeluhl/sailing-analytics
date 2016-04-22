package com.sap.sailing.android.buoy.positioning.app.valueobjects;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.GPSFix;

public class MarkPingInfo {
    private final Serializable markId;
    private final GPSFix gpsFix;
    private final double accuracy;

    public MarkPingInfo(Serializable markId, GPSFix gpsFix, double accuracy) {
        super();
        this.markId = markId;
        this.gpsFix = gpsFix;
        this.accuracy = accuracy;
    }

    public Serializable getMarkId() {
        return markId;
    }

    public String getLongitude() {
        return "" + gpsFix.getPosition().getLngDeg();
    }

    public String getLatitude() {
        return "" + gpsFix.getPosition().getLatDeg();
    }

    public double getAccuracy() {
        return accuracy;
    }

    public long getTimestamp() {
        return gpsFix.getTimePoint().asMillis();
    }
}

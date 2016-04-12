package com.sap.sailing.android.buoy.positioning.app.valueobjects;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.GPSFix;

public class MarkPingInfo {
    private Serializable markId;
    private GPSFix gpsFix;
    private double accuracy;

    public Serializable getMarkId() {
        return markId;
    }

    public void setGpsFix(GPSFix fix) {
        gpsFix = fix;
    }

    public void setMarkId(Serializable markId) {
        this.markId = markId;
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

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public long getTimestamp() {
        return gpsFix.getTimePoint().asMillis();
    }
}

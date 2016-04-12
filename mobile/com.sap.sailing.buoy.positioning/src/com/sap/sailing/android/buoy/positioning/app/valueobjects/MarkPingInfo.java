package com.sap.sailing.android.buoy.positioning.app.valueobjects;

import java.io.Serializable;

public class MarkPingInfo {
    private Serializable markId;
    private String longitude;
    private String latitude;
    private double accuracy;
    private int timestamp;

    public Serializable getMarkId() {
        return markId;
    }

    public void setMarkId(Serializable markId) {
        this.markId = markId;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}

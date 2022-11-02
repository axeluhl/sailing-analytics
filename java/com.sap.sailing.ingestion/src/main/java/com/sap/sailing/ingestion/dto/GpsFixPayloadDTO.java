package com.sap.sailing.ingestion.dto;

import java.io.Serializable;

public class GpsFixPayloadDTO implements Serializable, Comparable<GpsFixPayloadDTO> {
    private static final long serialVersionUID = 6802355060150334552L;

    private long timestamp;
    private double latitude;
    private double longitude;
    private double speed;
    private double course;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getCourse() {
        return course;
    }

    public void setCourse(double course) {
        this.course = course;
    }
    
    @Override
    public int compareTo(GpsFixPayloadDTO o) {
        return (int) (this.getTimestamp() - o.getTimestamp());
    }
}

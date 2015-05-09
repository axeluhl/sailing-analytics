package com.sap.sailing.android.tracking.app.valueobjects;

public class GpsFix {
    public int id;
    public long timestamp;
    public double latitude;
    public double longitude;
    public double speed;
    public double course;
    public int synced;
    public String host;
    public String eventId;

    @Override
    public String toString() {
        return "ID: " + id + ", T: " + timestamp + ", LAT: " + latitude + ", LON: " + longitude + ", SPD: " + speed
                + ", CRS: " + course + ", SYN: " + synced + ", HOST: " + host + ", EVENT-ID: " + eventId;
    }
}
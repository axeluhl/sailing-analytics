package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingMessage;

public class SwissTimingMessageImpl implements SwissTimingMessage {
    private final String raceID;
    private final int packetID;
    private final TimePoint timestamp;
    private final int gpsID;
    private final Position position;
    private final Speed speed;
    private final int numberOfSatellites;
    private final int batteryPercent;
    
    public SwissTimingMessageImpl(String raceID, int packetID, TimePoint timestamp, int gpsID, Position position, Speed speed,
            int numberOfSatellites, int batteryPercent) {
        super();
        this.raceID = raceID;
        this.packetID = packetID;
        this.timestamp = timestamp;
        this.gpsID = gpsID;
        this.position = position;
        this.speed = speed;
        this.numberOfSatellites = numberOfSatellites;
        this.batteryPercent = batteryPercent;
    }

    public String getRaceID() {
        return raceID;
    }

    public int getPacketID() {
        return packetID;
    }

    public TimePoint getTimestamp() {
        return timestamp;
    }

    public int getGpsID() {
        return gpsID;
    }

    public Position getPosition() {
        return position;
    }

    public Speed getSpeed() {
        return speed;
    }

    public int getNumberOfSatellites() {
        return numberOfSatellites;
    }

    public int getBatteryPercent() {
        return batteryPercent;
    }
    
}

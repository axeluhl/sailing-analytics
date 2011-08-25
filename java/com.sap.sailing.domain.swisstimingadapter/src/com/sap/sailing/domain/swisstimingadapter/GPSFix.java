package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;

public class GPSFix {
    private final String raceID;
    private final int packetID;
    private final TimePoint timestamp;
    private final short gpsID;
    private final Position position;
    private final Speed speed;
    private final byte numberOfSatellites;
    private final byte batteryPercent;
    
    public GPSFix(String raceID, int packetID, TimePoint timestamp, short gpsID, Position position, Speed speed,
            byte numberOfSatellites, byte batteryPercent) {
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

    public short getGpsID() {
        return gpsID;
    }

    public Position getPosition() {
        return position;
    }

    public Speed getSpeed() {
        return speed;
    }

    public byte getNumberOfSatellites() {
        return numberOfSatellites;
    }

    public byte getBatteryPercent() {
        return batteryPercent;
    }
    
}

package com.sap.sailing.expeditionconnector.impl;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.expeditionconnector.ExpeditionMessage;

public class ExpeditionMessageImpl implements ExpeditionMessage {
    private final int boatID;
    private final Map<Integer, Double> values;
    private final boolean valid;
    private final long createdAtMillis;
    
    public ExpeditionMessageImpl(int boatID, Map<Integer, Double> values, boolean valid) {
        this.boatID = boatID;
        // ensure that nobody can manipulate the map used by this message object from outside
        this.values = new HashMap<Integer, Double>(values);
        this.valid = valid;
        this.createdAtMillis = System.currentTimeMillis();
    }
    
    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public int getBoatID() {
        return boatID;
    }

    @Override
    public Set<Integer> getVariableIDs() {
        return Collections.unmodifiableSet(values.keySet());
    }

    @Override
    public boolean hasValue(int variableID) {
        return values.containsKey(variableID);
    }

    @Override
    public double getValue(int variableID) {
        if (!hasValue(variableID)) {
            throw new IllegalArgumentException("Variable ID "+variableID+" not present in message");
        }
        return values.get(variableID);
    }

    @Override
    public GPSFix getGPSFix() {
        if (hasValue(ID_GPS_LAT) && hasValue(ID_GPS_LNG)) {
            TimePoint timePoint;
            if (hasValue(ID_GPS_TIME)) {
                TimeZone UTC = TimeZone.getTimeZone("UTC");
                if (UTC == null) {
                    UTC = TimeZone.getTimeZone("GMT");
                }
                GregorianCalendar cal = new GregorianCalendar(UTC);
                cal.set(1899, 11, 30, 0, 0, 0);
                timePoint = new MillisecondsTimePoint((long)
                        (getValue(ID_GPS_TIME)*24*3600*1000) +   // this is the milliseconds since 31.12.1899 0:00:00 UTC
                        cal.getTimeInMillis());
            } else {
                timePoint = new MillisecondsTimePoint(createdAtMillis);
            }
            return new GPSFixImpl(new DegreePosition(getValue(ID_GPS_LAT), getValue(ID_GPS_LNG)), timePoint);
        } else {
            return null;
        }
    }

    @Override
    public GPSFixMoving getGPSFixMoving() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpeedWithBearing getTrueWind() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpeedWithBearing getSpeedOverGround() {
        // TODO Auto-generated method stub
        return null;
    }

}

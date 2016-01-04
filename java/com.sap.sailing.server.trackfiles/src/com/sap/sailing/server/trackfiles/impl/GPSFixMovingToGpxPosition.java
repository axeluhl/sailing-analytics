package com.sap.sailing.server.trackfiles.impl;

import slash.common.type.CompactCalendar;
import slash.navigation.gpx.GpxPosition;

import com.sap.sailing.domain.common.tracking.GPSFixMoving;

/**
 * Speed in m/s and bearing is in Degrees
 * 
 * @author Fredrik Teschke
 * 
 */
public enum GPSFixMovingToGpxPosition implements WaypointCreator<GPSFixMoving> {
    INSTANCE;

    @Override
    public GpxPosition getPosition(GPSFixMoving waypoint) {
        GpxPosition p = new GpxPosition(waypoint.getPosition().getLngDeg(), // lon
                waypoint.getPosition().getLatDeg(), // lat
                0., // elevation
                waypoint.getSpeed().getKilometersPerHour(), // speed
                CompactCalendar.fromDate(waypoint.getTimePoint().asDate()), // time
                "" // comment
        );

        p.setHeading(waypoint.getSpeed().getBearing().getDegrees());
        return p;
    }

}
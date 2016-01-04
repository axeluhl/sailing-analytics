package com.sap.sailing.server.trackfiles.impl;

import slash.common.type.CompactCalendar;
import slash.navigation.gpx.GpxPosition;

import com.sap.sailing.domain.common.tracking.GPSFix;

public enum GPSFixToGpxPosition implements WaypointCreator<GPSFix> {
    INSTANCE;

    @Override
    public GpxPosition getPosition(GPSFix waypoint) {
        return new GpxPosition(waypoint.getPosition().getLngDeg(), // lon
                waypoint.getPosition().getLatDeg(), // lat
                0., // elevation
                0., // speed
                CompactCalendar.fromDate(waypoint.getTimePoint().asDate()), // time
                "" // comment
        );
    }

}
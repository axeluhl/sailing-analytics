package com.sap.sailing.server.trackfiles.impl;

import slash.common.type.CompactCalendar;
import slash.navigation.gpx.GpxPosition;

import com.sap.sailing.domain.tracking.Maneuver;

/**
 * Speed in m/s and bearing is in Degrees
 * 
 * @author Fredrik Teschke
 * 
 */
public enum ManeuverToGpxPosition implements WaypointCreator<Maneuver> {
    INSTANCE;

    @Override
    public GpxPosition getPosition(Maneuver maneuver) {
        GpxPosition p = new GpxPosition(maneuver.getPosition().getLngDeg(), // lon
                maneuver.getPosition().getLatDeg(), // lat
                0., // elevation
                0., // speed
                CompactCalendar.fromDate(maneuver.getTimePoint().asDate()), // time
                maneuver.getType().toString() // comment
        );

        p.setHeading(maneuver.getDirectionChangeInDegrees());
        return p;
    }

}
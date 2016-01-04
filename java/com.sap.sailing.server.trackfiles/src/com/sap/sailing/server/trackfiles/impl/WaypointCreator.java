package com.sap.sailing.server.trackfiles.impl;

import slash.navigation.gpx.GpxPosition;

import com.sap.sailing.domain.common.tracking.GPSFix;

/**
 * Used for export functionality, converts some kind of input (e.g. GPSFix) to some Output position (e.g. GPX waypoint).
 * 
 * @author Fredrik Teschke
 * 
 * @param <I>
 *            Input format, e.g. {@link GPSFix} (I for in)
 */
public interface WaypointCreator<I> {
    GpxPosition getPosition(I waypoint);
}

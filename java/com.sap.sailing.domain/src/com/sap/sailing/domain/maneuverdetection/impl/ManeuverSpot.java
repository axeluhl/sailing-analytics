package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sse.common.TimePoint;

/**
 * Represents a spot within the track of competitor, where maneuvers have been detected by analysis of a douglas peucker
 * fixes group. The spot contains the analysed douglas peucker fixes group and the course change direction within
 * douglas peucker fixes group of this spot.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpot {

    private final List<GPSFixMoving> douglasPeuckerFixes;
    private final NauticalSide maneuverSpotDirection;
    private final CompleteManeuverCurve maneuverCurve;

    public ManeuverSpot(List<GPSFixMoving> douglasPeuckerFixes, NauticalSide maneuverSpotDirection,
            CompleteManeuverCurve maneuverCurve) {
        this.douglasPeuckerFixes = Collections.unmodifiableList(new ArrayList<>(douglasPeuckerFixes));
        this.maneuverCurve = maneuverCurve;
        this.maneuverSpotDirection = maneuverSpotDirection;
    }

    /**
     * Gets douglas peucker fixes group which represents this spot.
     */
    public List<GPSFixMoving> getDouglasPeuckerFixes() {
        return douglasPeuckerFixes;
    }

    /**
     * Gets the course change direction within this spot.
     */
    public NauticalSide getManeuverSpotDirection() {
        return maneuverSpotDirection;
    }

    public CompleteManeuverCurve getManeuverCurve() {
        return maneuverCurve;
    }

    public TimePoint getTimePoint() {
        return douglasPeuckerFixes.get(0).getTimePoint();
    }

}

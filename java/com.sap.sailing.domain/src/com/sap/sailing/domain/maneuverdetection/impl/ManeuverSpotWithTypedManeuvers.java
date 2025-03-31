package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.List;

import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.Maneuver;

/**
 * In addition to {@link ManeuverSpot}, contains determined maneuvers and the wind measurements performed during
 * analysis of this spot.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSpotWithTypedManeuvers extends ManeuverSpot {

    private final List<Maneuver> maneuvers;
    private final WindMeasurement windMeasurement;

    public ManeuverSpotWithTypedManeuvers(List<GPSFixMoving> douglasPeuckerFixes, NauticalSide maneuverSpotDirection,
            CompleteManeuverCurve maneuverCurve, List<Maneuver> maneuvers, WindMeasurement windMeasurement) {
        super(douglasPeuckerFixes, maneuverSpotDirection, maneuverCurve);
        this.maneuvers = maneuvers;
        this.windMeasurement = windMeasurement;
    }

    public ManeuverSpotWithTypedManeuvers(ManeuverSpot maneuverSpot, List<Maneuver> maneuvers,
            WindMeasurement windMeasurement) {
        this(maneuverSpot.getDouglasPeuckerFixes(), maneuverSpot.getManeuverSpotDirection(),
                maneuverSpot.getManeuverCurve(), maneuvers, windMeasurement);
    }

    /**
     * Gets maneuvers discovered within this spot.
     */
    public List<Maneuver> getManeuvers() {
        return maneuvers;
    }

    /**
     * Gets wind measurement data measured during analysis of this spot.
     */
    public WindMeasurement getWindMeasurement() {
        return windMeasurement;
    }

}

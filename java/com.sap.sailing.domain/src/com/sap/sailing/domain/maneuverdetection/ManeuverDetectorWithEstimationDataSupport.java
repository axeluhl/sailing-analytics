package com.sap.sailing.domain.maneuverdetection;

import java.util.List;

import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.Maneuver;

/**
 * A maneuver detector which additional support for management of estimation data for wind estimation.
 * 
 * @author Vladislav Chumak (D069712)
 * @see ManeuverDetector
 *
 */
public interface ManeuverDetectorWithEstimationDataSupport extends ManeuverDetector {
    /**
     * Derives maneuvers from the provided {@code maneuverCurves}. Since the provided complete maneuver curves already
     * include the calculated boundaries of each complete maneuvering spot, this method operates in a very
     * performance-efficient manner.
     * 
     * @param maneuverCurves
     *            The maneuver curves from which the maneuvers shall be derived
     * @return The maneuvers derived from the provided maneuver curves. The list gets empty, if the provided maneuver
     *         curves list is also empty.
     */
    List<Maneuver> detectManeuvers(Iterable<CompleteManeuverCurve> maneuverCurves);

    /**
     * Detects the complete maneuver curves performed within a GPS-track of the competitor associated with this
     * {@link ManeuverDetector}-instance. In contrast to maneuvers determined by {@link #detectManeuvers()}, the
     * complete maneuver curves are not subject to any splitting logic for maneuvers with multiple "tacking" and
     * "jibing". See {@link ManeuverDetector} description for more info regarding the detection strategy.
     * 
     * @return an empty list if no maneuver spots were detected, otherwise the list with detected maneuver curves.
     * @see CompleteManeuverCurve
     * @see ManeuverDetector
     */
    List<CompleteManeuverCurve> detectCompleteManeuverCurves();

    /**
     * Parses {@link CompleteManeuverCurve}-instances from provided {@link Maneuver}-instances. This method performs
     * significantly faster than {@link #detectCompleteManeuverCurves()}.
     * 
     * @param maneuvers
     *            The maneuvers to parse into complete maneuver curves
     * @return an empty list if provided maneuvers list is empty, otherwise the list with complete maneuver curves
     *         derived from provided maneuvers.
     * @see CompleteManeuverCurve
     * @see Maneuver
     */
    List<CompleteManeuverCurve> getCompleteManeuverCurves(Iterable<Maneuver> maneuvers);

    /**
     * Converts provided {@link CompleteManeuverCurve}-instances into
     * {@link CompleteManeuverCurveWithEstimationData}-instances. For this, additional information to
     * {@code maneuverCurves} is computed. This computation is regarded as complex as the computation within
     * {@link #detectManeuvers()}.
     */
    List<CompleteManeuverCurveWithEstimationData> getCompleteManeuverCurvesWithEstimationData(
            Iterable<CompleteManeuverCurve> maneuverCurves);
}

package com.sap.sailing.domain.maneuverdetection;

import java.util.List;

import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.Maneuver;

/**
 * Determines maneuvers performed within a tracked race by competitor associated with this
 * {@link ManeuverDetector}-instance. The steps of maneuver detection look as follows:
 * <ol>
 * <li>Douglas-Peucker-fixes set is determined for the GPS-track of competitor. The analyzed GPS-fixes set gets limited
 * by the set contained between the time points of crossing start line and finish-line. The limit does not apply if no
 * start-line has been crossed by the competitor at all. In case of absent crossing of finish-line, the GPS-fix set gets
 * analyzed until the last available GPS-fix.</li>
 * <li>The bearings between DP-fixes are calculated</li>
 * <li>Consecutive DP-fixes get grouped together if their sign of associated bearing is equal and the duration and
 * distance limits between DP-fixes is satisfied.</li>
 * <li>For each DP-fixes group a {@link CompleteManeuverCurve} gets determined</li>
 * <li>For each {@link CompleteManeuverCurve}-instance appropriate {@link Maneuver}-instance(s) are determined. In
 * contrast to maneuvers, the complete maneuver curves are not subject to any splitting logic for maneuvers with
 * multiple "tacking" and "jibing".</li>
 * <ol>
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverDetector {

    /**
     * Detects maneuvers performed within a GPS-track of the competitor associated with this
     * {@link ManeuverDetector}-instance. See {@link ManeuverDetector} description for more info regarding the detection
     * strategy.
     * 
     * @return an empty list if no maneuvers were detected, otherwise the list with detected maneuvers.
     */
    List<Maneuver> detectManeuvers();

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

}

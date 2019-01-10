package com.sap.sailing.domain.maneuverdetection;

import java.util.List;

import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.Maneuver;

/**
 * An extension of {@link ManeuverDetector} which supports incremental maneuver detection within calls of
 * {@link #detectManeuvers()}. The purpose of this implementation is to save performance for maneuver detection during
 * live races with continuously new incoming fixes. The implementations must take care of concurrency, when state is
 * introduced.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface IncrementalManeuverDetector extends ManeuverDetector {

    /**
     * Gets the already detected maneuvers by previous calls of {@link #detectManeuvers()} and
     * {@link #detectCompleteManeuverCurves()}.
     */
    List<Maneuver> getAlreadyDetectedManeuvers();

    /**
     * Gets the already detected maneuver curves by previous calls of {@link #detectManeuvers()} and
     * {@link #detectCompleteManeuverCurves()}.
     */
    List<CompleteManeuverCurve> getAlreadyDetectedManeuverCurves();

    /**
     * Clears the whole state of the detector, which is used for incremental maneuver detection. The following calls of
     * {@link #detectManeuvers()} will cause the maneuver analysis to perform from scratch to detect maneuvers.
     */
    void clearState();

    /**
     * Gets the count of how often incremental maneuver detection computation was triggered to produce the current state
     * with increment data.
     */
    int getIncrementalRunsCount();

}

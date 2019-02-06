package com.sap.sailing.domain.windestimation;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.maneuverdetection.TrackTimeInfo;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;

/**
 * Interface which is meant to be used by maneuver detection in order to notify the wind estimator instance about new
 * maneuvers detected.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimationInteraction {

    /**
     * Must be called when new maneuvers are detected by maneuver detector.
     * 
     * @param competitor
     *            The competitor on whose track the provided maneuvers were detected
     * @param newManeuvers
     *            The new maneuvers within the track of provided competitor
     * @param trackTimeInfo
     *            Information about the track of provided competitor determined by maneuver detector
     */
    void newManeuverSpotsDetected(Competitor competitor, Iterable<CompleteManeuverCurve> newManeuvers,
            TrackTimeInfo trackTimeInfo);

}

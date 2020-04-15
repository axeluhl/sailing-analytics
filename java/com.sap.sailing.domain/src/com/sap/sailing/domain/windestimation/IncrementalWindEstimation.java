package com.sap.sailing.domain.windestimation;

import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Wind estimator which interacts with maneuver detector and maintains a wind track with estimated wind fixes. All in
 * all, this instance can be seen as a wind source with corresponding wind track. The managed wind track is prone to
 * changes which are communicated to tracked race via its
 * {@link TrackedRace#recordWind(com.sap.sailing.domain.common.Wind, WindSource, boolean)} and
 * {@link TrackedRace#removeWind(com.sap.sailing.domain.common.Wind, WindSource)}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface IncrementalWindEstimation extends WindEstimationInteraction {

    /**
     * @return The wind source assigned to this wind track
     */
    WindSource getWindSource();

    /**
     * Gets the produced wind track of this wind estimation
     */
    WindTrack getWindTrack();

}

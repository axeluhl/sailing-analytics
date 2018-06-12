package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;

/**
 * This trivial "cache" implementation doesn't cache and may be used as a default for those cases where only few
 * calculations are to be done and creating the caching structure would cost more cycles than simply doing the
 * calculation.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class NoCachingWindLegTypeAndLegBearingCache implements WindLegTypeAndLegBearingCache {
    @Override
    public Wind getWind(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint) {
        return trackedRace.getWind(trackedRace.getTrack(competitor).getEstimatedPosition(timePoint, false), timePoint);
    }

    @Override
    public LegType getLegType(TrackedLeg trackedLeg, TimePoint timePoint) throws NoWindException {
        return trackedLeg.getLegType(timePoint);
    }

    @Override
    public Bearing getLegBearing(TrackedLeg trackedLeg, TimePoint timePoint) {
        return trackedLeg.getLegBearing(timePoint);
    }
}

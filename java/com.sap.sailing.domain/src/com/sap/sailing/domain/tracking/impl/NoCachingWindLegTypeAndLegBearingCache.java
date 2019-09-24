package com.sap.sailing.domain.tracking.impl;

import java.util.function.Function;
import java.util.function.Supplier;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveCourse;
import com.sap.sailing.domain.orc.ORCPerformanceCurve;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

/**
 * This trivial "cache" implementation doesn't cache and may be used as a default for those cases where only few
 * calculations are to be done and creating the caching structure would cost more cycles than simply doing the
 * calculation.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class NoCachingWindLegTypeAndLegBearingCache implements WindLegTypeAndLegBearingAndORCPerformanceCurveCache {
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

    @Override
    public ORCPerformanceCurveCourse getTotalCourse(Supplier<ORCPerformanceCurveCourse> totalCourseSupplier) {
        return totalCourseSupplier.get();
    }

    @Override
    public Competitor getScratchBoat(Supplier<Competitor> scratchBoatSupplier) {
        return scratchBoatSupplier.get();
    }

    @Override
    public ORCPerformanceCurve getPerformanceCurveForPartialCourse(Competitor competitor,
            Function<Competitor, ORCPerformanceCurve> performanceCurveSupplier) {
        return performanceCurveSupplier.apply(competitor);
    }

    @Override
    public Speed getImpliedWind(Competitor competitor, Function<Competitor, Speed> impliedWindSupplier) {
        return impliedWindSupplier.apply(competitor);
    }
}

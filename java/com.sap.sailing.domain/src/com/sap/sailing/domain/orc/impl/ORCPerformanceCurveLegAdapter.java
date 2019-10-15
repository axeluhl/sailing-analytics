package com.sap.sailing.domain.orc.impl;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

/**
 * Adapts a {@link TrackedLeg} to the {@link ORCPerformanceCurveLeg} interface, using as length the
 * {@link TrackedLeg#getWindwardDistance() "windward distance"} (which for reaching legs is defined as the great circle
 * distance between the leg's start and end waypoint) for the leg's {@link TrackedLeg#getReferenceTimePoint() reference
 * time point}, and as the {@link #getTwa() TWA} the angular difference between this leg's bearing and the TWA as
 * obtained from the {@link TrackedRace#getWind(com.sap.sailing.domain.common.Position, TimePoint) TrackedRace} of which
 * the {@link TrackedLeg} passed to the constructor is a part.
 */
public class ORCPerformanceCurveLegAdapter implements ORCPerformanceCurveLeg {
    private final TrackedLeg trackedLeg;
    
    public ORCPerformanceCurveLegAdapter(TrackedLeg trackedLeg) {
        this.trackedLeg = trackedLeg;
    }

    @Override
    public Distance getLength() {
        return trackedLeg.getWindwardDistance();
    }

    public Distance getLength(WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return trackedLeg.getWindwardDistance(cache);
    }

    @Override
    public Bearing getTwa() {
        final TimePoint referenceTimePoint = trackedLeg.getReferenceTimePoint();
        return trackedLeg.getLegBearing(referenceTimePoint).getDifferenceTo(
                trackedLeg.getTrackedRace().getWind(trackedLeg.getMiddleOfLeg(referenceTimePoint),
                        referenceTimePoint).getFrom());
    }

    @Override
    public ORCPerformanceCurveLegTypes getType() {
        return ORCPerformanceCurveLegTypes.TWA;
    }

    @Override
    public ORCPerformanceCurveLeg scale(final double share) {
        return new ORCPerformanceCurveLegAdapter(trackedLeg) {
            @Override
            public Distance getLength() {
                return ORCPerformanceCurveLegAdapter.this.getLength().scale(share);
            }
        };
    }

    @Override
    public String toString() {
        return "ORCPerformanceCurveLegAdapter for "+trackedLeg.getLeg()+": length="+getLength().getNauticalMiles()+"NM, TWA="+getTwa();
    }
}

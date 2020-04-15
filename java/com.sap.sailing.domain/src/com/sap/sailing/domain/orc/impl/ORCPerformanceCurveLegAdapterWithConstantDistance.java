package com.sap.sailing.domain.orc.impl;

import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;

/**
 * Adapts a {@link TrackedLeg} to the {@link ORCPerformanceCurveLeg} interface. If wind information is known, the leg
 * will be of type {@link ORCPerformanceCurveLegTypes#TWA}, using as length the {@link TrackedLeg#getWindwardDistance()
 * "windward distance"} (which for reaching legs is defined as the great circle distance between the leg's start and end
 * waypoint) for the leg's {@link TrackedLeg#getReferenceTimePoint() reference time point}, and as the {@link #getTwa()
 * TWA} the angular difference between this leg's bearing and the TWA as obtained from the
 * {@link TrackedRace#getWind(com.sap.sailing.domain.common.Position, TimePoint) TrackedRace} of which the
 * {@link TrackedLeg} passed to the constructor is a part. If no wind information is known, the leg is emulated to
 * be of type {@link ORCPerformanceCurveLegTypes#LONG_DISTANCE}, and {@link #getTwa()} will return {@code null}.
 */
public class ORCPerformanceCurveLegAdapterWithConstantDistance extends AbstractORCPerformanceCurveTwaLegAdapter {
    private static final long serialVersionUID = -6432064480098807397L;
    private final Distance length;
    
    public ORCPerformanceCurveLegAdapterWithConstantDistance(TrackedLeg trackedLeg, Distance length) {
        super(trackedLeg);
        this.length = length;
    }

    @Override
    public Distance getLength() {
        return length;
    }

    public Distance getLength(WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return length;
    }
}

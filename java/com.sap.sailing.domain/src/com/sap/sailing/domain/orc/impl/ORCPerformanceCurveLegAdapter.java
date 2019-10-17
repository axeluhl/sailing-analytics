package com.sap.sailing.domain.orc.impl;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.Bearing;
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
public class ORCPerformanceCurveLegAdapter implements ORCPerformanceCurveLeg {
    private static final long serialVersionUID = -6432064480098807397L;
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

    private Wind getWind() {
        final TimePoint referenceTimePoint = trackedLeg.getReferenceTimePoint();
        return trackedLeg.getTrackedRace().getWind(trackedLeg.getMiddleOfLeg(referenceTimePoint), referenceTimePoint);
    }

    @Override
    public Bearing getTwa() {
        final Wind wind = getWind();
        final Bearing result;
        if (wind == null) {
            result = null;
        } else {
            final TimePoint referenceTimePoint = trackedLeg.getReferenceTimePoint();
            result = trackedLeg.getLegBearing(referenceTimePoint).getDifferenceTo(wind.getFrom());
        }
        return result;
    }

    @Override
    public ORCPerformanceCurveLegTypes getType() {
        final ORCPerformanceCurveLegTypes result;
        if (getWind() == null) {
            result = ORCPerformanceCurveLegTypes.LONG_DISTANCE;
        } else {
            result = ORCPerformanceCurveLegTypes.TWA;
        }
        return result;
    }

    @Override
    public ORCPerformanceCurveLeg scale(final double share) {
        return new ORCPerformanceCurveLegAdapter(trackedLeg) {
            private static final long serialVersionUID = -6724721873285438431L;

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

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
 * Abstract base class for adapting a {@link TrackedLeg} to the {@link ORCPerformanceCurveLeg} interface. If wind
 * information is known, the leg will be of type {@link ORCPerformanceCurveLegTypes#TWA} using as the {@link #getTwa()
 * TWA} the angular difference between this leg's bearing and the TWA as obtained from the
 * {@link TrackedRace#getWind(com.sap.sailing.domain.common.Position, TimePoint) TrackedRace} of which the
 * {@link TrackedLeg} passed to the constructor is a part. If no wind information is known, the leg is emulated to be of
 * type {@link ORCPerformanceCurveLegTypes#LONG_DISTANCE}, and {@link #getTwa()} will return {@code null}.
 * <p>
 * 
 * Subclasses have to define the {@link #getLength()} method which could, e.g., return a constant {@link Distance}
 * obtained from some other definition, or the tracked windward distance of the leg.
 */
public abstract class AbstractORCPerformanceCurveTwaLegAdapter implements ORCPerformanceCurveLeg {
    private static final long serialVersionUID = -6432064480098807397L;
    private final TrackedLeg trackedLeg;
    
    public AbstractORCPerformanceCurveTwaLegAdapter(TrackedLeg trackedLeg) {
        this.trackedLeg = trackedLeg;
    }
    
    protected TrackedLeg getTrackedLeg() {
        return trackedLeg;
    }

    private Wind getWind() {
        final Wind result;
        if (trackedLeg == null || trackedLeg.getTrackedRace() == null) {
            result = null;
        } else {
            final TimePoint referenceTimePoint = trackedLeg.getReferenceTimePoint();
            result = trackedLeg.getTrackedRace().getWind(trackedLeg.getMiddleOfLeg(referenceTimePoint), referenceTimePoint);
        }
        return result;
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
        return new AbstractORCPerformanceCurveTwaLegAdapter(trackedLeg) {
            private static final long serialVersionUID = -6724721873285438431L;

            @Override
            public Distance getLength() {
                return AbstractORCPerformanceCurveTwaLegAdapter.this.getLength().scale(share);
            }

            @Override
            public Distance getLength(
                    WindLegTypeAndLegBearingAndORCPerformanceCurveCache leaderboardDTOCalculationReuseCache) {
                return AbstractORCPerformanceCurveTwaLegAdapter.this.getLength(leaderboardDTOCalculationReuseCache).scale(share);
            }
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+" for "+trackedLeg.getLeg()+": length="+getLength().getNauticalMiles()+"NM, TWA="+getTwa();
    }

    public abstract Distance getLength(WindLegTypeAndLegBearingAndORCPerformanceCurveCache leaderboardDTOCalculationReuseCache);
}

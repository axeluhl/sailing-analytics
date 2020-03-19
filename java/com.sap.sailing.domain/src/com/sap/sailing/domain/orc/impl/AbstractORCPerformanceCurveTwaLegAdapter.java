package com.sap.sailing.domain.orc.impl;

import java.util.stream.Collectors;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.confidence.impl.PositionAndTimePointWeigher;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLeg;
import com.sap.sailing.domain.common.orc.ORCPerformanceCurveLegTypes;
import com.sap.sailing.domain.confidence.ConfidenceBasedWindAverager;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

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
    private final int numParts;
    
    public AbstractORCPerformanceCurveTwaLegAdapter(TrackedLeg trackedLeg) {
        this.trackedLeg = trackedLeg;
        this.numParts = 10;
    }
    
    public AbstractORCPerformanceCurveTwaLegAdapter(TrackedLeg trackedLeg, int numParts) {
        this.trackedLeg = trackedLeg;
        this.numParts = numParts;
    }
    
    protected TrackedLeg getTrackedLeg() {
        return trackedLeg;
    }

    private Wind getWind() {
        ConfidenceBasedWindAverager<Util.Pair<Position, TimePoint>> timeWeigher = 
                ConfidenceFactory.INSTANCE.createWindAverager(new PositionAndTimePointWeigher(1000, WindTrack.WIND_HALF_CONFIDENCE_DISTANCE));
        final Iterable<TimePoint> referenceTimePoints = trackedLeg.getEquidistantReferenceTimePoints(numParts);
        Iterable<WindWithConfidence<Util.Pair<Position, TimePoint>>> winds = 
                Util.stream(referenceTimePoints).flatMap(timepoint -> {
                    return Util.stream(trackedLeg.getEquidistantSectionsOfLeg(timepoint, numParts))
                            .map(p -> trackedLeg.getTrackedRace().getWindWithConfidence(p, timepoint));
                }).collect(Collectors.toList());
        Util.Pair<Position, TimePoint> at = winds.iterator().next().getRelativeTo();
        return timeWeigher.getAverage(winds, at).getObject();
    }

    @Override
    public Bearing getTwa() {
        final Wind wind = getWind();
        final Bearing result;
        if (wind == null) {
            result = null;
        } else {
            final TimePoint referenceTimePoint = trackedLeg.getReferenceTimePoint();
            final Bearing bearing = trackedLeg.getLegBearing(referenceTimePoint);
            result = bearing != null ? bearing.getDifferenceTo(wind.getFrom()) : null;
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

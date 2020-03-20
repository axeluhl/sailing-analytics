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
        this(trackedLeg, 10);
    }
    
    public AbstractORCPerformanceCurveTwaLegAdapter(TrackedLeg trackedLeg, int numParts) {
        this.trackedLeg = trackedLeg;
        this.numParts = numParts;
    }
    
    protected TrackedLeg getTrackedLeg() {
        return trackedLeg;
    }

    /**
     * Computes a {@link Wind} estimation based on {@link #numParts} x {@link #numParts} wind samples, taken for
     * {@link #numParts} time points spread equally across the time range between the first boat entering and the last
     * boat exiting the leg (defaulting to "now" if no boat has exited the leg yet) and across {@link #numParts}
     * positions along the great circle segment connecting the approximate start waypoint's position and the approximate
     * end waypoint's position at the respective time point. Those wind samples are averaged based on their original
     * confidences. The {@link #scale(double) scaling} of this leg does not affect the wind sampling; in all cases, wind
     * samples will always be taken along the full leg distance, making the result of this method the same for the same
     * boundary conditions (mark passings etc.) for all competitors.
     */
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
        if (hasWind()) {
            result = ORCPerformanceCurveLegTypes.LONG_DISTANCE;
        } else {
            result = ORCPerformanceCurveLegTypes.TWA;
        }
        return result;
    }

    private boolean hasWind() {
        final TimePoint referenceTimePoint = trackedLeg.getReferenceTimePoint();
        final Wind result = trackedLeg.getTrackedRace().getWind(trackedLeg.getMiddleOfLeg(referenceTimePoint), referenceTimePoint);
        return result != null;
    }

    /**
     * The TWA calculation must not be affected by scaling a leg because otherwise competitors who sailed different
     * ratios of the same leg may get different {@link #getWind()} results.
     */
    @Override
    public ORCPerformanceCurveLeg scale(final double share) {
        return new AbstractORCPerformanceCurveTwaLegAdapter(trackedLeg, numParts) {
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

package com.sap.sailing.domain.common;

import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * Information about the time competitors of a race are expected to need for the legs of a race. This is based on a
 * {@link TrackedRace} with {@link TrackedLeg}s and
 * {@link TrackedRace#getWind(com.sap.sailing.domain.common.Position, com.sap.sse.common.TimePoint) wind information}.
 * Furthermore, a valid {@link PolarDataService} instance must be bound to the {@link TrackedRace} to make predictions
 * possible.
 * <p>
 * 
 * In order to give a rationale for the response, in addition to the total expected duration the expected durations for
 * each leg are specified, together with the {@link Wind} data used for the leg, leg distance, the true wind angle for
 * the leg's direction and the leg type that this leads to.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface TargetTimeInfo {
    public interface LegTargetTimeInfo {
        /**
         * The distance between the waypoints delimiting the leg, at the {@link #getExpectedStartTimePoint() expected
         * time point} at which the competitor starts the leg.
         */
        Distance getDistance();
        
        /**
         * True wind at the position in the middle between the two positions at which the waypoints delimiting
         * the leg are at {@link #getExpectedStartTimePoint()}.
         */
        Wind getWind();
        
        /**
         * The bearing from the leg's start waypoint to the leg's end waypoint at {@link #getExpectedStartTimePoint()}/
         */
        Bearing getLegBearing();
        
        /**
         * The angle between the wind direction (from) and the bearing from the leg's start waypoint position at
         * {@link #getExpectedStartTimePoint()} and the leg's end waypoint position at
         * {@link #getExpectedStartTimePoint()}. For example, if a windward mark is a few meters off to the left,
         * viewed from the leeward start line, then the wind comes a little more from the starboard side compared
         * to the course middle line, therefore would have a true wind angle to the leg of, say 5deg.
         */
        Bearing getTrueWindAngleToLeg();
        
        /**
         * The time a competitor is expected to need to sail the leg in the {@link #getWind() wind conditions expected}.
         */
        Duration getExpectedDuration();
        
        /**
         * The time point when the competitor is expected to enter this leg. For the race's first leg
         * this equals the {@link TargetTimeInfo#getExpectedStartTimePoint()} result.
         */
        TimePoint getExpectedStartTimePoint();
        
        /**
         * The type of the leg at {@link #getExpectedStartTimePoint()}, given the {@link #getWind() wind conditions}
         * and the {@link #getLegBearing() leg bearing} at that point in time.
         */
        LegType getLegType();

        Distance getExpectedDistance();
    }
    
    /**
     * The time point when a competitor whose hypothetical race is described
     * by this object starts. This time is the basis for determining wind angles
     * as input for the polar / VPP calculations. Wind is taken on each leg
     * during the expected time at which the competitor starts that leg.
     */
    TimePoint getExpectedStartTimePoint();
    
    /**
     * The expected total duration; equals the sum of the {@link LegTargetTimeInfo#getExpectedDuration() leg durations}
     * as aggregated across {@link #getLegTargetTimes()}.
     */
    Duration getExpectedDuration();
    
    /**
     * The expected total duration; equals the sum of the {@link LegTargetTimeInfo#getExpectedDuration() leg durations}
     * as aggregated across {@link #getLegTargetTimes()}.
     */
    Distance getExpectedDistance();

    /**
     * The per-leg information about the expected hypothetical sail around the course. The element order matches that of
     * the {@link Course#getLegs() legs in the race course}.
     */
    Iterable<LegTargetTimeInfo> getLegTargetTimes();
    
    /**
     * The {@link Duration#plus(Duration) sum} of {@link LegTargetTimeInfo#getExpectedDuration() expected leg durations}
     * across all legs of type {@code spentInLegsOfType}.
     */
    Duration getExpectedDuration(LegType spentInLegsOfType);
}

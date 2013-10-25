package com.sap.sailing.domain.tracking;

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.impl.TrackedLegImpl;

public interface TrackedLeg extends Serializable {
    Leg getLeg();
    
    Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors();

    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor);

    TrackedRace getTrackedRace();

    /**
     * Determines whether the current {@link #getLeg() leg} is +/- {@link #UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees
     * collinear with the current wind's bearing.
     */
    boolean isUpOrDownwindLeg(TimePoint at) throws NoWindException;
    
    LegType getLegType(TimePoint at) throws NoWindException;

    /**
     * Computes the ranks of all competitors in this leg in one sweep. There are two advantages of this operation over
     * the more fine-grained {@link TrackedLegOfCompetitor#getRank(TimePoint)}:
     * <ol>
     * <li>all ranks are available in one call</li>
     * <li>no incoming events can influence ranks between separate calls to
     * {@link TrackedLegOfCompetitor#getRank(TimePoint)}</li>
     * </ol>
     * 
     * The iteration order of the linked hash map returned is leader to last.
     */
    LinkedHashMap<Competitor, Integer> getRanks(TimePoint timePoint);

    Bearing getLegBearing(TimePoint at);

    Distance getCrossTrackError(Position p, TimePoint timePoint);

    /**
     * Must be called when the start and finish waypoint of this leg may have changed.
     */
    void waypointsMayHaveChanges();
    
    /**
     * Computes the great-circle distance of this leg based on the position of the adjacent marks at <code>timePoint</code>.
     * If any of the positions of the two adjacent marks cannot be found, <code>null</code> is returned.
     */
    Distance getGreatCircleDistance(TimePoint timePoint);

    /**
     * If the current {@link #getLeg() leg} is +/- {@link TrackedLegImpl#UPWIND_DOWNWIND_TOLERANCE_IN_DEG} degrees
     * collinear with the wind's bearing, the competitor's position is projected onto the line crossing
     * <code>mark</code> in the wind's bearing, and the distance from the projection to the <code>mark</code> is
     * returned. Otherwise, it is assumed that the leg is neither an upwind nor a downwind leg, and hence the
     * along-track distance to <code>mark</code> is returned.
     * 
     * @param at
     *            the wind estimation is performed for this point in time
     */
    Distance getWindwardDistance(Position pos1, Position pos2, TimePoint at) throws NoWindException;
}

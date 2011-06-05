package com.sap.sailing.domain.tracking;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;

/**
 * Live tracking data of a single race. The race follows a defined {@link Course} with a sequence
 * of {@link Leg}s. The tracking information of such a leg can be requested either for all
 * competitors (see {@link #getTrackedLegs()} and {@link #getTrackedLeg(Leg)}) or for a
 * single competitor (see {@link #getTrackedLeg(Competitor, Leg)}).<p>
 * 
 * The overall race standings can be requested in terms of a competitor's ranking. More
 * detailed information about what happens / happened within a leg is available from
 * {@link TrackedLeg} and {@link TrackedLegOfCompetitor}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface TrackedRace {
    RaceDefinition getRace();
    
    TimePoint getStart();
    
    TimePoint getFirstFinish();
    
    Iterable<TrackedLeg> getTrackedLegs();
    
    TrackedLeg getTrackedLeg(Leg leg);
    
    /**
     * Tracking information about the leg <code>competitor</code> is on at <code>timePoint</code>, or
     * <code>null</code> if the competitor hasn't started any leg yet at <code>timePoint</code> or has
     * already finished the race.
     */
    TrackedLegOfCompetitor getCurrentLeg(Competitor competitor, TimePoint timePoint);
    
    /**
     * Tells which leg the leader at <code>timePoint</code> is on
     */
    TrackedLeg getCurrentLeg(TimePoint timePoint);
    
    TrackedLeg getTrackedLegFinishingAt(Waypoint endOfLeg);
    
    TrackedLeg getTrackedLegStartingAt(Waypoint startOfLeg);
    
    /**
     * The raw, updating feed of a single competitor participating in this race
     */
    GPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor);
    
    /**
     * Tells the leg on which the <code>competitor</code> was at time <code>at</code>.
     * If the competitor hasn't passed the start waypoint yet, <code>null</code> is
     * returned because the competitor was not yet on any leg at that point in time. If
     * the time point happens to be after the last fix received from that competitor,
     * the last known leg for that competitor is returned. 
     */
    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, TimePoint at);
    
    TrackedLegOfCompetitor getTrackedLeg(Competitor competitor, Leg leg);
    
    /**
     * @return a sequential number counting the updates that occurred to this tracked race. Callers may use this to ask
     *         for updates newer than such a sequence number.
     */
    int getUpdateCount();
    
    int getRankDifference(Competitor competitor, Leg leg, TimePoint timePoint);
    
    /**
     * Computes the rank of the competitor in this race for the current time. 
     */
    int getRank(Competitor competitor);
    
    /**
     * Computes the rank of <code>competitor</code> in this race. A competitor is ahead of all
     * competitors that are one or more legs behind. Within the same leg, the rank is determined
     * by the windward distance to go and therefore depends on the assumptions of the wind direction
     * for the given <code>timePoint</code>.
     */
    int getRank(Competitor competitor, TimePoint timePoint);
    
    /**
     * For a competitor, computes the distance (TODO not yet clear whether over ground or
     * projected onto wind direction) into the race <code>secondsIntoTheRace</code> after
     * the race {@link TrackedRace#getStart() started}.
     */
    Distance getStartAdvantage(Competitor competitor, double secondsIntoTheRace);

    /**
     * For the given waypoint lists the {@link MarkPassing} events that describe which competitor passed the waypoint at
     * which point in time. This can, e.g., be used to sort those competitors who already finished a leg within the leg
     * that ends with <code>waypoint</code>. The remaining competitors needs to be ordered by the advantage line-related
     * distance to the waypoint.
     */
    Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint);

    /**
     * Obtains the {@link MarkPassing} for <code>competitor</code> passing <code>waypoint</code>. If no such
     * mark passing has been reported (yet), <code>null</code> is returned.
     */
    MarkPassing getMarkPassing(Competitor competitor, Waypoint waypoint);

    /**
     * Yields the track describing <code>buoy</code>'s movement over time
     */
    GPSFixTrack<Buoy, GPSFix> getTrack(Buoy buoy);

    /**
     * Obtains estimated interpolated wind information for a given position and time point.
     * The information is taken from the currently selected {@link WindSource wind source} which
     * can be selected using {@link #setWindSource}.
     */
    Wind getWind(Position p, TimePoint at);

    void setWindSource(WindSource windSource);

    WindSource getWindSource();

    WindTrack getWindTrack(WindSource windSource);

    /**
     * Waits until {@link #getUpdateCount()} is after <code>since</code>.
     * @param sinceUpdate TODO
     */
    void waitForNextUpdate(int sinceUpdate) throws InterruptedException;

    TimePoint getStartOfTracking();

    TimePoint getTimePointOfNewestEvent();

    NavigableSet<MarkPassing> getMarkPassings(Competitor competitor);
}

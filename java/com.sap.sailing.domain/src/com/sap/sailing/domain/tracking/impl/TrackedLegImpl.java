package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public class TrackedLegImpl implements TrackedLeg, RaceChangeListener<Competitor> {
    private final static double UPWIND_DOWNWIND_TOLERANCE_IN_DEG = 40; // TracTrac does 22.5, Marcus Baur suggest 40

    private final Leg leg;
    private final Map<Competitor, TrackedLegOfCompetitor> trackedLegsOfCompetitors;
    private TrackedRaceImpl trackedRace;
    private final Map<TimePoint, SortedSet<TrackedLegOfCompetitor>> competitorTracksOrderedByRank;
    
    public TrackedLegImpl(DynamicTrackedRaceImpl trackedRace, Leg leg, Iterable<Competitor> competitors) {
        super();
        this.leg = leg;
        this.trackedRace = trackedRace;
        trackedLegsOfCompetitors = new HashMap<Competitor, TrackedLegOfCompetitor>();
        for (Competitor competitor : competitors) {
            trackedLegsOfCompetitors.put(competitor, new TrackedLegOfCompetitorImpl(this, competitor));
        }
        trackedRace.addListener(this);
        competitorTracksOrderedByRank = new HashMap<TimePoint, SortedSet<TrackedLegOfCompetitor>>();
    }
    
    @Override
    public Leg getLeg() {
        return leg;
    }
    
    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors() {
        return trackedLegsOfCompetitors.values();
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor) {
        return trackedLegsOfCompetitors.get(competitor);
    }

    protected Competitor getLeader(TimePoint timePoint) {
        SortedSet<TrackedLegOfCompetitor> byRank = getCompetitorTracksOrderedByRank(timePoint);
        return byRank.first().getCompetitor();
    }

    /**
     * Orders the tracked legs for all competitors for this tracked leg for the given time point. This
     * results in an order that gives a ranking for this tracked leg. In particular, boats that have not
     * yet entered this leg will all be ranked equal because their windward distance to go is the full
     * leg's winward distance. Boats who already finished this leg have their tracks ordered by the time
     * points at which they finished the leg.<p>
     * 
     * Note that this does not reflect overall race standings. For that, the ordering would have to
     * consider the order of the boats not currently in this leg, too.
     */
    protected SortedSet<TrackedLegOfCompetitor> getCompetitorTracksOrderedByRank(TimePoint timePoint) {
        synchronized (competitorTracksOrderedByRank) {
            SortedSet<TrackedLegOfCompetitor> treeSet = competitorTracksOrderedByRank.get(timePoint);
            if (treeSet == null) {
                treeSet = new TreeSet<TrackedLegOfCompetitor>(new WindwardToGoComparator(this, timePoint));
                for (TrackedLegOfCompetitor competitorLeg : getTrackedLegsOfCompetitors()) {
                    treeSet.add(competitorLeg);
                }
                competitorTracksOrderedByRank.put(timePoint, Collections.unmodifiableSortedSet(treeSet));
            }
            return treeSet;
        }
    }
    
    @Override
    public Map<Competitor, Integer> getRanks(TimePoint timePoint) {
        SortedSet<TrackedLegOfCompetitor> orderedTrackedLegsOfCompetitors = getCompetitorTracksOrderedByRank(timePoint);
        Map<Competitor, Integer> result = new HashMap<Competitor, Integer>();
        for (TrackedLegOfCompetitor tloc : orderedTrackedLegsOfCompetitors) {
            result.put(tloc.getCompetitor(), orderedTrackedLegsOfCompetitors.headSet(tloc).size()+1);
        }
        return result;
    }

    @Override
    public boolean isUpOrDownwindLeg(TimePoint at) throws NoWindException {
        Wind wind = getWindOnLeg(at);
        if (wind == null) {
            throw new NoWindException("Need to know wind direction to determine whether leg "+getLeg()+
                    " is an upwind or downwind leg");
        }
        // check for all combinations of start/end waypoint buoys:
        for (Buoy startBuoy : getLeg().getFrom().getBuoys()) {
            Position startBuoyPos = getTrackedRace().getTrack(startBuoy).getEstimatedPosition(at, false);
            for (Buoy endBuoy : getLeg().getTo().getBuoys()) {
                Position endBuoyPos = getTrackedRace().getTrack(endBuoy).getEstimatedPosition(at, false);
                Bearing legBearing = startBuoyPos.getBearingGreatCircle(endBuoyPos);
                double deltaDeg = legBearing.getDegrees() - wind.getBearing().getDegrees();
                double deltaDegOpposite = legBearing.getDegrees() - wind.getBearing().reverse().getDegrees();
                if (Math.min(Math.abs(deltaDeg), Math.abs(deltaDegOpposite)) < UPWIND_DOWNWIND_TOLERANCE_IN_DEG) {
                    return true;
                }
            }
        }
        return false;
    }

    private Wind getWindOnLeg(TimePoint at) {
        Position approximateLegStartPosition = getTrackedRace().getTrack(
                getLeg().getFrom().getBuoys().iterator().next()).getEstimatedPosition(at, false);
        Position approximateLegEndPosition = getTrackedRace().getTrack(
                getLeg().getTo().getBuoys().iterator().next()).getEstimatedPosition(at, false);
        if (approximateLegStartPosition == null || approximateLegEndPosition == null) {
            throw new RuntimeException("No mark positions received yet for leg "+getLeg()+
                    ". Can't determine wind direction since position is not known.");
        }
        Wind wind = getWind(
                approximateLegStartPosition.translateGreatCircle(approximateLegStartPosition.getBearingGreatCircle(approximateLegEndPosition),
                        approximateLegStartPosition.getDistance(approximateLegEndPosition).scale(0.5)), at);
        return wind;
    }

    private Wind getWind(Position p, TimePoint at) {
        return getTrackedRace().getWind(p, at);
    }

    @Override
    public void gpsFixReceived(GPSFix fix, Competitor competitor) {
        clearCaches();
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        clearCaches();
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        clearCaches();
    }

    @Override
    public void markPassingReceived(MarkPassing markPassing) {
        clearCaches();
    }

    @Override
    public void windDataReceived(Wind wind) {
        clearCaches();
    }
    
    @Override
    public void windDataRemoved(Wind wind) {
        clearCaches();
    }
    
    private void clearCaches() {
        synchronized (competitorTracksOrderedByRank) {
            competitorTracksOrderedByRank.clear();
        }
    }

}

package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Provides a convenient view on the tracked leg, projecting to a single competitor's performance.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TrackedLegOfCompetitorImpl implements TrackedLegOfCompetitor {
    private final TrackedLegImpl trackedLeg;
    private final Competitor competitor;
    
    public TrackedLegOfCompetitorImpl(TrackedLegImpl trackedLeg, Competitor competitor) {
        this.trackedLeg = trackedLeg;
        this.competitor = competitor;
    }

    protected TrackedLegImpl getTrackedLeg() {
        return trackedLeg;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public Leg getLeg() {
        return trackedLeg.getLeg();
    }
    
    private TrackedRace getTrackedRace() {
        return getTrackedLeg().getTrackedRace();
    }

    @Override
    public long getTimeInMilliSeconds() {
        long result = -1;
        MarkPassing passedEndWaypoint = getTrackedRace().getMarkPassing(getCompetitor(), getTrackedLeg().getLeg().getTo());
        if (passedEndWaypoint != null) {
            result = passedEndWaypoint.getTimePoint().asMillis() - getTrackedRace().getStart().asMillis();
        }
        return result;
    }

    @Override
    public Distance getDistanceTraveled() {
        return getDistanceTraveled(MillisecondsTimePoint.now());
    }
    
    private Distance getDistanceTraveled(TimePoint until) {
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            return Distance.NULL;
        } else {
            MarkPassing legEnd = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getTo());
            TimePoint end;
            if (legEnd == null) {
                // leg not yet finished; take time specified
                end = until;
            } else {
                end = legEnd.getTimePoint();
            }
            return getTrackedRace().getTrack(getCompetitor()).getDistanceTraveled(legStart.getTimePoint(), end);
        }
    }

    private MarkPassing getMarkPassingForLegStart() {
        MarkPassing legStart = getTrackedRace().getMarkPassing(getCompetitor(), getLeg().getFrom());
        return legStart;
    }

    @Override
    public Speed getAverageVelocityMadeGood() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Speed getAverageSpeedOverGround() {
        MarkPassing legStart = getMarkPassingForLegStart();
        if (legStart == null) {
            return null;
        } else {
            TimePoint now = MillisecondsTimePoint.now();
            Distance d = getDistanceTraveled(now);
            long millis = getTimeInMilliSeconds();
            if (millis == -1) {
                // didn't finish the leg yet
                millis = now.asMillis() - legStart.getTimePoint().asMillis();
            }
            return d.inTime(millis);
        }
    }

    @Override
    public Speed getMaximumSpeedOverGround() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getNumberOfTacks() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfJibes() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfDirectionChanges() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Distance getWindwardDistanceToGo() {
        // TODO Auto-generated method stub
        return null;
    }

}

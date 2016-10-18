package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.MarkPassingManeuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;

public class MarkPassingWithContext implements HasMarkPassingContext {

    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitor;
    private final MarkPassingManeuver maneuver;
    
    private Double absoluteRank;
    private boolean rankHasBeenInitialized;

    public MarkPassingWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitor, MarkPassingManeuver maneuver) {
        this.trackedLegOfCompetitor = trackedLegOfCompetitor;
        this.maneuver = maneuver;
    }

    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return trackedLegOfCompetitor;
    }

    @Override
    public MarkPassingManeuver getManeuver() {
        return maneuver;
    }

    @Override
    public Tack getTack() {
        return getManeuver().getNewTack();
    }
    
    @Override
    public Waypoint getWaypoint() {
        return getManeuver().getWaypointPassed();
    }
    
    @Override
    public NauticalSide getPassingSide() {
        return getManeuver().getSide();
    }

    @Override
    public SpeedWithBearing getSpeedBefore() {
        return getManeuver().getSpeedWithBearingBefore();
    }

    @Override
    public SpeedWithBearing getSpeedAfter() {
        return getManeuver().getSpeedWithBearingAfter();
    }

    @Override
    public Double getDirectionChangeInDegrees() {
        return getManeuver().getDirectionChangeInDegrees();
    }

    @Override
    public Double getRelativeRank() {
        Leaderboard leaderboard = getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getLeaderboardContext().getLeaderboard();
        double competitorCount = Util.size(leaderboard.getCompetitors());
        return getAbsoluteRank() == null ? null : getAbsoluteRank() / competitorCount;
    }

    @Override
    public Double getAbsoluteRank() {
        if (!rankHasBeenInitialized) {
            TrackedRace trackedRace = getTrackedLegOfCompetitorContext().getTrackedLegContext().getTrackedRaceContext().getTrackedRace();
            Competitor competitor = getTrackedLegOfCompetitorContext().getCompetitor();
            int rank = trackedRace.getRank(competitor, getManeuver().getTimePoint());
            absoluteRank = rank == 0 ? null : Double.valueOf(rank);
            rankHasBeenInitialized = true;
        }
        return absoluteRank;
    }

}

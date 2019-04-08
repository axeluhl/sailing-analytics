package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;

public class MarkPassingWithContext implements HasMarkPassingContext {
    private static final long serialVersionUID = -337042113749307686L;
    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitor;
    private final Maneuver maneuver;
    
    private Double absoluteRank;
    private boolean rankHasBeenInitialized;

    public MarkPassingWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitor, Maneuver markPassingManeuver) {
        this.trackedLegOfCompetitor = trackedLegOfCompetitor;
        this.maneuver = markPassingManeuver;
    }

    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return trackedLegOfCompetitor;
    }

    @Override
    public Maneuver getManeuver() {
        return maneuver;
    }
    
    @Override
    public Waypoint getWaypoint() {
        return getManeuver().getMarkPassing().getWaypoint();
    }
    
    @Override
    public NauticalSide getPassingSide() {
        return getManeuver().getToSide();
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
            TrackedRace trackedRace = getTrackedLegOfCompetitorContext().getTrackedRace();
            Competitor competitor = getTrackedLegOfCompetitorContext().getCompetitor();
            int rank = trackedRace.getRank(competitor, getManeuver().getTimePoint());
            absoluteRank = rank == 0 ? null : Double.valueOf(rank);
            rankHasBeenInitialized = true;
        }
        return absoluteRank;
    }
}

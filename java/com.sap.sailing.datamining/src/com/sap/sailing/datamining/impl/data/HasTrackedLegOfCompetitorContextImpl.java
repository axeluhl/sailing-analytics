package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasTrackedLegOfCompetitorContextImpl extends HasTrackedLegContextImpl implements HasTrackedLegOfCompetitorContext {

    private final Competitor competitor;

    public HasTrackedLegOfCompetitorContextImpl(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard,
            CourseArea courseArea, Fleet fleet, TrackedRace trackedRace, TrackedLeg trackedLeg, int legNumber, Competitor competitor) {
        super(leaderboardGroup, leaderboard, courseArea, fleet, trackedRace, trackedLeg, legNumber);
        this.competitor = competitor;
    }

    public HasTrackedLegOfCompetitorContextImpl(HasTrackedLegContext trackedLegContext, Competitor competitor) {
        this(trackedLegContext.getLeaderboardGroup(), trackedLegContext.getLeaderboard(), trackedLegContext.getCourseArea(),
             trackedLegContext.getFleet(), trackedLegContext.getTrackedRace(), trackedLegContext.getTrackedLeg(), trackedLegContext.getLegNumber(),
             competitor);
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

}

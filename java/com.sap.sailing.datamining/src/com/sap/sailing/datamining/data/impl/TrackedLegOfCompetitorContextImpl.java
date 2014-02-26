package com.sap.sailing.datamining.data.impl;

import com.sap.sailing.datamining.data.TrackedLegContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedLegOfCompetitorContextImpl extends TrackedLegContextImpl implements TrackedLegOfCompetitorContext {

    private Competitor competitor;

    public TrackedLegOfCompetitorContextImpl(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard,
            CourseArea courseArea, Fleet fleet, TrackedRace trackedRace, TrackedLeg trackedLeg, int legNumber, Competitor competitor) {
        super(leaderboardGroup, leaderboard, courseArea, fleet, trackedRace, trackedLeg, legNumber);
        this.competitor = competitor;
    }

    public TrackedLegOfCompetitorContextImpl(TrackedLegContext trackedLegContext, Competitor competitor) {
        this(trackedLegContext.getLeaderboardGroup(), trackedLegContext.getLeaderboard(), trackedLegContext.getCourseArea(),
             trackedLegContext.getFleet(), trackedLegContext.getTrackedRace(), trackedLegContext.getTrackedLeg(), trackedLegContext.getLegNumber(),
             competitor);
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

}

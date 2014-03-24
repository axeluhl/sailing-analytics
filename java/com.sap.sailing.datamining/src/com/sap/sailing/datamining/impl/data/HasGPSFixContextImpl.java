package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasGPSFixContextImpl extends HasTrackedLegOfCompetitorContextImpl implements HasGPSFixContext {

    public HasGPSFixContextImpl(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard, CourseArea courseArea,
            Fleet fleet, TrackedRace trackedRace, TrackedLeg trackedLeg, int legNumber, Competitor competitor) {
        super(leaderboardGroup, leaderboard, courseArea, fleet, trackedRace, trackedLeg, legNumber, competitor);
    }

    public HasGPSFixContextImpl(HasTrackedLegOfCompetitorContext baseContext) {
        this(baseContext.getLeaderboardGroup(), baseContext.getLeaderboard(), baseContext.getCourseArea(), baseContext.getFleet(),
             baseContext.getTrackedRace(), baseContext.getTrackedLeg(), baseContext.getLegNumber(),
             baseContext.getCompetitor());
    }
    
}
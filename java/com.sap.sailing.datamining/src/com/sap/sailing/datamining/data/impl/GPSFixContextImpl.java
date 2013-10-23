package com.sap.sailing.datamining.data.impl;

import com.sap.sailing.datamining.data.GPSFixContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class GPSFixContextImpl extends TrackedLegOfCompetitorContextImpl implements GPSFixContext {

    public GPSFixContextImpl(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard, CourseArea courseArea,
            Fleet fleet, TrackedRace trackedRace, TrackedLeg trackedLeg, int legNumber, Competitor competitor) {
        super(leaderboardGroup, leaderboard, courseArea, fleet, trackedRace, trackedLeg, legNumber, competitor);
    }

    public GPSFixContextImpl(TrackedLegOfCompetitorContext baseContext) {
        this(baseContext.getLeaderboardGroup(), baseContext.getLeaderboard(), baseContext.getCourseArea(), baseContext.getFleet(),
             baseContext.getTrackedRace(), baseContext.getTrackedLeg(), baseContext.getLegNumber(),
             baseContext.getCompetitor());
    }
    
}
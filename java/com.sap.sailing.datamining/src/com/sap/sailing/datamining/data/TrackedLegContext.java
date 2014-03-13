package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface TrackedLegContext {

    public LeaderboardGroup getLeaderboardGroup();
    public Leaderboard getLeaderboard();
    public CourseArea getCourseArea();
    public Fleet getFleet();
    public TrackedRace getTrackedRace();
    public TrackedLeg getTrackedLeg();
    public LegType getLegType();
    public int getLegNumber();
    public Integer getYear();

}
package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class GPSFixContextImpl implements GPSFixContext {
    
    private LeaderboardGroup leaderboardGroup;
    private Leaderboard leaderboard;
    private CourseArea courseArea;
    private Fleet fleet;
    private TrackedRace trackedRace;
    private TrackedLeg trackedLeg;
    private int legNumber;
    private Competitor competitor;

    public GPSFixContextImpl(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard, CourseArea courseArea,
            Fleet fleet, TrackedRace trackedRace, TrackedLeg trackedLeg, int legNumber, Competitor competitor) {
        this.leaderboardGroup = leaderboardGroup;
        this.leaderboard = leaderboard;
        this.courseArea = courseArea;
        this.fleet = fleet;
        this.trackedRace = trackedRace;
        this.trackedLeg = trackedLeg;
        this.legNumber = legNumber;
        this.competitor = competitor;
    }

    @Override
    public LeaderboardGroup getLeaderboardGroup() {
        return leaderboardGroup;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    @Override
    public CourseArea getCourseArea() {
        return courseArea;
    }

    @Override
    public Fleet getFleet() {
        return fleet;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public TrackedLeg getTrackedLeg() {
        return trackedLeg;
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }
}
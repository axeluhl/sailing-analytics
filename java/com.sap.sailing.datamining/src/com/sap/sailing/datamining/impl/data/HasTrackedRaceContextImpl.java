package com.sap.sailing.datamining.impl.data;

import java.util.Calendar;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasTrackedRaceContextImpl extends HasLeaderboardContextImpl implements HasTrackedRaceContext {

    private final CourseArea courseArea;
    private final Fleet fleet;
    private final TrackedRace trackedRace;
    private Integer year;
    private boolean yearHasBeenInitialized;
    
    public HasTrackedRaceContextImpl(HasLeaderboardContext leaderboardContext, CourseArea courseArea, Fleet fleet,
            TrackedRace trackedRace) {
        this(leaderboardContext.getLeaderboardGroup(), leaderboardContext.getLeaderboard(), courseArea, fleet, trackedRace);
    }

    public HasTrackedRaceContextImpl(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard, CourseArea courseArea,
            Fleet fleet, TrackedRace trackedRace) {
        super(leaderboardGroup, leaderboard);
        this.courseArea = courseArea;
        this.fleet = fleet;
        this.trackedRace = trackedRace;
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
    public Integer getYear() {
        if (!yearHasBeenInitialized) {
            initializeYear();
        }
        return year;
    }

    private void initializeYear() {
        TimePoint time = getTrackedRace().getStartOfRace() != null ? getTrackedRace().getStartOfRace() : getTrackedRace().getStartOfTracking();
        if (time == null) {
            year = 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time.asDate());
        year = calendar.get(Calendar.YEAR);
        yearHasBeenInitialized = true;
    }

}
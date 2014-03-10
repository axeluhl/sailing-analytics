package com.sap.sailing.datamining.impl.data;

import java.util.Calendar;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasTrackedLegContextImpl implements HasTrackedLegContext {
    
    private LeaderboardGroup leaderboardGroup;
    private Leaderboard leaderboard;
    private CourseArea courseArea;
    private Fleet fleet;
    private TrackedRace trackedRace;
    private TrackedLeg trackedLeg;
    private LegType legType;
    private int legNumber;
    private Integer year;

    private boolean legTypeHasBeenInitialized;
    private boolean yearHasBeenInitialized;

    public HasTrackedLegContextImpl(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard, CourseArea courseArea, Fleet fleet,
                                 TrackedRace trackedRace, TrackedLeg trackedLeg, int legNumber) {
        this.leaderboardGroup = leaderboardGroup;
        this.leaderboard = leaderboard;
        this.courseArea = courseArea;
        this.fleet = fleet;
        this.trackedRace = trackedRace;
        this.trackedLeg = trackedLeg;
        this.legNumber = legNumber;
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
    public LegType getLegType() {
        if (!legTypeHasBeenInitialized) {
            initializeLegType();
        }
        return legType;
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

    @Override
    public Integer getYear() {
        if (!yearHasBeenInitialized) {
            initializeYear();
        }
        return year;
    }

    private void initializeLegType() {
        try {
            legType = getTrackedLeg() == null ? null : getTrackedLeg().getLegType(getTimePointForLegType());
        } catch (NoWindException e) {
            legType = null;
        }
        legTypeHasBeenInitialized = true;
    }

    private TimePoint getTimePointForLegType() {
        TimePoint at = null;
        for (TrackedLegOfCompetitor trackedLegOfCompetitor : getTrackedLeg().getTrackedLegsOfCompetitors()) {
            TimePoint start = trackedLegOfCompetitor.getStartTime();
            TimePoint finish = trackedLegOfCompetitor.getFinishTime();
            if (start != null && finish != null) {
                at = new MillisecondsTimePoint((start.asMillis() + finish.asMillis()) / 2);
                break;
            }
        }
        return at;
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

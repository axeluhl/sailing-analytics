package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.NamedImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RaceInLeaderboardImpl extends NamedImpl implements RaceInLeaderboard {
    private TrackedRace trackedRace;
    private final Leaderboard leaderboard;
    
    public RaceInLeaderboardImpl(Leaderboard leaderboard, String name) {
        super(name);
        this.leaderboard = leaderboard;
    }

    @Override
    public int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        if (getTrackedRace() != null) {
            return leaderboard.getTotalPoints(competitor, getTrackedRace(), timePoint);
        } else {
            return 0;
        }
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public void setTrackedRace(TrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }


}

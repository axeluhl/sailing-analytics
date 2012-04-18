package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RaceInLeaderboardImpl implements RaceInLeaderboard {
    private static final long serialVersionUID = -7801617988982540470L;
    private TrackedRace trackedRace;
    private final Leaderboard leaderboard;
    private boolean medalRace;
    private String name;
    private RaceIdentifier raceIdentifier;
    
    public RaceInLeaderboardImpl(Leaderboard leaderboard, String name, boolean medalRace) {
        this.name = name;
        this.leaderboard = leaderboard;
        this.medalRace = medalRace;
    }
    
    @Override
    public int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        if (getTrackedRace() != null) {
            return leaderboard.getTotalPoints(competitor, this, timePoint);
        } else {
            return 0;
        }
    }
    
    @Override
    public boolean isMedalRace() {
        return medalRace;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public void setTrackedRace(TrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public Pair<Competitor, RaceInLeaderboard> getKey(Competitor competitor) {
        return new Pair<Competitor, RaceInLeaderboard>(competitor, this);
    }

	@Override
	public void setIsMedalRace(boolean isMedalRace) {
		this.medalRace = isMedalRace;
	}

    @Override
    public RaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }
    
    @Override
    public void setRaceIdentifier(RaceIdentifier raceIdentifier) {
        this.raceIdentifier = raceIdentifier;
    }
}

package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.RaceColumn;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RaceInLeaderboardImpl implements RaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    private TrackedRace trackedRace;
    private boolean medalRace;
    private String name;
    private RaceIdentifier raceIdentifier;
    
    public RaceInLeaderboardImpl(String name, boolean medalRace) {
        this.name = name;
        this.medalRace = medalRace;
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
    public Pair<Competitor, RaceColumn> getKey(Competitor competitor) {
        return new Pair<Competitor, RaceColumn>(competitor, this);
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

package com.sap.sailing.racecommittee.app.domain.impl;

import java.util.List;

public class LeaderboardResult {

    private List<CompetitorWithRaceRankImpl> mCompetitors;

    public LeaderboardResult(List<CompetitorWithRaceRankImpl> competitors) {
        mCompetitors = competitors;
    }

    public List<CompetitorWithRaceRankImpl> getCompetitors() {
        return mCompetitors;
    }

}

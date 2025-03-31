package com.sap.sailing.racecommittee.app.domain.impl;

public class RaceRankImpl {

    private String mRace;
    private Long mRank;

    public RaceRankImpl(String race, Long rank) {
        mRace = race;
        mRank = rank;
    }

    public Long getRank() {
        return mRank;
    }

    public String getRace() {
        return mRace;
    }
}

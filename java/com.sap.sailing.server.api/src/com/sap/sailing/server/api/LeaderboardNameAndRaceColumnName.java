package com.sap.sailing.server.api;



public class LeaderboardNameAndRaceColumnName implements RaceIdentifier {
    private static final long serialVersionUID = 6290279076772313524L;
    private String leaderboardName;
    private String raceColumnName;
    
    LeaderboardNameAndRaceColumnName() {}
    
    public LeaderboardNameAndRaceColumnName(String leaderboardName, String raceColumnName) {
        super();
        this.leaderboardName = leaderboardName;
        this.raceColumnName = raceColumnName;
    }
    public String getLeaderboardName() {
        return leaderboardName;
    }
    public String getRaceColumnName() {
        return raceColumnName;
    }

    @Override
    public Object getRace(RaceFetcher raceFetcher) {
        return raceFetcher.getRace(this);
    }
    @Override
    public Object getTrackedRace(RaceFetcher raceFetcher) {
        return raceFetcher.getTrackedRace(this);
    }
    @Override
    public Object getExistingTrackedRace(RaceFetcher raceFetcher) {
        return raceFetcher.getExistingTrackedRace(this);
    }

    @Override
    public String toString() {
        return raceColumnName;
    }
    
    
}

package com.sap.sailing.gwt.ui.shared;

public class RaceInLeaderboardDAO {
    private String raceColumnName;
    private boolean medalRace;
    private boolean trackedRace;
    public String getRaceColumnName() {
        return raceColumnName;
    }
    public void setRaceColumnName(String raceColumnName) {
        this.raceColumnName = raceColumnName;
    }
    public boolean isMedalRace() {
        return medalRace;
    }
    public void setMedalRace(boolean medalRace) {
        this.medalRace = medalRace;
    }
    public boolean isTrackedRace() {
        return trackedRace;
    }
    public void setTrackedRace(boolean trackedRace) {
        this.trackedRace = trackedRace;
    }

    
}

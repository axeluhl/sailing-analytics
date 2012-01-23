package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.server.api.RaceIdentifier;

public class RaceInLeaderboardDTO implements IsSerializable {
    private String raceColumnName;
    private boolean medalRace;
    private RaceIdentifier trackedRaceIdentifier;
    
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
        return trackedRaceIdentifier != null;
    }
    public void setRaceIdentifier(RaceIdentifier raceIdentifier) {
        this.trackedRaceIdentifier = raceIdentifier;
    }
    public RaceIdentifier getRaceIdentifier() {
        return trackedRaceIdentifier;
    }
}

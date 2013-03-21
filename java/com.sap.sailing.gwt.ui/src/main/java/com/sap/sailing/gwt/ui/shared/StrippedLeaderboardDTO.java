package com.sap.sailing.gwt.ui.shared;

public class StrippedLeaderboardDTO extends AbstractLeaderboardDTO {
    public void removeRace(String raceColumnName) {
        getRaceList().remove(getRaceColumnByName(raceColumnName));
    }
}

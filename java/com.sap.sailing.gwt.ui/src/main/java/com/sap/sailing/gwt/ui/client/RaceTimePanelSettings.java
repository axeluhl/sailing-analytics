package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class RaceTimePanelSettings extends TimePanelSettings {
    private RaceTimesInfoDTO raceTimesInfo;

    public RaceTimePanelSettings() {
        super();
    }

    public RaceTimesInfoDTO getRaceTimesInfo() {
        return raceTimesInfo;
    }

    public void setRaceTimesInfo(RaceTimesInfoDTO raceTimesInfo) {
        this.raceTimesInfo = raceTimesInfo;
    }

}

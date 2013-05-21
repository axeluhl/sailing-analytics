package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SimulatorUISelectionDTO implements IsSerializable {

    public Integer boatClassIndex;
    public Integer raceIndex;
    public Integer competitorIndex;
    public Integer legIndex;

    public SimulatorUISelectionDTO() {
        this.boatClassIndex = -1;
        this.raceIndex = -1;
        this.competitorIndex = -1;
        this.legIndex = -1;
    }

    public SimulatorUISelectionDTO(int boatClassIndex, int raceIndex, int competitorIndex, int legIndex) {
        this.boatClassIndex = boatClassIndex;
        this.raceIndex = raceIndex;
        this.competitorIndex = competitorIndex;
        this.legIndex = legIndex;
    }
}

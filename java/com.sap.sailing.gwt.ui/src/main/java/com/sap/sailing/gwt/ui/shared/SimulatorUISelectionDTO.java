package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.ui.client.SimulatorService;

public class SimulatorUISelectionDTO implements IsSerializable {
    /**
     * The index refers to the order in which {@link SimulatorService#getBoatClasses()} returns the boat classes inside
     * the {@link BoatClassDTOsAndNotificationMessage#getBoatClassDTOs()} result. Starts with 0. Can be used, e.g., for
     * the {@link SimulatorService#getPolarDiagram(Double, int)} method's {@code boatClassIndex} parameter.
     */
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

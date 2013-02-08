package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Request1TurnerDTO implements IsSerializable {

    public Integer boatClassID = 0;
    public SimulatorWindDTO firstPoint = null;
    public PositionDTO secondPoint = null;
    public Boolean leftSide = false;

    public Request1TurnerDTO() {
        this.boatClassID = 0;
        this.firstPoint = null;
        this.secondPoint = null;
        this.leftSide = false;
    }

    public Request1TurnerDTO(int boatClassID, SimulatorWindDTO firstPoint, PositionDTO secondPoint, boolean leftSide) {
        this.boatClassID = boatClassID;
        this.firstPoint = firstPoint;
        this.secondPoint = secondPoint;
        this.leftSide = leftSide;
    }
}

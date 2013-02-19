package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Request1TurnerDTO implements IsSerializable {

    public Integer boatClassID = 0;
    public PositionDTO firstPoint = null;
    public Long firstPointTimepoint = 0L;
    public PositionDTO secondPoint = null;
    public Boolean leftSide = false;

    public Request1TurnerDTO() {
        this.boatClassID = 0;
        this.firstPoint = null;
        this.firstPointTimepoint = 0L;
        this.secondPoint = null;
        this.leftSide = false;
    }

    public Request1TurnerDTO(int boatClassID, PositionDTO firstPoint, Long firstPointTimepoint, PositionDTO secondPoint, boolean leftSide) {
        this.boatClassID = boatClassID;
        this.firstPoint = firstPoint;
        this.firstPointTimepoint = firstPointTimepoint;
        this.secondPoint = secondPoint;
        this.leftSide = leftSide;
    }
}

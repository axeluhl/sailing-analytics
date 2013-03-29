package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Request1TurnerDTO implements IsSerializable {

    public SimulatorUISelectionDTO selection = null;
    public PositionDTO oldMovedPoint = null;
    public PositionDTO newMovedPoint = null;
    public PositionDTO beforeMovedPoint = null;
    public PositionDTO edgeStart = null;
    public PositionDTO edgeEnd = null;

    public Long oldMovedPointTimePoint = 0L;
    public Double startToEndBearingDegrees = 0.0;

    public Request1TurnerDTO() {
        this.selection = null;

        this.oldMovedPoint = null;
        this.newMovedPoint = null;
        this.beforeMovedPoint = null;
        this.edgeStart = null;
        this.edgeEnd = null;

        this.oldMovedPointTimePoint = 0L;
        this.startToEndBearingDegrees = 0.0;

    }

    public Request1TurnerDTO(SimulatorUISelectionDTO selection, PositionDTO oldMovedPoint, PositionDTO newMovedPoint, PositionDTO beforeMovedPoint,
            PositionDTO edgeStart, PositionDTO edgeEnd,
            Long oldMovedPointTimePoint, Double startToEndBearingDegrees) {

        this.selection = selection;

        this.oldMovedPoint = oldMovedPoint;
        this.newMovedPoint = newMovedPoint;
        this.beforeMovedPoint = beforeMovedPoint;
        this.edgeStart = edgeStart;
        this.edgeEnd = edgeEnd;

        this.oldMovedPointTimePoint = oldMovedPointTimePoint;
        this.startToEndBearingDegrees = startToEndBearingDegrees;
    }
}

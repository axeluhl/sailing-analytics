package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Request1TurnerDTO implements IsSerializable {

    public Integer selectedBoatClassIndex = 0;
    public Integer selectedRaceIndex = 0;
    public Integer selectedCompetitorIndex = 0;
    public Integer selectedLegIndex = 0;

    public PositionDTO oldMovedPoint = null;
    public PositionDTO newMovedPoint = null;
    public PositionDTO beforeMovedPoint = null;
    public PositionDTO edgeStart = null;
    public PositionDTO edgeEnd = null;

    public Long oldMovedPointTimePoint = 0L;
    public Double startToEndBearingDegrees = 0.0;

    public Request1TurnerDTO() {
        this.selectedBoatClassIndex = 0;
        this.selectedRaceIndex = 0;
        this.selectedCompetitorIndex = 0;
        this.selectedLegIndex = 0;

        this.oldMovedPoint = null;
        this.newMovedPoint = null;
        this.beforeMovedPoint = null;
        this.edgeStart = null;
        this.edgeEnd = null;

        this.oldMovedPointTimePoint = 0L;
        this.startToEndBearingDegrees = 0.0;

    }

    public Request1TurnerDTO(Integer selectedBoatClassIndex, Integer selectedRaceIndex, Integer selectedCompetitorIndex, Integer selectedLegIndex,
            PositionDTO oldMovedPoint, PositionDTO newMovedPoint, PositionDTO beforeMovedPoint, PositionDTO edgeStart, PositionDTO edgeEnd,
            Long oldMovedPointTimePoint, Double startToEndBearingDegrees) {

        this.selectedBoatClassIndex = selectedBoatClassIndex;
        this.selectedRaceIndex = selectedRaceIndex;
        this.selectedCompetitorIndex = selectedCompetitorIndex;
        this.selectedLegIndex = selectedLegIndex;

        this.oldMovedPoint = oldMovedPoint;
        this.newMovedPoint = newMovedPoint;
        this.beforeMovedPoint = beforeMovedPoint;
        this.edgeStart = edgeStart;
        this.edgeEnd = edgeEnd;

        this.oldMovedPointTimePoint = oldMovedPointTimePoint;
        this.startToEndBearingDegrees = startToEndBearingDegrees;
    }
}

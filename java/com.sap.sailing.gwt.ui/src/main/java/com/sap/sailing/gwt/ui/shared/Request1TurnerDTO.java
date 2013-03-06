package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Request1TurnerDTO implements IsSerializable {

    public Integer selectedBoatClassIndex = 0;
    public PositionDTO firstPoint = null;
    public Long firstPointTimepoint = 0L;
    public PositionDTO secondPoint = null;
    public Boolean leftSide = false;
    public int selectedRaceIndex;
    public int selectedLegIndex;
    public int selectedCompetitorIndex;

    public Request1TurnerDTO() {
        this.selectedBoatClassIndex = 0;
        this.firstPoint = null;
        this.firstPointTimepoint = 0L;
        this.secondPoint = null;
        this.leftSide = false;
    }

    public Request1TurnerDTO(int selectedBoatClassIndex, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex, PositionDTO firstPoint,
            Long firstPointTimepoint, PositionDTO secondPoint, boolean leftSide) {
        this.selectedBoatClassIndex = selectedBoatClassIndex;
        this.firstPoint = firstPoint;
        this.firstPointTimepoint = firstPointTimepoint;
        this.secondPoint = secondPoint;
        this.leftSide = leftSide;
        this.selectedRaceIndex = selectedRaceIndex;
        this.selectedCompetitorIndex = selectedCompetitorIndex;
        this.selectedLegIndex = selectedLegIndex;
    }
}

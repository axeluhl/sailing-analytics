package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Request1TurnerDTO implements IsSerializable {

    public Integer selectedBoatClassIndex = 0;
    public PositionDTO firstPoint = null;
    public Long firstPointTimepoint = 0L;
    public PositionDTO secondPoint = null;
    public Boolean leftSide = false;

    public int selectedRaceIndex = 0;
    public int selectedLegIndex = 0;
    public int selectedCompetitorIndex = 0;

    public PositionDTO edgeStart = null;
    public PositionDTO edgeEnd = null;

    public Request1TurnerDTO() {
        this.selectedBoatClassIndex = 0;
        this.firstPoint = null;
        this.firstPointTimepoint = 0L;
        this.secondPoint = null;
        this.leftSide = false;

        this.selectedRaceIndex = 0;
        this.selectedLegIndex = 0;
        this.selectedCompetitorIndex = 0;

        this.edgeStart = null;
        this.edgeEnd = null;
    }

    public Request1TurnerDTO(int selectedBoatClassIndex, int selectedRaceIndex, int selectedCompetitorIndex, int selectedLegIndex, PositionDTO firstPoint,
            Long firstPointTimepoint, PositionDTO secondPoint, boolean leftSide, PositionDTO edgeStart, PositionDTO edgeEnd) {
        this.selectedBoatClassIndex = selectedBoatClassIndex;
        this.firstPoint = firstPoint;
        this.firstPointTimepoint = firstPointTimepoint;
        this.secondPoint = secondPoint;
        this.leftSide = leftSide;
        this.selectedRaceIndex = selectedRaceIndex;
        this.selectedCompetitorIndex = selectedCompetitorIndex;
        this.selectedLegIndex = selectedLegIndex;

        this.selectedRaceIndex = selectedRaceIndex;
        this.selectedCompetitorIndex = selectedCompetitorIndex;
        this.selectedLegIndex = selectedLegIndex;

        this.edgeStart = edgeStart;
        this.edgeEnd = edgeEnd;
    }
}

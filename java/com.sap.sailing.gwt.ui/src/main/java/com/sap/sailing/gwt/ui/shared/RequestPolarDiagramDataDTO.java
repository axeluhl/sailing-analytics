package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RequestPolarDiagramDataDTO implements IsSerializable {

    private int boatClass;
    private List<PositionDTO> positions;

    public RequestPolarDiagramDataDTO() {

    }

    public RequestPolarDiagramDataDTO(final int boatClass, final List<PositionDTO> positions) {
        this.boatClass = boatClass;
        this.positions = positions;
    }

    public int getBoatClass() {
        return this.boatClass;
    }

    public void setBoatClass(final int boatClass) {
        this.boatClass = boatClass;
    }

    public List<PositionDTO> getPositions() {
        return this.positions;
    }

    public void setPositions(final List<PositionDTO> positions) {
        this.positions = positions;
    }
}

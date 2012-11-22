package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RequestPolarDiagramDataDTO implements IsSerializable {

    public int boatClass;
    public List<PositionDTO> positions;

    public RequestPolarDiagramDataDTO() {

    }

    public RequestPolarDiagramDataDTO(final int boatClass, final List<PositionDTO> positions) {
        this.boatClass = boatClass;
        this.positions = positions;
    }
}

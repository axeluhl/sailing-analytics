package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Response1TurnerDTO implements IsSerializable {

    public List<SimulatorWindDTO> path = null;
    public SimulatorWindDTO leftSide1Turner = null;
    public SimulatorWindDTO rightSide1Turner = null;
    public PositionDTO startPosition = null;
    public PositionDTO endPosition = null;
    public String notificationMessage = "";

    public Response1TurnerDTO() {
        this.path = null;
        this.leftSide1Turner = null;
        this.rightSide1Turner = null;
        this.startPosition = null;
        this.endPosition = null;
        this.notificationMessage = "";
    }

    public Response1TurnerDTO(List<SimulatorWindDTO> path, SimulatorWindDTO leftSide1Turner, SimulatorWindDTO rightSide1Turner, PositionDTO startPosition,
            PositionDTO endPosition, String notificationMessage) {
        this.path = path;
        this.leftSide1Turner = leftSide1Turner;
        this.rightSide1Turner = rightSide1Turner;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.notificationMessage = notificationMessage;
    }
}

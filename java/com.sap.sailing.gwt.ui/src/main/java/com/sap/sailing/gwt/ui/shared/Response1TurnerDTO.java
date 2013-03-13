package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Response1TurnerDTO implements IsSerializable {

    public SimulatorWindDTO intersection = null;
    public SimulatorWindDTO oneTurner = null;
    public String notificationMessage = "";

    public Response1TurnerDTO() {
        this.intersection = null;
        this.oneTurner = null;
        this.notificationMessage = "";
    }

    public Response1TurnerDTO(SimulatorWindDTO intersection, SimulatorWindDTO oneTurner, String notificationMessage) {
        this.intersection = intersection;
        this.oneTurner = oneTurner;
        this.notificationMessage = notificationMessage;
    }
}

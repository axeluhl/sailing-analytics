package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Response1TurnerDTO implements IsSerializable {

    public SimulatorWindDTO oneTurner = null;
    public String notificationMessage = "";

    public Response1TurnerDTO() {
        this.oneTurner = null;
        this.notificationMessage = "";
    }

    public Response1TurnerDTO(final SimulatorWindDTO oneTurner, final String notificationMessage) {
        this.oneTurner = oneTurner;
        this.notificationMessage = notificationMessage;
    }
}

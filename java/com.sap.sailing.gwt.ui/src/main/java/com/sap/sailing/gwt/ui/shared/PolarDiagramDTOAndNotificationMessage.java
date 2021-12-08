package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PolarDiagramDTOAndNotificationMessage implements IsSerializable {
    private PolarDiagramDTO polarDiagramDTO;
    private String notificationMessage;

    public PolarDiagramDTOAndNotificationMessage() {
        this.notificationMessage = "";
    }

    public PolarDiagramDTO getPolarDiagramDTO() {
        return this.polarDiagramDTO;
    }

    public void setPolarDiagramDTO(PolarDiagramDTO polarDiagramDTO) {
        this.polarDiagramDTO = polarDiagramDTO;
    }

    public String getNotificationMessage() {
        return this.notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}

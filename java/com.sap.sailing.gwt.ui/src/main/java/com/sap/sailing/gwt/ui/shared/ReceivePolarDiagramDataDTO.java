package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReceivePolarDiagramDataDTO implements IsSerializable {

    private List<SpeedWithBearingDTO> speeds;
    private String notificationMessage;

    public ReceivePolarDiagramDataDTO() {
        this.notificationMessage = "";
    }

    public ReceivePolarDiagramDataDTO(final List<SpeedWithBearingDTO> speeds, final String notificatonMessage) {
        this.speeds = speeds;
        this.notificationMessage = notificatonMessage;
    }

    public List<SpeedWithBearingDTO> getSpeeds() {
        return this.speeds;
    }

    public void setSpeeds(final List<SpeedWithBearingDTO> speeds) {
        this.speeds = speeds;
    }

    public String getNotificationMessage() {
        return this.notificationMessage;
    }

    public void setNotificationMessage(final String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}

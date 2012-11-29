package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReceivePolarDiagramDataDTO implements IsSerializable {

    private List<SpeedBearingPositionDTO> speedsBearingsPositions;
    private String notificationMessage;

    public ReceivePolarDiagramDataDTO() {
        this.notificationMessage = "";
    }

    public ReceivePolarDiagramDataDTO(final List<SpeedBearingPositionDTO> speedsBearingsPositions, final String notificatonMessage) {
        this.speedsBearingsPositions = speedsBearingsPositions;
        this.notificationMessage = notificatonMessage;
    }

    public List<SpeedBearingPositionDTO> getSpeedsBearingsPositions() {
        return this.speedsBearingsPositions;
    }

    public void setSpeedsBearingsPositions(final List<SpeedBearingPositionDTO> speedsBearingsPositions) {
        this.speedsBearingsPositions = speedsBearingsPositions;
    }

    public String getNotificationMessage() {
        return this.notificationMessage;
    }

    public void setNotificationMessage(final String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}

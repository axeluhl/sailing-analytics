package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.BoatClassDTO;

public class BoatClassDTOsAndNotificationMessage implements IsSerializable {
    private BoatClassDTO[] boatClassDTOs;
    private String notificationMessage;

    public BoatClassDTOsAndNotificationMessage() {
        this.notificationMessage = "";
    }

    public BoatClassDTO[] getBoatClassDTOs() {
        return this.boatClassDTOs;
    }

    public void setBoatClassDTOs(BoatClassDTO[] boatClassDTOs) {
        this.boatClassDTOs = boatClassDTOs;
    }

    public String getNotificationMessage() {
        return this.notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}

package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ResponseTotalTimeDTO implements IsSerializable {

    public Long totalTimeSeconds = 0L;
    public Double factorSim2GPS = 0.0;
    public String notificationMessage = "";

    public ResponseTotalTimeDTO() {
        this.totalTimeSeconds = 0L;
        this.factorSim2GPS = 0.0;
        this.notificationMessage = "";
    }

    public ResponseTotalTimeDTO(final Long totalTimeSeconds, final Double factorSim2GPS, final String notificationMessage) {
        this.totalTimeSeconds = totalTimeSeconds;
        this.factorSim2GPS = factorSim2GPS;
        this.notificationMessage = notificationMessage;
    }
}

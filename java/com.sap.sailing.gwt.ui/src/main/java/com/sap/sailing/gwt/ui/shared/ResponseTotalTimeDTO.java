package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ResponseTotalTimeDTO implements IsSerializable {

    public Long totalTimeSeconds = 0L;
    public String notificationMessage = "";

    public ResponseTotalTimeDTO() {
        this.totalTimeSeconds = 0L;
        this.notificationMessage = "";
    }

    public ResponseTotalTimeDTO(final Long totalTimeSeconds, final String notificationMessage) {
        this.totalTimeSeconds = totalTimeSeconds;
        this.notificationMessage = notificationMessage;
    }
}

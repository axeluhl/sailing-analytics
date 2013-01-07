package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.impl.Util.Quadruple;

public class ResponseTotalTimeDTO implements IsSerializable {

    public Long totalTimeSeconds = 0L;
    public String notificationMessage = "";
    public List<Quadruple<PositionDTO, PositionDTO, Double, Double>> segments = null;

    public ResponseTotalTimeDTO() {
        this.totalTimeSeconds = 0L;
        this.notificationMessage = "";
        this.segments = null;
    }

    public ResponseTotalTimeDTO(final Long totalTimeSeconds, final String notificationMessage,
            final List<Quadruple<PositionDTO, PositionDTO, Double, Double>> segments) {
        this.totalTimeSeconds = totalTimeSeconds;
        this.notificationMessage = notificationMessage;
        this.segments = segments;
    }
}

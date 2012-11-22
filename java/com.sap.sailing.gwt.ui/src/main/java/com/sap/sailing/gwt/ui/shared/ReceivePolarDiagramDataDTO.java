package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReceivePolarDiagramDataDTO implements IsSerializable {

    public List<SpeedWithBearingDTO> speeds;

    public ReceivePolarDiagramDataDTO() {

    }

    public ReceivePolarDiagramDataDTO(final List<SpeedWithBearingDTO> speeds) {
        this.speeds = speeds;
    }
}

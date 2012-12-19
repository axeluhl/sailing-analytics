package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RequestTotalTimeDTO implements IsSerializable {

    public int boatClassID = 0;
    public List<SimulatorWindDTO> allPoints = null;
    public List<PositionDTO> turnPoints = null;

    public RequestTotalTimeDTO() {
        this.boatClassID = 0;
        this.allPoints = new ArrayList<SimulatorWindDTO>();
        this.turnPoints = new ArrayList<PositionDTO>();
    }

    public RequestTotalTimeDTO(final int boatClassID, final List<SimulatorWindDTO> allPoints, final List<PositionDTO> turnPoints) {
        this.boatClassID = boatClassID;
        this.allPoints = allPoints;
        this.turnPoints = turnPoints;
    }
}

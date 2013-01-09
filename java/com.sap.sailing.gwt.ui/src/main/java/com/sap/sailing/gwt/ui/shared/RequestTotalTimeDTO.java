package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RequestTotalTimeDTO implements IsSerializable {

    public int boatClassID = 0;
    public List<SimulatorWindDTO> allPoints = null;
    public List<PositionDTO> turnPoints = null;
    public boolean useRealAverageWindSpeed = false;
    public int stepDurationMilliseconds = 0;
    public boolean debugMode = false;

    public RequestTotalTimeDTO() {
        this.boatClassID = 0;
        this.allPoints = new ArrayList<SimulatorWindDTO>();
        this.turnPoints = new ArrayList<PositionDTO>();
        this.useRealAverageWindSpeed = true;
        this.stepDurationMilliseconds = 2000;
        this.debugMode = false;
    }

    public RequestTotalTimeDTO(final int boatClassID, final List<SimulatorWindDTO> allPoints, final List<PositionDTO> turnPoints,
            final boolean useRealAverageWindSpeed, final int stepDurationMilliseconds, final boolean debugMode) {
        this.boatClassID = boatClassID;
        this.allPoints = allPoints;
        this.turnPoints = turnPoints;
        this.useRealAverageWindSpeed = useRealAverageWindSpeed;
        this.stepDurationMilliseconds = stepDurationMilliseconds;
        this.debugMode = debugMode;
    }
}

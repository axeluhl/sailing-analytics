package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Request1TurnerDTO implements IsSerializable {

    public Integer boatClassID = 0;
    public List<SimulatorWindDTO> allPoints = null;
    public SimulatorWindDTO firstPoint = null;
    public PositionDTO secondPoint = null;
    public Boolean useRealAverageWindSpeed = false;
    public Integer stepDurationMilliseconds = 0;
    public Boolean leftSide = false;

    public Request1TurnerDTO() {
        this.boatClassID = 0;
        this.allPoints = new ArrayList<SimulatorWindDTO>();
        this.firstPoint = null;
        this.secondPoint = null;
        this.useRealAverageWindSpeed = true;
        this.stepDurationMilliseconds = 2000;
        this.leftSide = false;
    }

    public Request1TurnerDTO(int boatClassID, List<SimulatorWindDTO> allPoints, SimulatorWindDTO firstPoint, PositionDTO secondPoint,
            boolean useRealAverageWindSpeed, int stepDurationMilliseconds, boolean leftSide) {
        this.boatClassID = boatClassID;
        this.allPoints = allPoints;
        this.firstPoint = firstPoint;
        this.secondPoint = secondPoint;
        this.useRealAverageWindSpeed = useRealAverageWindSpeed;
        this.stepDurationMilliseconds = stepDurationMilliseconds;
        this.leftSide = leftSide;
    }
}

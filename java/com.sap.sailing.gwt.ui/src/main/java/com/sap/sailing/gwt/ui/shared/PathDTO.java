package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.sap.sailing.domain.common.dto.NamedDTO;

public class PathDTO extends NamedDTO {
    private static final long serialVersionUID = 2381814072763342186L;
    private List<SimulatorWindDTO> points;
    private boolean algorithmTimedOut;
    private boolean mixedLeg;

    public PathDTO() {
    }

    public PathDTO(final String name) {
        super(name);
    }

    public List<SimulatorWindDTO> getPoints() {
        return points;
    }

    public void setPoints(final List<SimulatorWindDTO> matrix) {
        this.points = matrix;
    }

    public long getPathTime() {
        if (points == null || points.size() == 0) {
            return 0;
        }

        return points.get(points.size() - 1).timepoint - points.get(0).timepoint;
    }
    
    public boolean getAlgorithmTimedOut() {
        return this.algorithmTimedOut;
    }

    public void setAlgorithmTimedOut(boolean algorithmTimedOut) {
        this.algorithmTimedOut = algorithmTimedOut;
    }

    public boolean getMixedLeg() {
        return this.mixedLeg;
    }

    public void setMixedLeg(boolean mixedLeg) {
        this.mixedLeg = mixedLeg;
    }

}

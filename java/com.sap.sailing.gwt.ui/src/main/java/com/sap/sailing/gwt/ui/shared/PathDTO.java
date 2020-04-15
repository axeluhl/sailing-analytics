package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.sap.sailing.domain.common.PathType;
import com.sap.sse.security.shared.dto.NamedDTO;

public class PathDTO extends NamedDTO {
    private static final long serialVersionUID = 4926185583314563898L;
    private List<SimulatorWindDTO> points;
    private boolean algorithmTimedOut;
    private boolean mixedLeg;
    private PathType pathType;

    protected PathDTO() {
    }
    
    public PathDTO(PathType pathType) {
        super(pathType.getTxtId());
        this.pathType = pathType;
    }

    public PathDTO(final String name) {
        super(name);
        this.pathType = null;
    }
    
    public PathType getPathType() {
        return pathType;
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

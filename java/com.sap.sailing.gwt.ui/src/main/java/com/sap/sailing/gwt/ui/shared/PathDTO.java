package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PathDTO extends NamedDTO implements IsSerializable {

    private List<SimulatorWindDTO> points;

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

}

package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.ControlPointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class WaypointWithMarkConfigurationImpl implements WaypointWithMarkConfiguration {

    private final ControlPointWithMarkConfiguration controlPoint;
    private final PassingInstruction passingInstruction;

    public WaypointWithMarkConfigurationImpl(ControlPointWithMarkConfiguration controlPoint,
            PassingInstruction passingInstruction) {
        super();
        this.controlPoint = controlPoint;
        this.passingInstruction = passingInstruction;
    }

    @Override
    public ControlPointWithMarkConfiguration getControlPoint() {
        return controlPoint;
    }

    @Override
    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

}

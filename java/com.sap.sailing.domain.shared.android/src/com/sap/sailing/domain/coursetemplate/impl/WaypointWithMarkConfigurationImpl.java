package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.ControlPointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class WaypointWithMarkConfigurationImpl<P> implements WaypointWithMarkConfiguration<P> {
    private final ControlPointWithMarkConfiguration<P> controlPoint;
    private final PassingInstruction passingInstruction;

    public WaypointWithMarkConfigurationImpl(ControlPointWithMarkConfiguration<P> controlPoint,
            PassingInstruction passingInstruction) {
        super();
        this.controlPoint = controlPoint;
        this.passingInstruction = passingInstruction;
    }

    @Override
    public ControlPointWithMarkConfiguration<P> getControlPoint() {
        return controlPoint;
    }

    @Override
    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

}

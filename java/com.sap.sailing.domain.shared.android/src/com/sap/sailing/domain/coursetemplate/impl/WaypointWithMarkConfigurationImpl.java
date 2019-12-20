package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.ControlPointWithMarkConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.coursetemplate.WaypointWithMarkConfiguration;

public class WaypointWithMarkConfigurationImpl<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>>
        implements WaypointWithMarkConfiguration<MarkConfigurationT> {
    private final ControlPointWithMarkConfiguration<MarkConfigurationT> controlPoint;
    private final PassingInstruction passingInstruction;

    public WaypointWithMarkConfigurationImpl(ControlPointWithMarkConfiguration<MarkConfigurationT> controlPoint,
            PassingInstruction passingInstruction) {
        super();
        this.controlPoint = controlPoint;
        this.passingInstruction = passingInstruction;
    }

    @Override
    public ControlPointWithMarkConfiguration<MarkConfigurationT> getControlPoint() {
        return controlPoint;
    }

    @Override
    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

}

package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.PassingInstruction;

public interface WaypointWithMarkConfiguration<MarkConfigurationT extends MarkConfiguration<MarkConfigurationT>> {
    ControlPointWithMarkConfiguration<MarkConfigurationT> getControlPoint();
    PassingInstruction getPassingInstruction();
}

package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.PassingInstruction;

public interface WaypointWithMarkConfiguration<P> {
    ControlPointWithMarkConfiguration<P> getControlPoint();
    PassingInstruction getPassingInstruction();
}

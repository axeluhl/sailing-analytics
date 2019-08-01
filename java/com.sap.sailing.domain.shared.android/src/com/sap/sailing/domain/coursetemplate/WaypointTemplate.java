package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.PassingInstruction;

public interface WaypointTemplate {
    ControlPointTemplate getControlPoint();
    PassingInstruction getPassingInstruction();
}

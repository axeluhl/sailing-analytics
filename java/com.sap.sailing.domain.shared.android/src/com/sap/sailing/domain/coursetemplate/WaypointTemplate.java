package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.WithID;

public interface WaypointTemplate extends WithID {
    ControlPointTemplate getControlPoint();
    PassingInstruction getPassingInstruction();
}

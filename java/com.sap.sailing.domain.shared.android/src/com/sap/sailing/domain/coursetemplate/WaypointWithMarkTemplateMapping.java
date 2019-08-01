package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.WithID;

public interface WaypointWithMarkTemplateMapping extends WithID {
    ControlPointWithMarkConfiguration getControlPoint();
    PassingInstruction getPassingInstruction();
}

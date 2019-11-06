package com.sap.sailing.domain.coursetemplate;

import java.io.Serializable;

import com.sap.sailing.domain.common.PassingInstruction;

public interface WaypointTemplate extends Serializable {
    ControlPointTemplate getControlPointTemplate();
    PassingInstruction getPassingInstruction();
}

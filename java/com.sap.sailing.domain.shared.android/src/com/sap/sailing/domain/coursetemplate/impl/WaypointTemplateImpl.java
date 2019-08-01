package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;

public class WaypointTemplateImpl implements WaypointTemplate {
    private final PassingInstruction passingInstruction;
    private final ControlPointTemplate controlPoint;
    
    public WaypointTemplateImpl(ControlPointTemplate controlPoint, PassingInstruction passingInstruction) {
        this.passingInstruction = passingInstruction;
        this.controlPoint = controlPoint;
    }

    @Override
    public ControlPointTemplate getControlPoint() {
        return controlPoint;
    }

    @Override
    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

}

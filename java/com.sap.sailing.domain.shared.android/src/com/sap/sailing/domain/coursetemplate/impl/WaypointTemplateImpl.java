package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;

public class WaypointTemplateImpl implements WaypointTemplate {
    private static final long serialVersionUID = -4708322032365234982L;
    
    private final PassingInstruction passingInstruction;
    private final ControlPointTemplate controlPointTemplate;
    
    public WaypointTemplateImpl(ControlPointTemplate controlPoint, PassingInstruction passingInstruction) {
        this.passingInstruction = passingInstruction;
        this.controlPointTemplate = controlPoint;
    }

    @Override
    public ControlPointTemplate getControlPointTemplate() {
        return controlPointTemplate;
    }

    @Override
    public PassingInstruction getPassingInstruction() {
        return passingInstruction;
    }

    @Override
    public String toString() {
        return "WaypointTemplateImpl [passingInstruction=" + passingInstruction + ", controlPointTemplate="
                + controlPointTemplate + "]";
    }
}

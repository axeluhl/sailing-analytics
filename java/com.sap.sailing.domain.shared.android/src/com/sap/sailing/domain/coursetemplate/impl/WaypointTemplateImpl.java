package com.sap.sailing.domain.coursetemplate.impl;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;

public class WaypointTemplateImpl implements WaypointTemplate {
    private final PassingInstruction passingInstruction;
    private final ControlPointTemplate controlPoint;
    private final UUID id;
    
    public WaypointTemplateImpl(ControlPointTemplate controlPoint, PassingInstruction passingInstruction) {
        this(UUID.randomUUID(), controlPoint, passingInstruction);
    }
    
    protected WaypointTemplateImpl(UUID id, ControlPointTemplate controlPoint, PassingInstruction passingInstruction) {
        this.id = id;
        this.passingInstruction = passingInstruction;
        this.controlPoint = controlPoint;
    }

    @Override
    public Serializable getId() {
        return id;
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

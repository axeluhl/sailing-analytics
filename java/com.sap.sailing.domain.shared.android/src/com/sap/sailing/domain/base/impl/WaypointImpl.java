package com.sap.sailing.domain.base.impl;

import java.util.UUID;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sse.common.Bearing;

public class WaypointImpl implements Waypoint {
    private static final long serialVersionUID = 1600863368078653897L;
    private final ControlPoint controlPoint;
    private final UUID id;
    private final PassingInstruction passingInstructions; 
    private final Bearing fixedBearing;

    public WaypointImpl(ControlPoint controlPoint) {
        this(controlPoint, PassingInstruction.None, /*Bearing*/null);
    }
    
    public WaypointImpl(ControlPoint controlPoint, PassingInstruction passingInstructions) {
        this(controlPoint, passingInstructions, /*Bearing*/null);
    }
    
    public WaypointImpl(ControlPoint controlPoint, PassingInstruction passingInstructions, Bearing fixedBearing) {
        if (passingInstructions == null) {
            throw new IllegalArgumentException("PassingInstructions cannot be null");
        }
        this.controlPoint = controlPoint;
        this.passingInstructions = passingInstructions;
        this.fixedBearing = fixedBearing;
        id = UUID.randomUUID();
    }
    
    @Override
    public ControlPoint getControlPoint() {
        return controlPoint;
    }

    @Override
    public String getName() {
        return getControlPoint().getName();
    }

    @Override
    public String toString() {
        return (getName()==null?"":getName()) + " ("+getPassingInstructions()+")";
    }

    @Override
    public Iterable<Mark> getMarks() {
        return getControlPoint().getMarks();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Waypoint resolve(SharedDomainFactory domainFactory) {
        return domainFactory.getExistingWaypointByIdOrCache(this);
    }

    @Override
    public PassingInstruction getPassingInstructions() {
        return passingInstructions;
    }
    
    @Override
    public Bearing getFixedBearing(){
        return fixedBearing;
    }
    
}

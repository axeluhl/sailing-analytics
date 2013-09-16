package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NauticalSide;

public class WaypointImpl implements Waypoint {
    private static final long serialVersionUID = 1600863368078653897L;
    private final ControlPoint controlPoint;
    private static int idCounter = 1;
    private final int id;
    private final NauticalSide passingSide;    

    public WaypointImpl(ControlPoint controlPoint) {
        this(controlPoint, null);
    }
    
    public WaypointImpl(ControlPoint controlPoint, NauticalSide passingSide) {
        this.controlPoint = controlPoint;
        this.passingSide = passingSide;
        id = idCounter++;
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
        return getName();
    }

    @Override
    public Iterable<Mark> getMarks() {
        return getControlPoint().getMarks();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Waypoint resolve(SharedDomainFactory domainFactory) {
        return domainFactory.getExistingWaypointByIdOrCache(this);
    }

    @Override
    public NauticalSide getPassingSide() {
        return passingSide;
    }
    
}

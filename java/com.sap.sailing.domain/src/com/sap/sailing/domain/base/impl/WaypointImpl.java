package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.SingleMark;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Waypoint;

public class WaypointImpl implements Waypoint {
    private static final long serialVersionUID = 1600863368078653897L;
    private final ControlPoint controlPoint;
    private static int idCounter = 1;
    private final int id;
    
    public WaypointImpl(ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
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
    public Iterable<SingleMark> getMarks() {
        return getControlPoint().getMarks();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public Waypoint resolve(DomainFactory domainFactory) {
        return domainFactory.getExistingWaypointByIdOrCache(this);
    }
    
}

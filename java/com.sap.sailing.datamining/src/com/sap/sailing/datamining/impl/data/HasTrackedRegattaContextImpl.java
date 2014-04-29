package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasEventContext;
import com.sap.sailing.datamining.data.HasTrackedRegattaContext;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Regatta;

public class HasTrackedRegattaContextImpl extends HasEventContextImpl implements HasTrackedRegattaContext {

    private Regatta regatta;
    
    public HasTrackedRegattaContextImpl(HasEventContext eventContext, Regatta regatta) {
        this(eventContext.getEvent(), regatta);
    }

    public HasTrackedRegattaContextImpl(Event event, Regatta regatta) {
        super(event);
        this.regatta = regatta;
    }

    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public CourseArea getCourseArea() {
        return getRegatta().getDefaultCourseArea();
    }

    @Override
    public BoatClass getBoatClass() {
        return getRegatta().getBoatClass();
    }

}

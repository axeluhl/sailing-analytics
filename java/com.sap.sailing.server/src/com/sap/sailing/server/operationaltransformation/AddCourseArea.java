package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddCourseArea extends AbstractRacingEventServiceOperation<CourseArea> {

    private static final long serialVersionUID = -3650109848260363949L;
    private final String courseAreaName;
    private final Serializable courseAreaId;
    private final Serializable eventId;

    public AddCourseArea(Serializable eventId, String courseAreaName, Serializable courseAreaId) {
        super();
        this.eventId = eventId;
        this.courseAreaId = courseAreaId;
        this.courseAreaName = courseAreaName;
    }

    @Override
    public CourseArea internalApplyTo(RacingEventService toState) throws Exception {
        return toState.addCourseArea(eventId, courseAreaName, courseAreaId);
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(
            RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(
            RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}

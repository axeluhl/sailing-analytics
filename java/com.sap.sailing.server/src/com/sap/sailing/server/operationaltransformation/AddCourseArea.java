package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

/**
 * Adds a course area to an {@link Event}'s {@link Venue}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class AddCourseArea extends AbstractRacingEventServiceOperation<CourseArea> {

    private static final long serialVersionUID = -3650109848260363949L;
    private final String courseAreaName;
    private final UUID courseAreaId;
    private final UUID eventId;

    public AddCourseArea(UUID eventId, String courseAreaName, UUID courseAreaId) {
        super();
        this.eventId = eventId;
        this.courseAreaId = courseAreaId;
        this.courseAreaName = courseAreaName;
    }

    @Override
    public CourseArea internalApplyTo(RacingEventService toState) throws Exception {
        return toState.addCourseAreaWithoutReplication(eventId, courseAreaId, courseAreaName);
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(
            RacingEventServiceOperation<CourseArea> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(
            RacingEventServiceOperation<CourseArea> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}

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
public class AddCourseAreas extends AbstractRacingEventServiceOperation<CourseArea[]> {

    private static final long serialVersionUID = -3650109848260363949L;
    private final String[] courseAreaNames;
    private final UUID[] courseAreaIds;
    private final UUID eventId;

    public AddCourseAreas(UUID eventId, String[] courseAreaNames, UUID[] courseAreaIds) {
        super();
        this.eventId = eventId;
        this.courseAreaIds = courseAreaIds;
        this.courseAreaNames = courseAreaNames;
    }

    @Override
    public CourseArea[] internalApplyTo(RacingEventService toState) throws Exception {
        return toState.addCourseAreasWithoutReplication(eventId, courseAreaIds, courseAreaNames);
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

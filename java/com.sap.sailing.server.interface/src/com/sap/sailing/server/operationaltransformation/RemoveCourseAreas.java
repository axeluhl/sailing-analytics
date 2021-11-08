package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

/**
 * Removes a course area from an {@link Event}'s {@link Venue}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RemoveCourseAreas extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -3650109848260363949L;
    private final UUID[] courseAreaIds;
    private final UUID eventId;

    public RemoveCourseAreas(UUID eventId, UUID[] courseAreaIds) {
        super();
        this.eventId = eventId;
        this.courseAreaIds = courseAreaIds;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.removeCourseAreaWithoutReplication(eventId, courseAreaIds);
        return null;
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

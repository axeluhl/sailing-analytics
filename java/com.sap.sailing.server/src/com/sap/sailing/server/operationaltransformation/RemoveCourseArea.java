package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

/**
 * Removes a course area from an {@link Event}'s {@link Venue}.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RemoveCourseArea extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -3650109848260363949L;
    private final UUID courseAreaId;
    private final UUID eventId;

    public RemoveCourseArea(UUID eventId, UUID courseAreaId) {
        super();
        this.eventId = eventId;
        this.courseAreaId = courseAreaId;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.removeCourseAreaWithoutReplication(eventId, courseAreaId);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(
            RacingEventServiceOperation<Void> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(
            RacingEventServiceOperation<Void> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}

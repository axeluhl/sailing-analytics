package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

/**
 * Creates an {@link Event} in the server, with a new venue and an empty course area list.
 * See the {@link AddCourseArea} operation for adding course areas to the event's venue.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CreateEvent extends AbstractEventOperation<Event> {
    private static final long serialVersionUID = 308389324918359960L;
    private final String venue;
    private final String publicationUrl;
    private final boolean isPublic;
    private final String eventName;
    
    public CreateEvent(String eventName, String venue, String publicationUrl, boolean isPublic, UUID id) {
        super(id);
        this.eventName = eventName;
        this.venue = venue;
        this.publicationUrl = publicationUrl;
        this.isPublic = isPublic;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    protected String getEventName() {
        return eventName;
    }

    @Override
    public Event internalApplyTo(RacingEventService toState) {
        return toState.createEventWithoutReplication(getEventName(), venue, publicationUrl, isPublic, getId());
    }

}

package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class CreateEvent extends AbstractEventOperation<Event> {
    private static final long serialVersionUID = 308389324918359960L;
    private final String venue;
    private final String publicationUrl;
    private final boolean isPublic;
    private final List<String> courseAreaNames;
    private final String eventName;
    
    public CreateEvent(String eventName, String venue, String publicationUrl, boolean isPublic, Serializable id,  List<String> courseAreaNames) {
        super(id);
        this.eventName = eventName;
        this.venue = venue;
        this.courseAreaNames = courseAreaNames;
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
        return toState.addEvent(getEventName(), venue, publicationUrl, isPublic, getId(), courseAreaNames);
    }

}

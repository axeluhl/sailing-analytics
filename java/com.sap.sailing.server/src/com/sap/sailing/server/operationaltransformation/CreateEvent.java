package com.sap.sailing.server.operationaltransformation;

import java.util.List;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class CreateEvent extends AbstractEventOperation<Event> {
    private static final long serialVersionUID = 308389324918359960L;
    private final String venue;
    private final List<String> regattaNames;

    public CreateEvent(String eventName, String venue, List<String> regattaNames) {
        super(eventName);
        this.venue = venue;
        this.regattaNames = regattaNames;
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

    @Override
    public Event internalApplyTo(RacingEventService toState) {
        return toState.addEvent(getEventName(), venue, regattaNames);
    }

}

package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RemoveEvent extends AbstractEventOperation<Void> {
    private static final long serialVersionUID = 8242646632399136789L;

    public RemoveEvent(String eventName) {
        super(eventName);
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
    public Void internalApplyTo(RacingEventService toState) {
        toState.removeEvent(getEventName());
        return null;
    }
}

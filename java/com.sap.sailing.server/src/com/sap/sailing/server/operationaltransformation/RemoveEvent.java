package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RemoveEvent extends AbstractEventOperation<Void> {
    private static final long serialVersionUID = 8242646632399136789L;

    public RemoveEvent(UUID id) {
        super(id);
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<Void> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<Void> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.removeEvent(getId());
        return null;
    }
}

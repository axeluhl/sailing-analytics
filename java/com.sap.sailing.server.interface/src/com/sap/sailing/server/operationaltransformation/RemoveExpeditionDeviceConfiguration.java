package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class RemoveExpeditionDeviceConfiguration extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = 2781190033335859683L;
    private final UUID deviceUuid;
    
    public RemoveExpeditionDeviceConfiguration(UUID deviceUuid) {
        super();
        this.deviceUuid = deviceUuid;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.removeExpeditionDeviceConfiguration(deviceUuid);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }
}

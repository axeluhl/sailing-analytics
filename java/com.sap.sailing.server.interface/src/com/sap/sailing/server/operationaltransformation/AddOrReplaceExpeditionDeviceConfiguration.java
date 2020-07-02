package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class AddOrReplaceExpeditionDeviceConfiguration extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = 2781190033335859683L;
    private final UUID deviceUuid;
    private final String name;
    private final Integer expeditionBoatId;
    
    public AddOrReplaceExpeditionDeviceConfiguration(UUID deviceUuid, String name, Integer expeditionBoatId) {
        super();
        this.deviceUuid = deviceUuid;
        this.name = name;
        this.expeditionBoatId = expeditionBoatId;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.addOrReplaceExpeditionDeviceConfiguration(deviceUuid, name, expeditionBoatId);
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

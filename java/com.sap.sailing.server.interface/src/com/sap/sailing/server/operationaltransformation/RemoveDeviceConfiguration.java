package com.sap.sailing.server.operationaltransformation;

import java.util.UUID;

import com.sap.sailing.server.interfaces.RacingEventService;

public class RemoveDeviceConfiguration extends AbstractDeviceConfigurationOperation {

    private static final long serialVersionUID = 4558840313811769137L;

    public RemoveDeviceConfiguration(UUID id) {
        super(id);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.removeDeviceConfiguration(id);
        return null;
    }
}

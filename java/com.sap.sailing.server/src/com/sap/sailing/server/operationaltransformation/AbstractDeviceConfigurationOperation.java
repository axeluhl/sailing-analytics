package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.server.RacingEventServiceOperation;

public abstract class AbstractDeviceConfigurationOperation extends AbstractRacingEventServiceOperation<Void> {
    
    private static final long serialVersionUID = -4918619894046424881L;
    
    protected DeviceConfigurationMatcher matcher;

    public AbstractDeviceConfigurationOperation(DeviceConfigurationMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<Void> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<Void> clientOp) {
        return null;
    }


}

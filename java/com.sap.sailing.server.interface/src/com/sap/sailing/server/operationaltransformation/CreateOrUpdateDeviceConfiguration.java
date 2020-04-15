package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.server.interfaces.RacingEventService;

public class CreateOrUpdateDeviceConfiguration extends AbstractDeviceConfigurationOperation {

    private static final long serialVersionUID = -3859272800957110301L;
    
    private DeviceConfiguration configuration;
    
    public CreateOrUpdateDeviceConfiguration(DeviceConfiguration configuration) {
        super(configuration.getId());
        this.configuration = configuration;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.createOrUpdateDeviceConfiguration(configuration);
        return null;
    }

}

package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.server.interfaces.RacingEventService;

public class CreateOrUpdateDeviceConfiguration extends AbstractDeviceConfigurationOperation {

    private static final long serialVersionUID = -3859272800957110301L;
    
    private DeviceConfiguration configuration;
    
    public CreateOrUpdateDeviceConfiguration(DeviceConfigurationMatcher matcher, DeviceConfiguration configuration) {
        super(matcher);
        this.configuration = configuration;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.createOrUpdateDeviceConfiguration(matcher, configuration);
        return null;
    }

}

package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.server.interfaces.RacingEventService;

public class CreateOrUpdateDeviceConfiguration extends AbstractDeviceConfigurationOperation {

    private static final long serialVersionUID = -3859272800957110301L;
    
    private DeviceConfiguration configuration;
    
    /**
     * Clones the {@code configuration} to make sure that if multiple occurrences of the same configuration
     * are to be serialized into the same stream all the different states of the configuration are serialized
     * properly, too. See also bug 5741.
     */
    public CreateOrUpdateDeviceConfiguration(DeviceConfiguration configuration) {
        super(configuration.getId());
        this.configuration = new DeviceConfigurationImpl(configuration);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.createOrUpdateDeviceConfiguration(configuration);
        return null;
    }

}

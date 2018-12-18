package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.server.interfaces.RacingEventService;

public class RemoveDeviceConfiguration extends AbstractDeviceConfigurationOperation {

    private static final long serialVersionUID = 4558840313811769137L;

    public RemoveDeviceConfiguration(DeviceConfigurationMatcher matcher) {
        super(matcher);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.removeDeviceConfiguration(matcher);
        return null;
    }


}

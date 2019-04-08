package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public abstract class AbstractDeviceConfigurationOperation extends AbstractRacingEventServiceOperation<Void> {
    
    private static final long serialVersionUID = -4918619894046424881L;
    
    protected DeviceConfigurationMatcher matcher;

    public AbstractDeviceConfigurationOperation(DeviceConfigurationMatcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Operations of this type are considered to replicate their effects transitively during the execution
     * of their {@link #internalApplyTo(com.sap.sailing.server.RacingEventService)} operation and therefore
     * return <code>false</code> from this method.
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
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

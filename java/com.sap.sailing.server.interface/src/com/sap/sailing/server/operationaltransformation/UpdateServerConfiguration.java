package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.SailingServerConfiguration;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class UpdateServerConfiguration extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -778186188996039972L;
    private final SailingServerConfiguration serverConfiguration;
    
    public UpdateServerConfiguration(SailingServerConfiguration serverConfiguration) {
        super();
        this.serverConfiguration = serverConfiguration;
    }

    /**
     * {@link #internalApplyTo(RacingEventService)} already replicates the effects
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        toState.updateServerConfiguration(serverConfiguration);
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

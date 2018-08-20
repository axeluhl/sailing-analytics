package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class TrackRegatta extends AbstractRacingEventServiceOperation<DynamicTrackedRegatta> {
    private static final long serialVersionUID = 7891960455598410633L;
    private final RegattaIdentifier regattaIdentifier;
    
    public TrackRegatta(RegattaIdentifier regattaIdentifier) {
        super();
        this.regattaIdentifier = regattaIdentifier;
    }
    
    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@link #internalApplyTo(RacingEventService)} already replicates the effects
     */
    @Override
    public boolean isRequiresExplicitTransitiveReplication() {
        return false;
    }
    
    @Override
    public DynamicTrackedRegatta internalApplyTo(RacingEventService toState) {
        Regatta regatta = toState.getRegatta(regattaIdentifier);
        return toState.getOrCreateTrackedRegatta(regatta);
    }

}

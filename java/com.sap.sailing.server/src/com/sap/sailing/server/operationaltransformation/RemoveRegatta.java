package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RemoveRegatta extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -2232723085937305299L;
    private static final Logger logger = Logger.getLogger(RemoveRegatta.class.getName());
    private final RegattaIdentifier regattaIdentifier;
    
    public RemoveRegatta(RegattaIdentifier regattaIdentifier) {
        super();
        this.regattaIdentifier = regattaIdentifier;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        Regatta regatta = toState.getRegatta(regattaIdentifier);
        if (regatta != null) {
            toState.removeRegatta(regatta);
        } else {
            logger.warning("Couldn't find regatta "+regattaIdentifier);
        }
        return null;
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

}

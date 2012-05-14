package com.sap.sailing.server.operationaltransformation;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class StopTrackingRegatta extends AbstractRacingEventServiceOperation<Void> {
    private static final long serialVersionUID = -651066062098923320L;
    private final RegattaIdentifier regattaIdentifier;

    public StopTrackingRegatta(RegattaIdentifier regattaIdentifier) {
        super();
        this.regattaIdentifier = regattaIdentifier;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws MalformedURLException, IOException, InterruptedException {
        Regatta regatta = toState.getRegatta(regattaIdentifier);
        if (regatta != null) {
            toState.stopTracking(regatta);
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

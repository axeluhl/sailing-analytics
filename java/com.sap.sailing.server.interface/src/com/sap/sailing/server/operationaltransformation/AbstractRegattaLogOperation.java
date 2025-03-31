package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public abstract class AbstractRegattaLogOperation<T> extends AbstractRacingEventServiceOperation<T> {
    private static final long serialVersionUID = 2748436647846774234L;
    protected final String regattaLikeParentName;
    
    public AbstractRegattaLogOperation(String regattaLikeParentName) {
        this.regattaLikeParentName = regattaLikeParentName;
    }
    
    static final class CouldNotResolveRegattaLogException extends Exception {
        private static final long serialVersionUID = -634066450023913105L;
    }

    protected abstract RegattaLog getRegattaLog(RacingEventService toState) throws CouldNotResolveRegattaLogException;
   
    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return null;
    }
}
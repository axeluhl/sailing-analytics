package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.server.interfaces.RacingEventService;

public abstract class AbstractRegattaLogOnRegattaOperation<T> extends AbstractRegattaLogOperation<T> {
    private static final long serialVersionUID = -1811293351556698801L;
    
    public AbstractRegattaLogOnRegattaOperation(String regattaName) {
        super(regattaName);
    }
    
    @Override
    protected RegattaLog getRegattaLog(RacingEventService toState) {
        return toState.getRegattaByName(regattaLikeParentName).getRegattaLog();
    }
}

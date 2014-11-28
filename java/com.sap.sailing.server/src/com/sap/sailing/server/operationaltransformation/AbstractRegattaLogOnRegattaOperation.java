package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.server.RacingEventService;

public abstract class AbstractRegattaLogOnRegattaOperation<T> extends AbstractRegattaLogOperation<T> {
    private static final long serialVersionUID = -1811293351556698801L;
    private final String regattaName;
    
    public AbstractRegattaLogOnRegattaOperation(String regattaName) {
        super();
        this.regattaName = regattaName;
    }

    protected String getRegattaName() {
        return regattaName;
    }
    protected Regatta getRegatta(RacingEventService toState) {
        return toState.getRegattaByName(regattaName);
    }
}

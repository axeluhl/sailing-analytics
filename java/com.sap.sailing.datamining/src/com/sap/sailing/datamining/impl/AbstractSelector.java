package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.shared.Selector;
import com.sap.sailing.server.RacingEventService;

public abstract class AbstractSelector implements Selector {
    private static final long serialVersionUID = -1867045534315618704L;

    protected abstract void initializeSelection(RacingEventService racingEventService);

}

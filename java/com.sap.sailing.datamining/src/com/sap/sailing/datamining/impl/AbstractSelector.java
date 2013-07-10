package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.shared.Selector;
import com.sap.sailing.server.RacingEventService;

public abstract class AbstractSelector implements Selector {

    protected abstract void initializeSelection(RacingEventService racingEventService);

}

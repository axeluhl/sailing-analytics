package com.sap.sailing.gwt.ui.server.dispatch;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.server.RacingEventService;

public abstract class AbstractSailingHandler<R extends Result, A extends Action<R>> extends AbstractHandler<R, A> {
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;

    public AbstractSailingHandler(ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker) {
        this.racingEventServiceTracker = racingEventServiceTracker;
    }
    
    public RacingEventService getService() {
        return racingEventServiceTracker.getService();
    }
}

package com.sap.sailing.gwt.ui.server.dispatch.handlers;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.gwt.ui.server.dispatch.AbstractSailingHandler;
import com.sap.sailing.gwt.ui.server.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.OtherAction;
import com.sap.sailing.gwt.ui.shared.dispatch.event.OtherDTO;
import com.sap.sailing.server.RacingEventService;

public class OtherActionHandler extends AbstractSailingHandler<ResultWithTTL<OtherDTO>, OtherAction> {

    public OtherActionHandler(ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker) {
        super(racingEventServiceTracker);
    }

    @Override
    public ResultWithTTL<OtherDTO> execute(OtherAction action, DispatchContext context) {
        return new ResultWithTTL<>(3000, new OtherDTO());
    }

}

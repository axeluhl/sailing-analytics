package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.net.MalformedURLException;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.shared.dispatch.IsClientCacheable;
import com.sap.sailing.gwt.server.EventListDataCalculator;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.eventlist.EventListViewDTO;

public class GetEventListViewAction implements Action<EventListViewDTO>, IsClientCacheable {
    @Override
    @GwtIncompatible
    public EventListViewDTO execute(DispatchContext context) {
        try {
            EventListDataCalculator eventListDataCalculator = new EventListDataCalculator(
                    context.getRacingEventService());
            HomeServiceUtil.forAllPublicEvents(context.getRacingEventService(), context.getRequest(),
                    eventListDataCalculator);
            return eventListDataCalculator.getResult();

        } catch (MalformedURLException e) {
            throw new DispatchException("Could not load event list");
        }
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
    }
}

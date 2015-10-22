package com.sap.sailing.gwt.home.communication.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.server.EventListDataCalculator;
import com.sap.sailing.gwt.server.HomeServiceUtil;

public class GetEventListViewAction implements SailingAction<EventListViewDTO>, IsClientCacheable {
    @Override
    @GwtIncompatible
    public EventListViewDTO execute(SailingDispatchContext context) {
        EventListDataCalculator eventListDataCalculator = new EventListDataCalculator(context.getRacingEventService());
        HomeServiceUtil.forAllPublicEvents(context.getRacingEventService(), context.getRequest(),
                eventListDataCalculator);
        return eventListDataCalculator.getResult();
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
    }
}

package com.sap.sailing.gwt.home.communication.event;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.eventlist.EventListViewDTO;
import com.sap.sailing.gwt.server.EventListDataCalculator;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in the events list overview, using a
 * {@link EventListDataCalculator} to prepare the appropriate data structure.
 * </p>
 */
public class GetEventListViewAction implements SailingAction<EventListViewDTO>, IsClientCacheable {
    @Override
    @GwtIncompatible
    public EventListViewDTO execute(SailingDispatchContext context) {
        EventListDataCalculator eventListDataCalculator = new EventListDataCalculator(context.getRacingEventService());
        HomeServiceUtil.forAllPublicEventsWithReadPermission(context.getRacingEventService(), context.getRequest(),
                context.getSecurityService(),
                eventListDataCalculator);
        return eventListDataCalculator.getResult();
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        // The key is empty because this action is global and does not need any key
    }
}

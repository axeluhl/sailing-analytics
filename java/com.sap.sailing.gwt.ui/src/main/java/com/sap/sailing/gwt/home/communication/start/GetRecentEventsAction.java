package com.sap.sailing.gwt.home.communication.start;

import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.gwt.dispatch.client.ListResult;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.server.HomeServiceUtil.EventVisitor;

public class GetRecentEventsAction implements SailingAction<ListResult<EventQuickfinderDTO>>, IsClientCacheable {
    
    private int limit;

    protected GetRecentEventsAction() {
    }
    
    public GetRecentEventsAction(int limit) {
        this.limit = limit;
    }
    
    @GwtIncompatible
    public ListResult<EventQuickfinderDTO> execute(final SailingDispatchContext context) throws DispatchException {
        final SortedSet<EventQuickfinderDTO> events = new TreeSet<>();
        HomeServiceUtil.forAllPublicEvents(context.getRacingEventService(), context.getRequest(), new EventVisitor() {
            @Override
            public void visit(EventBase event, boolean onRemoteServer, URL baseURL) {
                EventQuickfinderDTO dto = new EventQuickfinderDTO();
                dto.setId((UUID) event.getId());
                dto.setDisplayName(HomeServiceUtil.getEventDisplayName(event, context.getRacingEventService()));
                dto.setOnRemoteServer(onRemoteServer);
                dto.setBaseURL(baseURL == null ? null : baseURL.toString());
                dto.setStartTimePoint(event.getStartDate());
                dto.setState(HomeServiceUtil.calculateEventState(event));
                events.add(dto);
            }
        });
        ListResult<EventQuickfinderDTO> result = new ListResult<>();
        int count = 0;
        for (EventQuickfinderDTO event : events) {
            result.addValue(event);
            count++;
            if(limit > 0 && count >= limit) {
                break;
            }
        }
        return result;
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(limit);
    }
}

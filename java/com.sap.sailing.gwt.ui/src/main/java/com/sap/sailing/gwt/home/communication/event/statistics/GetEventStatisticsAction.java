package com.sap.sailing.gwt.home.communication.event.statistics;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.StatisticsCalculator;

public class GetEventStatisticsAction implements SailingAction<ResultWithTTL<EventStatisticsDTO>>, IsClientCacheable {
    
    private UUID eventId;

    protected GetEventStatisticsAction() {
    }

    public GetEventStatisticsAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(SailingDispatchContext context) throws DispatchException {
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
        EventActionUtil.forLeaderboardsOfEvent(context, eventId, statisticsCalculator);
        return statisticsCalculator.getResult();
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }
}

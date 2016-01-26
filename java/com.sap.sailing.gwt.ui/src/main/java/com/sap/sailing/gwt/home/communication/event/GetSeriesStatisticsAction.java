package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.StatisticsCalculator;
import com.sap.sse.gwt.dispatch.client.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sse.gwt.dispatch.client.system.caching.IsClientCacheable;

public class GetSeriesStatisticsAction implements SailingAction<ResultWithTTL<EventStatisticsDTO>>, IsClientCacheable {
    
    private UUID seriesId;
    
    protected GetSeriesStatisticsAction() {
    }

    public GetSeriesStatisticsAction(UUID seriesId) {
        this.seriesId = seriesId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(SailingDispatchContext context) throws DispatchException {
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
        EventActionUtil.forLeaderboardsOfEvent(context, seriesId, statisticsCalculator);
        return statisticsCalculator.getResult();
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(seriesId);
    }
}

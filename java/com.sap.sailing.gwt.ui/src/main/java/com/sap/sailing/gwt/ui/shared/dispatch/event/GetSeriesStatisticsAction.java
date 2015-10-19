package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.shared.dispatch.IsClientCacheable;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

public class GetSeriesStatisticsAction implements Action<ResultWithTTL<EventStatisticsDTO>>, IsClientCacheable {
    
    private UUID seriesId;
    
    protected GetSeriesStatisticsAction() {
    }

    public GetSeriesStatisticsAction(UUID seriesId) {
        this.seriesId = seriesId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(DispatchContext context) throws Exception {
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator();
        EventActionUtil.forLeaderboardsOfEvent(context, seriesId, statisticsCalculator);
        return statisticsCalculator.getResult();
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(seriesId);
    }
}

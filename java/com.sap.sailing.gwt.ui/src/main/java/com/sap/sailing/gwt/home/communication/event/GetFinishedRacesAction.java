package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.SortedSetResult;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.RaceListDataCalculator;
import com.sap.sse.common.Duration;

public class GetFinishedRacesAction implements SailingAction<ResultWithTTL<SortedSetResult<RaceListRaceDTO>>>,
        IsClientCacheable {
    
    private UUID eventId;
    private String regattaId;
    
    @SuppressWarnings("unused")
    private GetFinishedRacesAction() {
    }

    public GetFinishedRacesAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
        this.regattaId = regattaId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<SortedSetResult<RaceListRaceDTO>> execute(SailingDispatchContext context) {
        RaceListDataCalculator raceListDataCalculator = new RaceListDataCalculator();
        EventActionUtil.forRacesOfRegatta(context, eventId, regattaId, raceListDataCalculator);
        
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId,
                Duration.ONE_MINUTE.times(5)), new SortedSetResult<>(raceListDataCalculator.getResult()));
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId).append("_").append(regattaId);
    }
}

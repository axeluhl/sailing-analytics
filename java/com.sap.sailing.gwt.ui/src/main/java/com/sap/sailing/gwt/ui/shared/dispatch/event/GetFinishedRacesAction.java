package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.shared.dispatch.IsClientCacheable;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;
import com.sap.sse.common.Duration;

public class GetFinishedRacesAction implements Action<ResultWithTTL<SortedSetResult<RaceListRaceDTO>>>,
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
    public ResultWithTTL<SortedSetResult<RaceListRaceDTO>> execute(DispatchContext context) {
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

package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.SortedSetResult;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.LiveRaceCalculator;
import com.sap.sailing.gwt.home.server.EventActionUtil.CalculationWithEvent;

public class GetLiveRacesForRegattaAction implements SailingAction<ResultWithTTL<SortedSetResult<LiveRaceDTO>>>,
        IsClientCacheable {
    private UUID eventId;
    private String regattaName;
    
    @SuppressWarnings("unused")
    private GetLiveRacesForRegattaAction() {
    }

    public GetLiveRacesForRegattaAction(UUID eventId, String regattaName) {
        this.eventId = eventId;
        this.regattaName = regattaName;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<SortedSetResult<LiveRaceDTO>> execute(final SailingDispatchContext context) {
        return EventActionUtil.withLiveRaceOrDefaultSchedule(context, eventId, new CalculationWithEvent<SortedSetResult<LiveRaceDTO>>() {
            @Override
            public ResultWithTTL<SortedSetResult<LiveRaceDTO>> calculateWithEvent(Event event) {
                LiveRaceCalculator liveRaceCalculator = new LiveRaceCalculator();
                EventActionUtil.forRacesOfRegatta(context, eventId, regattaName, liveRaceCalculator);
                return liveRaceCalculator.getResult();
            }
        });
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId).append("_").append(regattaName);
    }
}

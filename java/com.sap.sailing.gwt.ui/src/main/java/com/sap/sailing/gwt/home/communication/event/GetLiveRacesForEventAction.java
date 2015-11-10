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

public class GetLiveRacesForEventAction implements SailingAction<ResultWithTTL<SortedSetResult<LiveRaceDTO>>>,
        IsClientCacheable {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetLiveRacesForEventAction() {
    }

    public GetLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<SortedSetResult<LiveRaceDTO>> execute(final SailingDispatchContext context) {
        return EventActionUtil.withLiveRaceOrDefaultSchedule(context, eventId, new CalculationWithEvent<SortedSetResult<LiveRaceDTO>>() {
            @Override
            public ResultWithTTL<SortedSetResult<LiveRaceDTO>> calculateWithEvent(Event event) {
                LiveRaceCalculator liveRaceCalculator = new LiveRaceCalculator();
                EventActionUtil.forRacesOfEvent(context, eventId, liveRaceCalculator);
                return liveRaceCalculator.getResult();
            }
        });
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }
}

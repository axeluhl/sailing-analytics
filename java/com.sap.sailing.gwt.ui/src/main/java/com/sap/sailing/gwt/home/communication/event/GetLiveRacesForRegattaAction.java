package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.EventActionUtil.CalculationWithEvent;
import com.sap.sailing.gwt.home.server.LiveRaceCalculator;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in the live races section for the
 * {@link #GetLiveRacesForRegattaAction(UUID, String) given event-id and regatta name}, using a
 * {@link LiveRaceCalculator} to prepare the appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is calculated using the {@link LiveRaceCalculator}'s internal
 * {@link com.sap.sailing.gwt.home.server.RaceRefreshCalculator}, which is called for every race in the regatta.
 * </p>
 * <p>
 * NOTE: Because there can only be live races in currently running events, this action returns an empty
 * {@link ResultWithTTL result} with a {@link EventActionUtil#calculateTtlForNonLiveEvent(Event, EventState)
 * state-dependent} time to live, if the {@link Event} for the given id is not currently running.
 * </p>
 */
public class GetLiveRacesForRegattaAction implements SailingAction<ResultWithTTL<SortedSetResult<LiveRaceDTO>>>,
        IsClientCacheable, ProvidesLeaderboardRouting {
    private UUID eventId;
    private String regattaName;
    
    @SuppressWarnings("unused")
    private GetLiveRacesForRegattaAction() {
    }
    
    /**
     * Creates a {@link GetLiveRacesForRegattaAction} instance for the given event-id and regatta name.
     * 
     * @param eventId {@link UUID} of the event to load live races for
     * @param regattaName {@link String name} of the regatta to load live races for
     */
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

    @Override
    public String getLeaderboardName() {
        return regattaName;
    }

}

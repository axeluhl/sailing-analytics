package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
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
 * {@link #GetLiveRacesForEventAction(UUID) given event-id}, using a {@link LiveRaceCalculator} to prepare the
 * appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is calculated using the {@link LiveRaceCalculator}'s internal
 *  {@link com.sap.sailing.gwt.home.server.RaceRefreshCalculator}, which is called for every race in the event.
 * </p>
 * <p>
 * NOTE: Because there can only be live races in currently running events, this action returns an empty
 * {@link ResultWithTTL result} with a {@link EventActionUtil#calculateTtlForNonLiveEvent(Event, EventState)
 * state-dependent} time to live, if the {@link Event} for the given id is not currently running.
 * </p>
 */
public class GetLiveRacesForEventAction implements SailingAction<ResultWithTTL<SortedSetResult<LiveRaceDTO>>>,
        IsClientCacheable {
    private UUID eventId;
    
    @SuppressWarnings("unused")
    private GetLiveRacesForEventAction() {
    }

    /**
     * Creates a {@link GetLiveRacesForEventAction} instance for the given event-id.
     * 
     * @param eventId
     *            {@link UUID} of the event to load live races for
     */
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

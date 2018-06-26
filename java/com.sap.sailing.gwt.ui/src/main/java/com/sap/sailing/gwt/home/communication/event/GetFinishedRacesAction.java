package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.RaceListDataCalculator;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ListResult;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in the finished races section for the
 * {@link #GetFinishedRacesAction(UUID, String) given event- and regatta-id}, using a {@link RaceListDataCalculator} to
 * prepare the appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live
 * {@link EventActionUtil#getEventStateDependentTTL(SailingDispatchContext, UUID, Duration) depends on the event's
 * state} using a duration of <i>5 minutes</i> for currently running events.
 * </p>
 */
public class GetFinishedRacesAction implements SailingAction<ResultWithTTL<ListResult<RaceListRaceDTO>>>,
        IsClientCacheable, ProvidesLeaderboardRouting {
    
    private UUID eventId;
    private String regattaId;
    
    @SuppressWarnings("unused")
    private GetFinishedRacesAction() {
    }

    /**
     * Creates a {@link GetCompetitionFormatRacesAction} instance for the given event and regatta-id.
     * 
     * @param eventId {@link UUID} of the event to load races for
     * @param regattaId {@link String id} of the regatta to load races for
     */
    public GetFinishedRacesAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
        this.regattaId = regattaId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<ListResult<RaceListRaceDTO>> execute(SailingDispatchContext context) {
        RaceListDataCalculator raceListDataCalculator = new RaceListDataCalculator();
        EventActionUtil.forRacesOfRegatta(context, eventId, regattaId, raceListDataCalculator);
        
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId,
                Duration.ONE_MINUTE.times(5)), new ListResult<>(raceListDataCalculator.getResult()));
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId).append("_").append(regattaId);
    }

    @Override
    public String getLeaderboardName() {
        return regattaId;
    }
}

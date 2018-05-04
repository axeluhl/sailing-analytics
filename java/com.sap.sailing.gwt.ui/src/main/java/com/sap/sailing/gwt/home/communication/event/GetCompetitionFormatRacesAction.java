package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.RaceCompetitionFormatDataCalculator;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ListResult;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in the competition format races view for the
 * {@link #GetCompetitionFormatRacesAction(UUID, String) given event- and regatta-id}, using a
 * {@link RaceCompetitionFormatDataCalculator} to prepare the appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live
 * {@link EventActionUtil#getEventStateDependentTTL(SailingDispatchContext, UUID, Duration) depends on the event's
 * state} using a duration of <i>1 minute</i> for currently running events.
 * </p>
 */
public class GetCompetitionFormatRacesAction implements
        SailingAction<ResultWithTTL<ListResult<RaceCompetitionFormatSeriesDTO>>>, IsClientCacheable, ProvidesLeaderboardRouting {
    
    private UUID eventId;
    private String regattaId;
    
    protected GetCompetitionFormatRacesAction() {
    }

    /**
     * Creates a {@link GetCompetitionFormatRacesAction} instance for the given event and regatta-id.
     * 
     * @param eventId {@link UUID} of the event to load races for
     * @param regattaId {@link String id} of the regatta to load races for
     */
    public GetCompetitionFormatRacesAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
        this.regattaId = regattaId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<ListResult<RaceCompetitionFormatSeriesDTO>> execute(SailingDispatchContext context) throws DispatchException {
        RaceCompetitionFormatDataCalculator competitionFormatDataCalculator = new RaceCompetitionFormatDataCalculator();
        EventActionUtil.forRacesOfRegatta(context, eventId, regattaId, competitionFormatDataCalculator);
        Duration timeToLive = EventActionUtil.getEventStateDependentTTL(context, eventId, Duration.ONE_MINUTE);
        return new ResultWithTTL<>(timeToLive, new ListResult<>(competitionFormatDataCalculator.getResult()));
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

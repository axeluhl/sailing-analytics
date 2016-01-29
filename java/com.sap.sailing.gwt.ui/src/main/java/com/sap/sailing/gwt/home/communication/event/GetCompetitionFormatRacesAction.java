package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.ListResult;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.RaceCompetitionFormatDataCalculator;
import com.sap.sse.common.Duration;

public class GetCompetitionFormatRacesAction implements
        SailingAction<ResultWithTTL<ListResult<RaceCompetitionFormatSeriesDTO>>>, IsClientCacheable {
    
    private UUID eventId;
    private String regattaId;
    
    protected GetCompetitionFormatRacesAction() {
    }

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
}

package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.shared.dispatch.IsClientCacheable;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sse.common.Duration;

public class GetCompetitionFormatRacesAction implements
        Action<ResultWithTTL<ListResult<RaceCompetitionFormatSeriesDTO>>>, IsClientCacheable {
    
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
    public ResultWithTTL<ListResult<RaceCompetitionFormatSeriesDTO>> execute(DispatchContext context) throws DispatchException {
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

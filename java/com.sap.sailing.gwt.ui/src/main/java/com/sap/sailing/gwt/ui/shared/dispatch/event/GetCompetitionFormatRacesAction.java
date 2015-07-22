package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.SortedSetResult;

public class GetCompetitionFormatRacesAction implements Action<ResultWithTTL<SortedSetResult<RaceCompetitionFormatSeriesDTO>>> {
    
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
    public ResultWithTTL<SortedSetResult<RaceCompetitionFormatSeriesDTO>> execute(DispatchContext context) throws DispatchException {
        RaceCompetitionFormatDataCalculator competitionFormatDataCalculator = new RaceCompetitionFormatDataCalculator();
        EventActionUtil.forRacesOfRegatta(context, eventId, regattaId, competitionFormatDataCalculator);
        long timeToLive = EventActionUtil.getEventStateDependentTTL(context, eventId, 60 * 1000);
        return new ResultWithTTL<>(timeToLive, new SortedSetResult<>(competitionFormatDataCalculator.getResult()));
    }

}

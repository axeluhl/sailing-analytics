package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.async.AsyncAction;

public abstract class AbstractGetMapRelatedDataAction<T> implements AsyncAction<T> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Map<CompetitorDTO, Date> from;
    private final Map<CompetitorDTO, Date> to;
    private final boolean extrapolate;
    
    public AbstractGetMapRelatedDataAction(SailingServiceAsync sailingService,
            RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, Date> from,
            Map<CompetitorDTO, Date> to, boolean extrapolate) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.from = from;
        this.to = to;
        this.extrapolate = extrapolate;
    }

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    protected RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    protected Map<CompetitorDTO, Date> getFrom() {
        return from;
    }

    protected Map<CompetitorDTO, Date> getTo() {
        return to;
    }

    protected boolean isExtrapolate() {
        return extrapolate;
    }
}

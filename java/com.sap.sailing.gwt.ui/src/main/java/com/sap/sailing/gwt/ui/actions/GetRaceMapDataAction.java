package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetRaceMapDataAction implements AsyncAction<RaceMapDataDTO> {
    private final SailingServiceAsync sailingService;
    private final Iterable<CompetitorDTO> allCompetitors;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Map<CompetitorDTO, Date> from;
    private final Map<CompetitorDTO, Date> to;
    private final boolean extrapolate;
    private final Date date;
    private final LegIdentifier simulationLegIdentifier;
    
    public GetRaceMapDataAction(SailingServiceAsync sailingService, Iterable<CompetitorDTO> allCompetitors,
            RegattaAndRaceIdentifier raceIdentifier, Date date, Map<CompetitorDTO, Date> from,
            Map<CompetitorDTO, Date> to, boolean extrapolate, LegIdentifier simulationLegIdentifier) {
        this.allCompetitors = allCompetitors;
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.date = date;
        this.from = from;
        this.to = to;
        this.extrapolate = extrapolate;
        this.simulationLegIdentifier = simulationLegIdentifier;
    }
    
    @Override
    public void execute(final AsyncCallback<RaceMapDataDTO> callback) {
        Map<String, Date> fromByCompetitorIdAsString = new HashMap<String, Date>();
        for (Map.Entry<CompetitorDTO, Date> fromEntry : from.entrySet()) {
            fromByCompetitorIdAsString.put(fromEntry.getKey().getIdAsString(), fromEntry.getValue());
        }
        
        Map<String, Date> toByCompetitorIdAsString = new HashMap<String, Date>();
        for (Map.Entry<CompetitorDTO, Date> toEntry : to.entrySet()) {
            toByCompetitorIdAsString.put(toEntry.getKey().getIdAsString(), toEntry.getValue());
        }
        
        sailingService.getRaceMapData(raceIdentifier, date, fromByCompetitorIdAsString, toByCompetitorIdAsString,
                extrapolate, simulationLegIdentifier, new AsyncCallback<CompactRaceMapDataDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(CompactRaceMapDataDTO result) {
                        callback.onSuccess(result.getRaceMapDataDTO(allCompetitors));
                    }
                });
    }
}

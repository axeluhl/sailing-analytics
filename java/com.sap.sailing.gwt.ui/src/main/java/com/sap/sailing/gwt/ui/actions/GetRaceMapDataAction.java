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

public class GetRaceMapDataAction extends AbstractGetMapRelatedDataAction<RaceMapDataDTO> {
    private final Map<String, CompetitorDTO> competitorsByIdAsString;
    private final Date date;
    private final LegIdentifier simulationLegIdentifier;
    private final byte[] md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID;
    private Date timeForEstimation;
    private boolean targetEstimationRequired;
    
    public GetRaceMapDataAction(SailingServiceAsync sailingService, Map<String, CompetitorDTO> competitorsByIdAsString,
            RegattaAndRaceIdentifier raceIdentifier, Date date, Map<CompetitorDTO, Date> from,
            Map<CompetitorDTO, Date> to, boolean extrapolate, LegIdentifier simulationLegIdentifier,
            byte[] md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID, Date timeForEstimation, boolean targetEstimationRequired) {
        super(sailingService, raceIdentifier, from, to, extrapolate);
        this.competitorsByIdAsString = competitorsByIdAsString;
        this.timeForEstimation = timeForEstimation;
        this.targetEstimationRequired =targetEstimationRequired;
        this.date = date;
        this.simulationLegIdentifier = simulationLegIdentifier;
        this.md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID = md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID;
    }
    
    @Override
    public void execute(final AsyncCallback<RaceMapDataDTO> callback) {
        Map<String, Date> fromByCompetitorIdAsString = new HashMap<String, Date>();
        for (Map.Entry<CompetitorDTO, Date> fromEntry : getFrom().entrySet()) {
            fromByCompetitorIdAsString.put(fromEntry.getKey().getIdAsString(), fromEntry.getValue());
        }
        Map<String, Date> toByCompetitorIdAsString = new HashMap<String, Date>();
        for (Map.Entry<CompetitorDTO, Date> toEntry : getTo().entrySet()) {
            toByCompetitorIdAsString.put(toEntry.getKey().getIdAsString(), toEntry.getValue());
        }
        getSailingService().getRaceMapData(getRaceIdentifier(), date, fromByCompetitorIdAsString, toByCompetitorIdAsString,
                isExtrapolate(), simulationLegIdentifier, md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID,timeForEstimation,targetEstimationRequired,
                new AsyncCallback<CompactRaceMapDataDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(CompactRaceMapDataDTO result) {
                        callback.onSuccess(result.getRaceMapDataDTO(competitorsByIdAsString));
                    }
                });
    }
}

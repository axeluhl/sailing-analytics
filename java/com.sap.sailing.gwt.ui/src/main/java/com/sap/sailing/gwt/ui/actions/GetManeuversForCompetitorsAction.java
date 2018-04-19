package com.sap.sailing.gwt.ui.actions;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sse.common.TimeRange;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetManeuversForCompetitorsAction implements AsyncAction<Map<CompetitorWithBoatDTO, List<ManeuverDTO>>> {

    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final Map<CompetitorWithBoatDTO, TimeRange> competitorTimeRanges;

    public GetManeuversForCompetitorsAction(final SailingServiceAsync sailingService,
            final RegattaAndRaceIdentifier raceIdentifier,
            final Map<CompetitorWithBoatDTO, TimeRange> competitorTimeRanges) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.competitorTimeRanges = competitorTimeRanges;
    }

    @Override
    public void execute(final AsyncCallback<Map<CompetitorWithBoatDTO, List<ManeuverDTO>>> callback) {
        sailingService.getManeuvers(raceIdentifier, competitorTimeRanges, callback);
    }

}

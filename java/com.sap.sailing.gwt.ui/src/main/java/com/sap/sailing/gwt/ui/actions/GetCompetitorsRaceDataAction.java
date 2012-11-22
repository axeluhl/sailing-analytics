package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.MultiCompetitorRaceDataDTO;

public class GetCompetitorsRaceDataAction extends DefaultAsyncAction<MultiCompetitorRaceDataDTO> {
    private final SailingServiceAsync sailingService;

    private final RegattaAndRaceIdentifier race;
    private final List<Pair<Date, CompetitorDTO>> competitorsQuery;
    private final Date toDate;
    private final long stepSize;
    private final DetailType detailType;
    
    public GetCompetitorsRaceDataAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier race, List<Pair<Date, CompetitorDTO>> competitorsQuery,
            Date toDate, long stepSize, DetailType detailType, AsyncCallback<MultiCompetitorRaceDataDTO> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.race = race;
        this.competitorsQuery = competitorsQuery;
        this.toDate = toDate;
        this.stepSize = stepSize;
        this.detailType = detailType;
    }

    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getCompetitorsRaceData(race, competitorsQuery, toDate, stepSize, detailType,
                    (AsyncCallback<MultiCompetitorRaceDataDTO>) getWrapperCallback(asyncActionsExecutor));
    }
}

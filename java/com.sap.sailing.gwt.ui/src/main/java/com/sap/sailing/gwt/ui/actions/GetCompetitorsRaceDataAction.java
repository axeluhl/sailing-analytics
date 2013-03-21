package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;

public class GetCompetitorsRaceDataAction extends DefaultAsyncAction<CompetitorsRaceDataDTO> {
    private final SailingServiceAsync sailingService;
    private final RegattaAndRaceIdentifier raceIdentifier;
    private final List<CompetitorDTO> competitors;
    private final Date fromDate;
    private final Date toDate;
    private final long stepSizeInMs;
    private final DetailType detailType;
    private final String leaderboarGroupName;
    private final String leaderboardName;
    
    public GetCompetitorsRaceDataAction(SailingServiceAsync sailingService, RegattaAndRaceIdentifier raceIdentifier,
            List<CompetitorDTO> competitors, Date fromDate, Date toDate, long stepSizeInMs, DetailType detailType,
            String leaderboardGroupName, String leaderboardName, AsyncCallback<CompetitorsRaceDataDTO> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.competitors = competitors;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.stepSizeInMs = stepSizeInMs;
        this.detailType = detailType;
        this.leaderboarGroupName = leaderboardGroupName;
        this.leaderboardName = leaderboardName;
    }

    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService.getCompetitorsRaceData(raceIdentifier, competitors, fromDate, toDate, stepSizeInMs, detailType, leaderboarGroupName, leaderboardName,
                    (AsyncCallback<CompetitorsRaceDataDTO>) getWrapperCallback(asyncActionsExecutor));
    }
}

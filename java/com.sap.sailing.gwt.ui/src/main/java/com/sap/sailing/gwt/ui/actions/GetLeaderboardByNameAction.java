package com.sap.sailing.gwt.ui.actions;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public class GetLeaderboardByNameAction extends DefaultAsyncAction<LeaderboardDTO> {
    private final SailingServiceAsync sailingService;
    private final String leaderboardName;
    private final Date date;
    private final Collection<String> namesOfRacesForWhichToLoadLegDetails;
    private final boolean waitForLatestManeuverAnalysis;
    
    public GetLeaderboardByNameAction(SailingServiceAsync sailingService, String leaderboardName, Date date,
            final Collection<String> namesOfRacesForWhichToLoadLegDetails, boolean waitForLatestManeuverAnalysis,
            AsyncCallback<LeaderboardDTO> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.date = date;
        this.namesOfRacesForWhichToLoadLegDetails = namesOfRacesForWhichToLoadLegDetails;
        this.waitForLatestManeuverAnalysis = waitForLatestManeuverAnalysis;
    }
    
    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService
                .getLeaderboardByName(leaderboardName, date, namesOfRacesForWhichToLoadLegDetails,
                        waitForLatestManeuverAnalysis,
                        (AsyncCallback<LeaderboardDTO>) getWrapperCallback(asyncActionsExecutor));
    }

}
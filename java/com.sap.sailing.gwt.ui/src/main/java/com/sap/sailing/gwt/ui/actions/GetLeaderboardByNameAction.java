package com.sap.sailing.gwt.ui.actions;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

public class GetLeaderboardByNameAction extends DefaultAsyncAction<LeaderboardDTO> {
    private final SailingServiceAsync sailingService;
    private final String leaderboardName;
    private final Date date;
    private final Collection<String> namesOfRacesForWhichToLoadLegDetails;
    
    public GetLeaderboardByNameAction(SailingServiceAsync sailingService, String leaderboardName, Date date,
            final Collection<String> namesOfRacesForWhichToLoadLegDetails, AsyncCallback<LeaderboardDTO> callback) {
        super(callback);
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.date = date;
        this.namesOfRacesForWhichToLoadLegDetails = namesOfRacesForWhichToLoadLegDetails;
    }
    
    @Override
    public void execute(AsyncActionsExecutor asyncActionsExecutor) {
        sailingService
                .getLeaderboardByName(leaderboardName, date, namesOfRacesForWhichToLoadLegDetails,
                        (AsyncCallback<LeaderboardDTO>) getWrapperCallback(asyncActionsExecutor));
    }

}
package com.sap.sailing.gwt.ui.actions;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public class GetLeaderboardByNameAction extends DefaultAsyncAction<LeaderboardDTO>
{
    private final SailingServiceAsync sailingService;
    private final String leaderboardName;
    private final Date date;
    private final Collection<String> namesOfRacesForWhichToLoadLegDetails;
    private LeaderboardDTO result;
    
    private AsyncCallback<LeaderboardDTO> callback;

    public GetLeaderboardByNameAction(SailingServiceAsync sailingService, String leaderboardName, Date date,
            final Collection<String> namesOfRacesForWhichToLoadLegDetails) {
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.date = date;
        this.namesOfRacesForWhichToLoadLegDetails = namesOfRacesForWhichToLoadLegDetails;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        sailingService.getLeaderboardByName(leaderboardName, date, namesOfRacesForWhichToLoadLegDetails, (AsyncCallback<LeaderboardDTO>) wrapperCallback);
    }

    @Override
    public LeaderboardDTO getResult() {
        return result;
    }

    @Override
    public AsyncCallback<LeaderboardDTO> getCallback() {
        return callback;
    }

    @Override
    public void setCallback(AsyncCallback<LeaderboardDTO> callback) {
        this.callback = callback;
    }

    @Override
    public String getName() {
        return GetLeaderboardByNameAction.class.getName();
    }
}
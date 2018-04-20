package com.sap.sailing.gwt.ui.actions;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.async.AsyncAction;

public class GetLeaderboardDataEntriesAction implements AsyncAction<List<Util.Triple<String, List<CompetitorWithBoatDTO>, List<Double>>>> {
    private final SailingServiceAsync sailingService;
    private final DetailType detailType;
    private final String leaderboardName;
    private final Date date;
    
    public GetLeaderboardDataEntriesAction(SailingServiceAsync sailingService, String leaderboardName, Date date, DetailType detailType) {
        this.sailingService = sailingService;
        this.leaderboardName = leaderboardName;
        this.date = date;
        this.detailType = detailType;
    }

    @Override
    public void execute(AsyncCallback<List<Util.Triple<String, List<CompetitorWithBoatDTO>, List<Double>>>> callback) {
        sailingService.getLeaderboardDataEntriesForAllRaceColumns(leaderboardName, date, detailType, callback);
    }
}

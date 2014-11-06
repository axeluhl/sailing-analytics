package com.sap.sailing.gwt.ui.test;

import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSortableColumnWithMinMax;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public class LeaderboardPanelMock extends LeaderboardPanel {

    public LeaderboardPanelMock(SailingServiceAsync sailingService,
            String leaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        super(sailingService, new AsyncActionsExecutor(), LeaderboardSettingsFactory.getInstance().createNewDefaultSettings(
        /* racesToShow */null, /* namesOfRacesToShow */ null, null, /* autoExpandFirstRace */ false, /* showRegattaRank */ true), new CompetitorSelectionModel(
                /* hasMultiSelection */true), leaderboardName, errorReporter, stringMessages, new UserAgentDetails("gecko1_8"), /* showRaceDetails */ true);
    }

    @Override
    public void addColumn(LeaderboardSortableColumnWithMinMax<LeaderboardRowDTO, ?> column) {
        // TODO Auto-generated method stub
        super.addColumn(column);
    }

    @Override
    public RaceColumn<?> createRaceColumn(RaceColumnDTO race) {
        // TODO Auto-generated method stub
        return super.createRaceColumn(race);
    }

    @Override
    public void removeColumn(int columnIndex) {
        // TODO Auto-generated method stub
        super.removeColumn(columnIndex);
    }

    @Override
    public void updateLeaderboard(LeaderboardDTO leaderboard) {
        // TODO Auto-generated method stub
        super.updateLeaderboard(leaderboard);
    }

    @Override
    public SortedCellTable<LeaderboardRowDTO> getLeaderboardTable() {
        // TODO Auto-generated method stub
        return super.getLeaderboardTable();
    }
    
}

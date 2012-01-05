package com.sap.sailing.gwt.ui.test;

import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringConstants;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.SortableColumn;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class LeaderboardPanelMock extends LeaderboardPanel {

    public LeaderboardPanelMock(SailingServiceAsync sailingService,
            String leaderboardName, ErrorReporter errorReporter,
            StringConstants stringConstants) {
        super(sailingService, leaderboardName, errorReporter, stringConstants);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void addColumn(SortableColumn<LeaderboardRowDAO, ?> column) {
        // TODO Auto-generated method stub
        super.addColumn(column);
    }

    @Override
    public RaceColumn<?> createRaceColumn(String raceName,
            boolean isMedalRace, boolean isTracked) {
        // TODO Auto-generated method stub
        return super.createRaceColumn(raceName, isMedalRace, isTracked);
    }

    @Override
    public void removeColumn(int columnIndex) {
        // TODO Auto-generated method stub
        super.removeColumn(columnIndex);
    }

    @Override
    public void updateLeaderboard(LeaderboardDAO leaderboard) {
        // TODO Auto-generated method stub
        super.updateLeaderboard(leaderboard);
    }

    @Override
    public CellTable<LeaderboardRowDAO> getLeaderboardTable() {
        // TODO Auto-generated method stub
        return super.getLeaderboardTable();
    }
    
}

package com.sap.sailing.gwt.ui.test;

import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.SortableColumn;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public class LeaderboardPanelMock extends LeaderboardPanel {

    public LeaderboardPanelMock(SailingServiceAsync sailingService,
            String leaderboardName, ErrorReporter errorReporter,
            StringMessages stringConstants) {
        super(sailingService, null, new CompetitorSelectionModel(/* hasMultiSelection */ true), leaderboardName, errorReporter, stringConstants);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void addColumn(SortableColumn<LeaderboardRowDTO, ?> column) {
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
    public void updateLeaderboard(LeaderboardDTO leaderboard) {
        // TODO Auto-generated method stub
        super.updateLeaderboard(leaderboard);
    }

    @Override
    public CellTable<LeaderboardRowDTO> getLeaderboardTable() {
        // TODO Auto-generated method stub
        return super.getLeaderboardTable();
    }
    
}

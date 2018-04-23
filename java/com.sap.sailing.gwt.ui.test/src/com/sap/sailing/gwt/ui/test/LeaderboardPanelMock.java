package com.sap.sailing.gwt.ui.test;

import java.util.Arrays;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.FlagImageResolverImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractSortableColumnWithMinMax;
import com.sap.sailing.gwt.ui.leaderboard.ClassicLeaderboardStyle;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;

public class LeaderboardPanelMock extends MultiRaceLeaderboardPanel {

    public LeaderboardPanelMock(SailingServiceAsync sailingService,
            String leaderboardName, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        super(null, null, sailingService, new AsyncActionsExecutor(), new MultiRaceLeaderboardSettings(),
                new CompetitorSelectionModel(
                        /* hasMultiSelection */true),
                leaderboardName, errorReporter, stringMessages, /* showRaceDetails */ true, new ClassicLeaderboardStyle(),
                FlagImageResolverImpl.get(), Arrays.asList(DetailType.values()));
    }

    @Override
    protected void addColumn(AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?> column) {
        super.addColumn(column);
    }

    @Override
    public RaceColumn<?> createRaceColumn(RaceColumnDTO race) {
        return super.createRaceColumn(race);
    }

    @Override
    public void removeColumn(int columnIndex) {
        super.removeColumn(columnIndex);
    }

    @Override
    public void updateLeaderboard(LeaderboardDTO leaderboard) {
        super.updateLeaderboard(leaderboard);
    }

    @Override
    public SortedCellTable<LeaderboardRowDTO> getLeaderboardTable() {
        return super.getLeaderboardTable();
    }
    
}

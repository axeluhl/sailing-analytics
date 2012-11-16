package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.charts.LeaderboardRankChart;

public class RankChartDialog extends DialogBoxExt {
    public RankChartDialog(SailingServiceAsync sailingService, String leaderboardName, CompetitorSelectionProvider competitorSelectionProvider,
            StringMessages stringMessages, ErrorReporter errorReporter, boolean compactChart) {
        super(new Label(stringMessages.close()));
        setWidget(new LeaderboardRankChart(sailingService, leaderboardName, competitorSelectionProvider, stringMessages, errorReporter, compactChart));
    }
}

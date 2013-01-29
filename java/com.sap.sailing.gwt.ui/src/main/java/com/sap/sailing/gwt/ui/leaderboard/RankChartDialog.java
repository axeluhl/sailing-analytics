package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.charts.LeaderboardRankChart;

public class RankChartDialog extends DialogBoxExt {
    public RankChartDialog(SailingServiceAsync sailingService, String leaderboardName, CompetitorSelectionProvider competitorSelectionProvider,
            Timer timer, StringMessages stringMessages, ErrorReporter errorReporter, boolean compactChart) {
        super(new Label(stringMessages.close()));
        this.setPopupPosition(15, 15);
        this.setHTML(stringMessages.compareCompetitors());
        this.setWidth(Window.getClientWidth() - 250 + "px");
        this.setAnimationEnabled(true);
        setWidget(new LeaderboardRankChart(sailingService, leaderboardName, competitorSelectionProvider, timer,
                stringMessages, errorReporter, compactChart));
    }
}

package com.sap.sailing.gwt.home.desktop.partials.old.competitorcharts;

import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;

public interface CompetitorChartsDelegate {

    void setCompetitorChart(MultiCompetitorLeaderboardChart competitorChart);

    ListBox getChartTypeSelectionControl();

    HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler);
}

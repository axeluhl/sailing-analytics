package com.sap.sailing.gwt.home.client.place.fakeseries.tabs;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.oldcompetitorcharts.OldCompetitorCharts;
import com.sap.sailing.gwt.home.client.place.fakeseries.EventSeriesAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesTabView;
import com.sap.sailing.gwt.home.client.place.fakeseries.SeriesView;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class EventSeriesCompetitorAnalyticsTabView extends Composite implements
        SeriesTabView<EventSeriesCompetitorAnalyticsPlace> {

    public EventSeriesCompetitorAnalyticsTabView() {

    }

    @UiField
    protected OldCompetitorCharts competitorCharts;

    @Override
    public Class<EventSeriesCompetitorAnalyticsPlace> getPlaceClassForActivation() {
        return EventSeriesCompetitorAnalyticsPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return currentPresenter.getCtx().getSeriesDTO().isHasAnalytics() ? TabView.State.VISIBLE : TabView.State.INVISIBLE;
    }

    @Override
    public void start(EventSeriesCompetitorAnalyticsPlace myPlace, AcceptsOneWidget contentArea) {

        contentArea.setWidget(new Placeholder());

        String leaderboardName = myPlace.getCtx().getSeriesDTO().getLeaderboardId();

        if (leaderboardName != null && !leaderboardName.isEmpty()) {


            EventSeriesAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx()
                    .getAnalyticsManager();

            initWidget(ourUiBinder.createAndBindUi(this));

            DetailType initialDetailType = DetailType.OVERALL_RANK;
            if (regattaAnalyticsManager.getMultiCompetitorChart() == null) {
                regattaAnalyticsManager.createMultiCompetitorChart(leaderboardName, initialDetailType);
            }
            competitorCharts.setChart(regattaAnalyticsManager.getMultiCompetitorChart(), getAvailableDetailsTypes(),
                    initialDetailType);

            regattaAnalyticsManager.showCompetitorChart(competitorCharts.getSelectedChartDetailType());
            contentArea.setWidget(this);
        }
    }

    private List<DetailType> getAvailableDetailsTypes() {
        List<DetailType> availableDetailsTypes = new ArrayList<DetailType>();
        availableDetailsTypes.add(DetailType.OVERALL_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_TOTAL_POINTS_SUM);
        return availableDetailsTypes;
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, EventSeriesCompetitorAnalyticsTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private SeriesView.Presenter currentPresenter;

    @Override
    public EventSeriesCompetitorAnalyticsPlace placeToFire() {
        return new EventSeriesCompetitorAnalyticsPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(SeriesView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }


}
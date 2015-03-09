package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.oldcompetitorcharts.OldCompetitorCharts;
import com.sap.sailing.gwt.home.client.place.event.regattaanalytics.RegattaAnalyticsDataManager;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaCompetitorAnalyticsTabView extends Composite implements
        RegattaTabView<RegattaCompetitorAnalyticsPlace> {

    public RegattaCompetitorAnalyticsTabView() {

    }

    @UiField
    protected OldCompetitorCharts competitorCharts;

    @Override
    public Class<RegattaCompetitorAnalyticsPlace> getPlaceClassForActivation() {
        return RegattaCompetitorAnalyticsPlace.class;
    }
    
    @Override
    public TabView.State getState() {
        return TabView.State.VISIBLE;
    }

    @Override
    public void start(RegattaCompetitorAnalyticsPlace myPlace, AcceptsOneWidget contentArea) {

        contentArea.setWidget(new Placeholder());

        String regattaId = myPlace.getRegattaId();

        if (regattaId != null && !regattaId.isEmpty()) {

            String leaderboardName = regattaId;

            RegattaAnalyticsDataManager regattaAnalyticsManager = currentPresenter.getCtx()
                    .getRegattaAnalyticsManager();

            initWidget(ourUiBinder.createAndBindUi(this));

            DetailType initialDetailType = DetailType.REGATTA_RANK;
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
        availableDetailsTypes.add(DetailType.REGATTA_RANK);
        availableDetailsTypes.add(DetailType.REGATTA_TOTAL_POINTS_SUM);
        return availableDetailsTypes;
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaCompetitorAnalyticsTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;

    @Override
    public RegattaCompetitorAnalyticsPlace placeToFire() {
        return new RegattaCompetitorAnalyticsPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }

    public boolean isSmallWidth() {
        int width = Window.getClientWidth();
        return width <= 720;
    }

}
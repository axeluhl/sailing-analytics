package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.home.client.place.event.regattaanalytics.RegattaAnalytics;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaCompetitorAnalyticsTabView extends Composite implements RegattaTabView<RegattaOverviewPlace> {

    public RegattaCompetitorAnalyticsTabView() {

    }

    @UiField(provided = true)
    protected RegattaAnalytics regattaAnalytics;

    @Override
    public Class<RegattaOverviewPlace> getPlaceClassForActivation() {
        return RegattaOverviewPlace.class;
    }

    @Override
    public void start(RegattaOverviewPlace myPlace, AcceptsOneWidget contentArea) {

        EventViewDTO event = myPlace.getCtx().getEventDTO();
        String regattaId = myPlace.getRegattaId();

//        regattaAnalytics = new RegattaAnalytics(event, regattaId, RegattaNavigationTabs.CompetitorAnalytics,
//                currentPresenter.getTimerForClientServerOffset(), currentPresenter.getHomePlaceNavigator());
//
//        // TODO: load leaderboard, as done in RegattaActivity
//
//        initWidget(ourUiBinder.createAndBindUi(this));
//
//        contentArea.setWidget(this);
        contentArea.setWidget(new Label());
    }

    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaCompetitorAnalyticsTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);
    private Presenter currentPresenter;

    @Override
    public RegattaOverviewPlace placeToFire() {
        return new RegattaOverviewPlace(currentPresenter.getCtx());
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;

    }

}
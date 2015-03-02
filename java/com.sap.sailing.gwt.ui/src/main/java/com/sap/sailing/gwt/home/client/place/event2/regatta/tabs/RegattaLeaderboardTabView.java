package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.home.client.place.event.regattaanalytics.RegattaAnalytics;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;
import com.sap.sailing.gwt.home.client.place.regatta.RegattaPlace.RegattaNavigationTabs;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaLeaderboardTabView extends Composite implements RegattaTabView<RegattaLeaderboardPlace> {

    private LeaderboardDTO leaderboardDTO;
    private Presenter currentPresenter;

    @UiField(provided = true)
    protected RegattaAnalytics regattaAnalytics;

    public RegattaLeaderboardTabView() {

    }

    @Override
    public Class<RegattaLeaderboardPlace> getPlaceClassForActivation() {
        return RegattaLeaderboardPlace.class;
    }

    @Override
    public void setPresenter(EventRegattaView.Presenter currentPresenter) {
        this.currentPresenter = currentPresenter;
    }

    @Override
    public void start(final RegattaLeaderboardPlace myPlace, final AcceptsOneWidget contentArea) {

        contentArea.setWidget(new Placeholder());

        String regattaId = myPlace.getRegattaId();

        if (regattaId != null && !regattaId.isEmpty()) {

            EventViewDTO event = myPlace.getCtx().getEventDTO();

            regattaAnalytics = new RegattaAnalytics(event, regattaId, RegattaNavigationTabs.Leaderboard,
                    currentPresenter.getTimerForClientServerOffset(), currentPresenter.getHomePlaceNavigator());

            // TODO: load leaderboard, as done in RegattaActivity

            initWidget(ourUiBinder.createAndBindUi(this));

            contentArea.setWidget(this);
        } else {
            contentArea.setWidget(new Label("No leaderboard specified, cannot proceed to leaderboardpage"));
            new Timer() {
                @Override
                public void run() {
                    currentPresenter.goToOverview();
                }
            }.schedule(3000);

        }

    }


    @Override
    public void stop() {

    }

    interface MyBinder extends UiBinder<HTMLPanel, RegattaLeaderboardTabView> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public RegattaLeaderboardPlace placeToFire() {
        return new RegattaLeaderboardPlace(currentPresenter.getCtx());
    }

}
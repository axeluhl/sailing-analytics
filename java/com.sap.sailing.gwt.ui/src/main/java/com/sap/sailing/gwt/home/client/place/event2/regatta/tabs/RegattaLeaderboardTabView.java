package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.home.client.place.event2.regatta.RegattaTabView;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class RegattaLeaderboardTabView extends Composite implements RegattaTabView<RegattaLeaderboardPlace> {

    private LeaderboardDTO leaderboardDTO;
    private Presenter currentPresenter;

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
    public void start(final RegattaLeaderboardPlace selectedPlace, final AcceptsOneWidget contentArea) {
        
        String regattaId = selectedPlace.getRegattaId();
       
        if (regattaId != null && !regattaId.isEmpty() ) {
            
        } else {
            contentArea.setWidget(new Label("No leaderboard specified, cannot proceed to leaderboardpage"));
            new Timer() {
                @Override
                public void run() {
                    currentPresenter.gotoOverview();
                }
            }.schedule(3000);

        }
            
            
        
        

        
        
    }

    private void afterLoad(AcceptsOneWidget contentArea) {

        initWidget(ourUiBinder.createAndBindUi(this));
        contentArea.setWidget(this);
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
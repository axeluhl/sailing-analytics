package com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.leaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.overview.RegattaOverviewPlace;

/**
 * Created by pgtaboada on 25.11.14.
 */
public class EventLeaderboardWidgetAndActivity extends Composite implements TabActivity<EventLeaderboardPlace, EventContext, EventRegattaView.Presenter> {

    private LeaderboardDTO leaderboardDTO;

    public EventLeaderboardWidgetAndActivity() {

    }

    @Override
    public Class<EventLeaderboardPlace> getPlaceClassForActivation() {
        return EventLeaderboardPlace.class;
    }
    
    @Override
    public void setPresenter(EventRegattaView.Presenter presenter) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void start(final EventLeaderboardPlace selectedPlace, final AcceptsOneWidget contentArea) {
        
        String regattaId = selectedPlace.getRegattaId();
       
        if (regattaId != null && !regattaId.isEmpty() ) {
            
        } else {
            contentArea.setWidget(new Label("No leaderboard specified, cannot proceed to leaderboardpage"));
            new Timer() {
                @Override
                public void run() {
                    // chain is not nice...
                    selectedPlace
                        .getCtx()
                        .getClientFactory()
                        .getPlaceController()
                            .goTo(new RegattaOverviewPlace(selectedPlace.getCtx()));
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

    interface MyBinder extends UiBinder<HTMLPanel, EventLeaderboardWidgetAndActivity> {
    }

    private static MyBinder ourUiBinder = GWT.create(MyBinder.class);

    @Override
    public EventLeaderboardPlace placeToFire(EventContext ctx) {
        return new EventLeaderboardPlace(ctx);
    }

}
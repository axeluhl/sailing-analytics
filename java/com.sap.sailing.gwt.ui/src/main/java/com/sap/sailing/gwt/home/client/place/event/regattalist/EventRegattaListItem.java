package com.sap.sailing.gwt.home.client.place.event.regattalist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class EventRegattaListItem extends Composite {
    private static EventRegattaHeaderUiBinder uiBinder = GWT.create(EventRegattaHeaderUiBinder.class);

    interface EventRegattaHeaderUiBinder extends UiBinder<Widget, EventRegattaListItem> {
    }

    @UiField SpanElement regattaName;
    @UiField SpanElement leaderboardGroupName;

    @UiField SpanElement competitorsCount;
    @UiField SpanElement boatDesign; 
    @UiField SpanElement scoringSystem; 
    @UiField SpanElement averageWind; 
    @UiField SpanElement plannedRacesCount; 
    @UiField SpanElement regattaSeriesName; 

    @UiField DivElement isLiveElement; 
    @UiField Anchor regattaRacesLink;
    
    @SuppressWarnings("unused")
    private RegattaDTO regatta;
    
    private final LeaderboardGroupDTO leaderboardGroup;
    private final StrippedLeaderboardDTO leaderboard;
    private final EventPageNavigator pageNavigator;
    
    public EventRegattaListItem(EventPageNavigator pageNavigator, LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO leaderboard) {
        this.pageNavigator = pageNavigator;
        this.leaderboardGroup = leaderboardGroup;
        this.leaderboard = leaderboard;

        EventRegattaListResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        updateUI();
    }

    @UiHandler("regattaRacesLink")
    public void goToRegattaRaces(ClickEvent e) {
        pageNavigator.goToRegattaRaces(leaderboard);
    }

    private void updateUI() {
        regattaName.setInnerText(leaderboard.displayName != null ? leaderboard.displayName : leaderboard.name);
        leaderboardGroupName.setInnerText(leaderboardGroup.getName());
        
        competitorsCount.setInnerText("XYZ");
        boatDesign.setInnerText("XYZ");
        scoringSystem.setInnerText("XYZ");
        averageWind.setInnerText("XYZ");
        plannedRacesCount.setInnerText("XYZ");
        regattaSeriesName.setInnerText("ABC");
    }
}

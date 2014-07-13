package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.home.client.place.event.regatta.Regatta;
import com.sap.sailing.gwt.home.client.place.event.regattaschedule.EventRegattaScheduleResources;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaRaces extends Composite {
    private static EventRegattaRacesUiBinder uiBinder = GWT.create(EventRegattaRacesUiBinder.class);

    interface EventRegattaRacesUiBinder extends UiBinder<Widget, EventRegattaRaces> {
    }

    @UiField(provided=true) Regatta regatta;
    @UiField HTMLPanel regattaSeriesNavigationPanel;   
    @UiField HTMLPanel regattaSeriesPanel;   
    
    @SuppressWarnings("unused")
    private final EventDTO event;
    
    private final Timer timerForClientServerOffset;
    private final EventPageNavigator pageNavigator;

    public EventRegattaRaces(EventDTO event, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        this.event = event;
        this.timerForClientServerOffset = timerForClientServerOffset;
        this.pageNavigator = pageNavigator;
        
        regatta = new Regatta(event, false, timerForClientServerOffset, pageNavigator);
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setRacesFromRaceGroup(RaceGroupDTO raceGroup, StrippedLeaderboardDTO leaderboard) {
        regatta.setData(raceGroup, leaderboard, null);
    }

//    public void setRacesFromRegatta(RegattaDTO regattaDTO, LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO regattaLeaderboard) {
//        regatta.setData(leaderboardGroup, regattaLeaderboard);
//        
//        for(SeriesDTO series: regattaDTO.series) {
//            EventRegattaScheduleSeries eventRegattaScheduleSeries = new EventRegattaScheduleSeries(regattaLeaderboard, series, timerForClientServerOffset, pageNavigator); 
//            regattaSeriesPanel.add(eventRegattaScheduleSeries);
//        }
//    }
//
//    public void setRacesFromFlexibleLeaderboard(LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO flexibleLeaderboard) {
//        regatta.setData(leaderboardGroup, flexibleLeaderboard);
//
//        EventRegattaScheduleFleet eventRegattaScheduleFleet = new EventRegattaScheduleFleet(flexibleLeaderboard, timerForClientServerOffset, pageNavigator);
//        regattaSeriesPanel.add(eventRegattaScheduleFleet);
//    }
//
}

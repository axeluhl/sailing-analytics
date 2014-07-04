package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.home.client.place.event.regattaheader.EventRegattaHeader;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaSchedule extends Composite {
    private static EventRegattaScheduleUiBinder uiBinder = GWT.create(EventRegattaScheduleUiBinder.class);

    interface EventRegattaScheduleUiBinder extends UiBinder<Widget, EventRegattaSchedule> {
    }

    @UiField(provided=true) EventRegattaHeader regattaHeader;
    @UiField HTMLPanel seriesListPanel;    
    
    @SuppressWarnings("unused")
    private final EventDTO event;
    
    private final Timer timerForClientServerOffset;
    private final EventPageNavigator pageNavigator;

    public EventRegattaSchedule(EventDTO event, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        this.event = event;
        this.timerForClientServerOffset = timerForClientServerOffset;
        this.pageNavigator = pageNavigator;
        
        regattaHeader = new EventRegattaHeader(event, timerForClientServerOffset, pageNavigator);
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setRacesFromRegatta(RegattaDTO regatta, LeaderboardGroupDTO leaderboardGroup, StrippedLeaderboardDTO regattaLeaderboard) {
        regattaHeader.setData(regatta, leaderboardGroup, regattaLeaderboard);
        
        for(SeriesDTO series: regatta.series) {
            EventRegattaScheduleSeries eventRegattaScheduleSeries = new EventRegattaScheduleSeries(regattaLeaderboard, series, timerForClientServerOffset, pageNavigator); 
            seriesListPanel.add(eventRegattaScheduleSeries);
        }
    }

    public void setRacesFromFlexibleLeaderboard(StrippedLeaderboardDTO flexibleLeaderboard) {
        
    }

}

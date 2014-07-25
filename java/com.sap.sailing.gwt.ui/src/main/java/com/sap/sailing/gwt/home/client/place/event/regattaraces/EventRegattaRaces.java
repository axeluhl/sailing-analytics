package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.home.client.place.event.regatta.Regatta;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaRaces extends Composite {
    private static EventRegattaRacesUiBinder uiBinder = GWT.create(EventRegattaRacesUiBinder.class);

    interface EventRegattaRacesUiBinder extends UiBinder<Widget, EventRegattaRaces> {
    }

//    @UiField(provided=true) Regatta regatta;
    @UiField HTMLPanel regattaPhasesNavigationPanel;   
    @UiField HTMLPanel regattaPhasesPanel;
    @UiField HTMLPanel rootPanel;
    
    @SuppressWarnings("unused")
    private final EventDTO event;

    private final Regatta regatta;
    private final Timer timerForClientServerOffset;
    private final EventPageNavigator pageNavigator;

    public EventRegattaRaces(EventDTO event, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        this.event = event;
        this.timerForClientServerOffset = timerForClientServerOffset;
        this.pageNavigator = pageNavigator;
        
        regatta = new Regatta(event, false, timerForClientServerOffset, pageNavigator);
        
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        rootPanel.getElement().appendChild(regatta.getElement());
    }
    
    public void setRacesFromRaceGroup(RaceGroupDTO raceGroup, StrippedLeaderboardDTO leaderboard) {
        regatta.setData(raceGroup, leaderboard, null);
        
        int regattaPhases = raceGroup.getSeries().size();
        if(regattaPhases > 1) {
            for(RaceGroupSeriesDTO series: raceGroup.getSeries()) {
                EventRegattaRacesPhase regattaPhase = new EventRegattaRacesPhase(leaderboard, series, timerForClientServerOffset, pageNavigator); 
                regattaPhasesPanel.getElement().appendChild(regattaPhase.getElement());
            }
        } else {
            RaceGroupSeriesDTO raceGroupSeriesDTO = raceGroup.getSeries().get(0);
            EventRegattaRacesPhase regattaPhase = new EventRegattaRacesPhase(leaderboard, raceGroupSeriesDTO, timerForClientServerOffset, pageNavigator); 
            regattaPhasesPanel.getElement().appendChild(regattaPhase.getElement());
        }
    }
}

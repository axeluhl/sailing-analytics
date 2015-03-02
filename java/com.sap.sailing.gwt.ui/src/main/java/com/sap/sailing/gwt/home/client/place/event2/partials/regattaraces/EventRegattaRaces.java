package com.sap.sailing.gwt.home.client.place.event2.partials.regattaraces;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventComposite;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.regatta.Regatta;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaRaces extends AbstractEventComposite {
    private static EventRegattaRacesUiBinder uiBinder = GWT.create(EventRegattaRacesUiBinder.class);

    interface EventRegattaRacesUiBinder extends UiBinder<Widget, EventRegattaRaces> {
    }

    @UiField(provided=true) Regatta regatta;
    @UiField HTMLPanel regattaPhasesNavigationPanel;   
    @UiField HTMLPanel regattaPhasesPanel;
    @UiField HTMLPanel rootPanel;
    
    private final Timer timerForClientServerOffset;
    private final List<EventRegattaRacesPhase> phaseElements;

    public EventRegattaRaces(EventViewDTO event, Timer timerForClientServerOffset, HomePlacesNavigator placeNavigator,
            EventPlaceNavigator pageNavigator) {
        super(event, pageNavigator);
        this.timerForClientServerOffset = timerForClientServerOffset;
        
        phaseElements = new ArrayList<EventRegattaRacesPhase>();
        
        regatta = new Regatta(event, timerForClientServerOffset, true, placeNavigator, pageNavigator);
        
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setRaces(LeaderboardGroupDTO leaderboardGroup, boolean hasMultipleLeaderboardGroups, StrippedLeaderboardDTO leaderboard, RaceGroupDTO raceGroup) {
        // clear all existing child elements first
        regattaPhasesPanel.getElement().removeAllChildren();
        phaseElements.clear();
       
        regatta.setData(leaderboardGroup, hasMultipleLeaderboardGroups, leaderboard, raceGroup);
        
        int regattaPhases = raceGroup.getSeries().size();
        if(regattaPhases > 1) {
            for(RaceGroupSeriesDTO series: raceGroup.getSeries()) {
                EventRegattaRacesPhase regattaPhase = new EventRegattaRacesPhase(leaderboard, series, timerForClientServerOffset, getPageNavigator()); 
                regattaPhasesPanel.getElement().appendChild(regattaPhase.getElement());
                phaseElements.add(regattaPhase);
            }
        } else {
            RaceGroupSeriesDTO raceGroupSeriesDTO = raceGroup.getSeries().get(0);
            EventRegattaRacesPhase regattaPhase = new EventRegattaRacesPhase(leaderboard, raceGroupSeriesDTO, timerForClientServerOffset, getPageNavigator()); 
            regattaPhasesPanel.getElement().appendChild(regattaPhase.getElement());
            phaseElements.add(regattaPhase);
        }
    }
}

package com.sap.sailing.gwt.home.client.place.event.partials.regattaraces;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.EventView.Presenter;
import com.sap.sailing.gwt.home.client.place.event.partials.Regatta;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class EventRegattaRaces extends Composite {
    private static EventRegattaRacesUiBinder uiBinder = GWT.create(EventRegattaRacesUiBinder.class);

    interface EventRegattaRacesUiBinder extends UiBinder<Widget, EventRegattaRaces> {
    }

    @UiField(provided=true) Regatta regatta;
    @UiField HTMLPanel regattaPhasesNavigationPanel;   
    @UiField HTMLPanel regattaPhasesPanel;
    @UiField HTMLPanel rootPanel;
    
    private final List<EventRegattaRacesPhase> phaseElements;
    private final Presenter presenter;

    public EventRegattaRaces(EventView.Presenter presenter) {
        this.presenter = presenter;
        
        phaseElements = new ArrayList<EventRegattaRacesPhase>();
        
        regatta = new Regatta(true, presenter);
        
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
                EventRegattaRacesPhase regattaPhase = new EventRegattaRacesPhase(leaderboard, series, presenter); 
                regattaPhasesPanel.getElement().appendChild(regattaPhase.getElement());
                phaseElements.add(regattaPhase);
            }
        } else {
            RaceGroupSeriesDTO raceGroupSeriesDTO = raceGroup.getSeries().get(0);
            EventRegattaRacesPhase regattaPhase = new EventRegattaRacesPhase(leaderboard, raceGroupSeriesDTO, presenter); 
            regattaPhasesPanel.getElement().appendChild(regattaPhase.getElement());
            phaseElements.add(regattaPhase);
        }
    }
}

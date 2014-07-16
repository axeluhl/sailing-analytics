package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaRacesPhase extends Composite {
    private static EventRegattaRacesPhaseUiBinder uiBinder = GWT.create(EventRegattaRacesPhaseUiBinder.class);

    interface EventRegattaRacesPhaseUiBinder extends UiBinder<Widget, EventRegattaRacesPhase> {
    }

    @UiField SpanElement phaseName;
    @UiField HTMLPanel fleetGroupsPanel;

    public EventRegattaRacesPhase(StrippedLeaderboardDTO leaderboard, RaceGroupSeriesDTO series, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        phaseName.setInnerText(series.getName());
        
        int numberOfFleets = series.getFleets().size();
        if(numberOfFleets > 1) {
            if(series.hasOrderedFleets()) { 
                for(FleetDTO fleet: series.getFleets()) {
                    // ordered fleets are splitted into different fleet groups -> e.g. Gold Silver
                    List<FleetDTO> fleetsToShow = new ArrayList<FleetDTO>();
                    fleetsToShow.add(fleet);
                    EventRegattaRacesFleetGroup fleetGroup = new EventRegattaRacesFleetGroup(leaderboard, series, fleetsToShow, timerForClientServerOffset, pageNavigator); 
                    fleetGroupsPanel.add(fleetGroup);
                }
            } else {
                // unordered fleets are NOT splitted into different fleet groups -> e.g. Blue, Yellow
                List<FleetDTO> fleetsToShow = new ArrayList<FleetDTO>(series.getFleets());
                EventRegattaRacesFleetGroup fleetGroup = new EventRegattaRacesFleetGroup(leaderboard, series, fleetsToShow, timerForClientServerOffset, pageNavigator); 
                fleetGroupsPanel.add(fleetGroup);
            }
        } else {
            // single fleet
            EventRegattaRacesFleetGroup fleetGroup = new EventRegattaRacesFleetGroup(leaderboard, series, Collections.<FleetDTO>emptyList(), timerForClientServerOffset, pageNavigator); 
            fleetGroupsPanel.add(fleetGroup);
        }
    }
    
}

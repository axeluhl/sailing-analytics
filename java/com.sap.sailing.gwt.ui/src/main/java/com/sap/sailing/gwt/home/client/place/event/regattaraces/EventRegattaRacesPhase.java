package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaRacesPhase extends UIObject {
    private static EventRegattaRacesPhaseUiBinder uiBinder = GWT.create(EventRegattaRacesPhaseUiBinder.class);

    interface EventRegattaRacesPhaseUiBinder extends UiBinder<DivElement, EventRegattaRacesPhase> {
    }

    @UiField SpanElement phaseName;
    @UiField DivElement fleetGroupsPanel;

    public EventRegattaRacesPhase(StrippedLeaderboardDTO leaderboard, RaceGroupSeriesDTO series, Timer timerForClientServerOffset, EventPlaceNavigator pageNavigator) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        if(series.getName().equals(LeaderboardNameConstants.DEFAULT_SERIES_NAME)) {
            phaseName.setInnerText(TextMessages.INSTANCE.races());
        } else {
            phaseName.setInnerText(series.getName());
        }
        
        int numberOfFleets = series.getFleets().size();
        if(numberOfFleets > 1) {
            if(series.hasOrderedFleets()) { 
                for(FleetDTO fleet: series.getFleets()) {
                    // ordered fleets are splitted into different fleet groups -> e.g. Gold Silver
                    List<FleetDTO> fleetsToShow = new ArrayList<FleetDTO>();
                    fleetsToShow.add(fleet);
                    EventRegattaRacesFleetGroup fleetGroup = new EventRegattaRacesFleetGroup(leaderboard, series, fleetsToShow, timerForClientServerOffset, pageNavigator); 
                    fleetGroupsPanel.appendChild(fleetGroup.getElement());
                }
            } else {
                // unordered fleets are NOT splitted into different fleet groups -> e.g. Blue, Yellow
                List<FleetDTO> fleetsToShow = new ArrayList<FleetDTO>(series.getFleets());
                EventRegattaRacesFleetGroup fleetGroup = new EventRegattaRacesFleetGroup(leaderboard, series, fleetsToShow, timerForClientServerOffset, pageNavigator); 
                fleetGroupsPanel.appendChild(fleetGroup.getElement());
            }
        } else {
            // single fleet
            EventRegattaRacesFleetGroup fleetGroup = new EventRegattaRacesFleetGroup(leaderboard, series, timerForClientServerOffset, pageNavigator); 
            fleetGroupsPanel.appendChild(fleetGroup.getElement());
        }
    }
    
}

package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaRacesFleetGroup extends Composite {
    private static EventRegattaRacesFleetGroupUiBinder uiBinder = GWT.create(EventRegattaRacesFleetGroupUiBinder.class);

    interface EventRegattaRacesFleetGroupUiBinder extends UiBinder<Widget, EventRegattaRacesFleetGroup> {
    }

    @UiField DivElement fleetsPanel;
    @UiField HTMLPanel racesFleetPanel;
    
    public EventRegattaRacesFleetGroup(StrippedLeaderboardDTO leaderboard, RaceGroupSeriesDTO series, List<FleetDTO> fleetsToShow, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        for(FleetDTO fleet: fleetsToShow) {
            EventRegattaRacesFleet eventRegattaRacesFleet = new EventRegattaRacesFleet(fleet);
            fleetsPanel.appendChild(eventRegattaRacesFleet.getElement());
            
            List<RaceColumnDTO> racesOfFleet = getRacesOfFleet(leaderboard, series, fleet);
            for(RaceColumnDTO raceColumn: racesOfFleet) {
                RaceColumnDTO raceColumnFromLeaderboard = leaderboard.getRaceColumnByName(raceColumn.getName());
                EventRegattaRacesRace race = new EventRegattaRacesRace(leaderboard, fleet, raceColumnFromLeaderboard, timerForClientServerOffset, pageNavigator);
                racesFleetPanel.add(race);
            }
        }
    }
    
    private List<RaceColumnDTO> getRacesOfFleet(StrippedLeaderboardDTO leaderboard, RaceGroupSeriesDTO series, FleetDTO fleet) {
        List<RaceColumnDTO> racesColumnsOfFleet = new ArrayList<RaceColumnDTO>();
        for (RaceColumnDTO raceColumn : series.getRaceColumns()) {
            for (FleetDTO fleetOfRaceColumn : series.getFleets()) {
                if (fleet.equals(fleetOfRaceColumn)) {
                    // We have to get the race column from the leaderboard, because the race column of the series
                    // have no tracked race and would be displayed as inactive race.
                    racesColumnsOfFleet.add(leaderboard.getRaceColumnByName(raceColumn.getName()));
                }
            }
        }
        return racesColumnsOfFleet;
    }

}

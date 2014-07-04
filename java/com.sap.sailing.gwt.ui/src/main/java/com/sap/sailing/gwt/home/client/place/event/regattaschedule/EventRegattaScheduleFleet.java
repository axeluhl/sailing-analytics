package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaScheduleFleet extends Composite {
    private static EventRegattaScheduleSeriesUiBinder uiBinder = GWT.create(EventRegattaScheduleSeriesUiBinder.class);

    interface EventRegattaScheduleSeriesUiBinder extends UiBinder<Widget, EventRegattaScheduleFleet> {
    }

    @SuppressWarnings("unused")
    private final SeriesDTO series;

    @SuppressWarnings("unused")
    private final FleetDTO fleet;

    @UiField DivElement fleetDiv;
    @UiField SpanElement fleetName;
    @UiField HTMLPanel racesListPanel;
    
    public EventRegattaScheduleFleet(StrippedLeaderboardDTO leaderboard, SeriesDTO series, FleetDTO fleet,
            Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        this.series = series;
        this.fleet = fleet;
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        fleetName.setInnerText(fleet.getName());
        fleetDiv.getStyle().setBackgroundColor(fleet.getColor().getAsHtml());
        
        List<RaceColumnDTO> racesOfFleet = getRacesOfFleet(leaderboard, series, fleet);
        for(RaceColumnDTO raceColumn: racesOfFleet) {
            EventRegattaScheduleRace eventRegattaScheduleRace = new EventRegattaScheduleRace(leaderboard, fleet, raceColumn, timerForClientServerOffset, pageNavigator);
            racesListPanel.add(eventRegattaScheduleRace);
        }
    }
    
    private List<RaceColumnDTO> getRacesOfFleet(StrippedLeaderboardDTO leaderboard, SeriesDTO series, FleetDTO fleet) {
        List<RaceColumnDTO> racesColumnsOfFleet = new ArrayList<RaceColumnDTO>();
        int raceColumnCounter = 1;
        for (RaceColumnDTO raceColumn : series.getRaceColumns()) {
            boolean skipCarryForwardColumn = series.isFirstColumnIsNonDiscardableCarryForward(); 
            for (FleetDTO fleetOfRaceColumn : series.getFleets()) {
                if (fleet.equals(fleetOfRaceColumn) && !(skipCarryForwardColumn == true && raceColumnCounter == 1)) {
                    // We have to get the race column from the leaderboard, because the race column of the series
                    // have no tracked race and would be displayed as inactive race.
                    racesColumnsOfFleet.add(leaderboard.getRaceColumnByName(raceColumn.getName()));
                }
            }
            raceColumnCounter++;
        }
        return racesColumnsOfFleet;
    }
}

package com.sap.sailing.gwt.home.client.place.event2.partials.regattaraces;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.home.client.place.event2.EventView;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class EventRegattaRacesFleetGroup extends Composite {
    private static EventRegattaRacesFleetGroupUiBinder uiBinder = GWT.create(EventRegattaRacesFleetGroupUiBinder.class);

    interface EventRegattaRacesFleetGroupUiBinder extends UiBinder<Widget, EventRegattaRacesFleetGroup> {
    }

    @UiField DivElement fleetsPanel;
    @UiField DivElement racesFromToDate;
    @UiField DivElement racesFleetPanel;
    
    public EventRegattaRacesFleetGroup(StrippedLeaderboardDTO leaderboard, RaceGroupSeriesDTO series, List<FleetDTO> fleetsToShow, EventView.Presenter presenter) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        for(FleetDTO fleet: fleetsToShow) {
            EventRegattaRacesFleet eventRegattaRacesFleet = new EventRegattaRacesFleet(fleet, 0);
            fleetsPanel.appendChild(eventRegattaRacesFleet.getElement());

            updateFleetRacesUI(leaderboard, series, fleet, presenter);
        }
    }

    public EventRegattaRacesFleetGroup(StrippedLeaderboardDTO leaderboard, RaceGroupSeriesDTO series, EventView.Presenter presenter) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        fleetsPanel.getStyle().setDisplay(Display.NONE);

        FleetDTO defaultFleet = series.getFleets().get(0);
        updateFleetRacesUI(leaderboard, series, defaultFleet, presenter);
    }

    private void updateFleetRacesUI(StrippedLeaderboardDTO leaderboard, RaceGroupSeriesDTO series, FleetDTO fleet, EventView.Presenter presenter) {
        Date fromDate = null;
        Date toDate = null;

        List<RaceColumnDTO> racesOfFleet = getRacesOfFleet(leaderboard, series, fleet);
        for(RaceColumnDTO raceColumn: racesOfFleet) {
            RaceColumnDTO raceColumnFromLeaderboard = leaderboard.getRaceColumnByName(raceColumn.getName());
            EventRegattaRacesRace race = new EventRegattaRacesRace(leaderboard, fleet, raceColumnFromLeaderboard, presenter);
            racesFleetPanel.appendChild(race.getElement());
            
            Date startDate = raceColumnFromLeaderboard.getStartDate(fleet);
            if(startDate != null) {
                if(fromDate == null || startDate.before(fromDate)) {
                    fromDate = startDate;
                }
                if(toDate == null || startDate.after(toDate)) {
                    toDate = startDate;
                }
            }
        }
        if(fromDate != null && toDate != null) {
            racesFromToDate.setInnerText(EventDatesFormatterUtil.formatDateRangeWithYear(fromDate, toDate));
        } else {
            // no single race with a date
            racesFromToDate.getStyle().setDisplay(Display.NONE);
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

package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;

public class EventRegattaRacesRace extends Composite {
    private static EventRegattaRacesRaceUiBinder uiBinder = GWT.create(EventRegattaRacesRaceUiBinder.class);

    interface EventRegattaRacesRaceUiBinder extends UiBinder<Widget, EventRegattaRacesRace> {
    }

    @UiField DivElement fleetColor;
    
    // TODO will be used soon:
//    private final DateTimeFormat raceTimeFormat = DateTimeFormat.getFormat("EEE, h:mm a");

    public EventRegattaRacesRace(FleetDTO fleet, RaceColumnDTO raceColumn, EventPageNavigator pageNavigator) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        fleetColor.getStyle().setBackgroundColor(fleet.getColor().getAsHtml());
        
//        raceName.setInnerText(raceColumn.getName());
//
//        RaceDTO race = raceColumn.getRace(fleet);
//        if(race != null) {
//            boolean live = raceColumn.isLive(fleet, timerForClientServerOffset.getLiveTimePointInMillis());
//            
//            // tracked race
//            raceBox.getElement().addClassName(css.eventregattaschedule_series_fleet_racetracked());
//            if(race.trackedRace != null) {
//                if(race.trackedRace.startOfTracking != null) {
//                    raceDetails.setInnerText(raceTimeFormat.format(race.trackedRace.startOfTracking));
//                } else {
//                    raceDetails.setInnerText("tracked");
//                }
//                if(live) {
//                    raceBox.getElement().setAttribute("data-live", "data-live");
//                }
//            }
//        } else {
//            raceBox.getElement().addClassName(css.eventregattaschedule_series_fleet_raceuntracked());
//            raceDetails.setInnerText("untracked");
//        }

    }

//    @UiHandler("raceLink")
//    public void goToRaceboard(ClickEvent e) {
//        RaceDTO race = raceColumn.getRace(fleet);
//        if(race != null && race.trackedRace != null) {
//            pageNavigator.openRaceViewer(leaderboard, race);
//        }
//    }
}

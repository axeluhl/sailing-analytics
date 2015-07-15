package com.sap.sailing.gwt.home.client.place.event.partials;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.common.client.LinkUtil;
import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class RegattaPhase extends UIObject {
    private static RegattaPhaseUiBinder uiBinder = GWT.create(RegattaPhaseUiBinder.class);

    interface RegattaPhaseUiBinder extends UiBinder<DivElement, RegattaPhase> {
    }

    @UiField SpanElement phaseName;
    @UiField DivElement phaseRacesDiv;
    @UiField AnchorElement phaseRacesAnchor;
    
    public RegattaPhase(RaceGroupSeriesDTO series, final LeaderboardGroupDTO leaderboardGroup,
            final StrippedLeaderboardDTO leaderboard, final RaceGroupDTO raceGroup, final EventView.Presenter presenter) {
        RegattaResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));

        if(LeaderboardNameConstants.DEFAULT_SERIES_NAME.equals(series.getName())) {
            phaseName.setInnerText("Races");
        } else {
            phaseName.setInnerText(series.getName());
        }
        
        for(RaceColumnDTO raceColumn: series.getRaceColumns()) {
            // the raceColumn from the RaceGroupSeriesDTO does not contain all required information for the race state.
            // therefore we take it from the LeaderboardDTO
            // TODO: try to have only one RaceColumnDTO 
            RaceColumnDTO raceColumn2 = leaderboard.getRaceColumnByName(raceColumn.getName());
            RegattaPhaseRace race = new RegattaPhaseRace(raceColumn2, presenter.getTimerForClientServerOffset());
            phaseRacesDiv.appendChild(race.getElement());
            
            final PlaceNavigation<RegattaRacesPlace> racesPlaceNavigation = presenter.getRegattaRacesNavigation(leaderboard.regattaName);
            phaseRacesAnchor.setHref(racesPlaceNavigation.getTargetUrl());
            Event.sinkEvents(phaseRacesAnchor, Event.ONCLICK);
            Event.setEventListener(phaseRacesAnchor, new EventListener() {
                @Override
                public void onBrowserEvent(Event browserEvent) {
                    if(LinkUtil.handleLinkClick(browserEvent)) {
                        browserEvent.preventDefault();
                        racesPlaceNavigation.goToPlace();
                    }
                }
            });
        }
    }
    
    public RegattaPhase() {
        RegattaResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        phaseName.setInnerText("No series defined yet.");
    }
}

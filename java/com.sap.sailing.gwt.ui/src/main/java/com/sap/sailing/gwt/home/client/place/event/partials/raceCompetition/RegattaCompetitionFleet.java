package com.sap.sailing.gwt.home.client.place.event.partials.raceCompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;

public class RegattaCompetitionFleet extends Widget {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Element, RegattaCompetitionFleet> {
    }
    
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;
    @UiField DivElement competitorCountUi;
    @UiField DivElement racesContainerUi;

    public RegattaCompetitionFleet(RaceListFleetDTO fleet) {
        setElement(uiBinder.createAndBindUi(this));
        this.competitorCountUi.setInnerText(fleet.getCompetitorCount() + " Comp. TODO");
        if (fleet.getFleet() != null) {
            fleetNameUi.setInnerText(fleet.getFleet().getFleetName());
            fleetCornerUi.getStyle().setProperty("borderTopColor", fleet.getFleet().getFleetColor());
        }
    }
    
    public void addRace(RaceListRaceDTO race, String raceViewerURL) {
        RegattaCompetitionFleetRace competitionRace = new RegattaCompetitionFleetRace(race, raceViewerURL);
        racesContainerUi.appendChild(competitionRace.getElement());
    }

}

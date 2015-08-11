package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetitionResources.LocalCss;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;

public class RegattaCompetitionFleet extends Widget {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Element, RegattaCompetitionFleet> {
    }
    
    private static final LocalCss CSS = RegattaCompetitionResources.INSTANCE.css();
    
    @UiField DivElement raceContainerUi;
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;

    public RegattaCompetitionFleet(RaceListFleetDTO fleet, int fleetCount) {
        setElement(uiBinder.createAndBindUi(this));
        raceContainerUi.getStyle().setBackgroundColor("rgba("+ fleet.getFleet().getFleetColor() + ", .1)");
        raceContainerUi.getStyle().setWidth(100.0 / fleetCount, Unit.PCT);
        if (fleetCount < 2) raceContainerUi.addClassName(CSS.regattacompetition_phase_fleetfullwidth());
        fleetCornerUi.getStyle().setProperty("borderTopColor", fleet.getFleet().getFleetColor());
        fleetNameUi.setInnerText(fleet.getFleet().getFleetName());
    }
    
    public void addRace(RaceListRaceDTO race, String raceViewerUrl) {
        RegattaCompetitionFleetRace fleetRace = new RegattaCompetitionFleetRace(race, raceViewerUrl);
        raceContainerUi.appendChild(fleetRace.getElement());
    }
    
}

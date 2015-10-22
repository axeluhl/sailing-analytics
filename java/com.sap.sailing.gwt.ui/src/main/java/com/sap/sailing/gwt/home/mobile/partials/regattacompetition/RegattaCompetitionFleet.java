package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.race.FleetMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetitionResources.LocalCss;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.RGBColor;

public class RegattaCompetitionFleet extends Widget {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Element, RegattaCompetitionFleet> {
    }
    
    private static final LocalCss CSS = RegattaCompetitionResources.INSTANCE.css();
    
    @UiField DivElement raceContainerUi;
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;

    public RegattaCompetitionFleet(RaceCompetitionFormatFleetDTO fleet, int fleetCount) {
        setElement(uiBinder.createAndBindUi(this));
        raceContainerUi.getStyle().setWidth(100.0 / fleetCount, Unit.PCT);
        if (fleetCount < 2) raceContainerUi.addClassName(CSS.regattacompetition_phase_fleetfullwidth());
        fleetCornerUi.getStyle().setProperty("borderTopColor", fleet.getFleet().getFleetColor());
        fleetNameUi.setInnerText(fleet.getFleet().getFleetName());
        if (fleet.getFleet().isDefaultFleet()) {
            fleetCornerUi.removeFromParent();
            fleetNameUi.removeFromParent();
        } else {
            raceContainerUi.getStyle().setBackgroundColor(getBackgroundColor(fleet.getFleet()));
        }
    }
    
    public void addRace(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        RegattaCompetitionFleetRace fleetRace = new RegattaCompetitionFleetRace(race, raceViewerUrl);
        raceContainerUi.appendChild(fleetRace.getElement());
    }
    
    private String getBackgroundColor(FleetMetadataDTO fleet) {
        Triple<Integer, Integer, Integer> rgbValues = new RGBColor(fleet.getFleetColor()).getAsRGB();
        return "rgba(" + rgbValues.getA() + "," + rgbValues.getB() + "," + rgbValues.getC() + ", 0.1)";
    }
    
}

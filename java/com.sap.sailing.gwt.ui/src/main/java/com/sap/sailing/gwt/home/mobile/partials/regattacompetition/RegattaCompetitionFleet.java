package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.gwt.home.mobile.partials.regattacompetition.RegattaCompetitionResources.LocalCss;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.impl.RGBColor;

public class RegattaCompetitionFleet extends Widget {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Element, RegattaCompetitionFleet> {
    }
    
    private static final LocalCss CSS = RegattaCompetitionResources.INSTANCE.css();
    
    @UiField DivElement raceContainerUi;
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;
    
    private Map<RegattaCompetitionFleetRace, SimpleRaceMetadataDTO> raceWidgetToDtoMap = new HashMap<>();

    public RegattaCompetitionFleet(RaceCompetitionFormatFleetDTO fleet, int fleetCount) {
        setElement(uiBinder.createAndBindUi(this));
        raceContainerUi.getStyle().setBackgroundColor(getBackgroundColor(fleet.getFleet()));
        updateFleetWidth(fleetCount);
        fleetCornerUi.getStyle().setProperty("borderTopColor", fleet.getFleet().getFleetColor());
        fleetNameUi.setInnerText(fleet.getFleet().getFleetName());
        if (LeaderboardNameConstants.DEFAULT_FLEET_NAME.equals(fleet.getFleet().getFleetName())) {
            fleetCornerUi.removeFromParent();
            fleetNameUi.removeFromParent();
        }
    }
    
    void updateFleetWidth(int fleetCount) {
        raceContainerUi.getStyle().setWidth(100.0 / fleetCount, Unit.PCT);
        setStyleName(raceContainerUi, CSS.regattacompetition_phase_fleetfullwidth(), fleetCount < 2);
    }
    
    public void addRace(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        RegattaCompetitionFleetRace fleetRace = new RegattaCompetitionFleetRace(race, raceViewerUrl);
        raceWidgetToDtoMap.put(fleetRace, race);
        raceContainerUi.appendChild(fleetRace.getElement());
    }
    
    private String getBackgroundColor(FleetMetadataDTO fleet) {
        Triple<Integer, Integer, Integer> rgbValues = new RGBColor(fleet.getFleetColor()).getAsRGB();
        return "rgba(" + rgbValues.getA() + "," + rgbValues.getB() + "," + rgbValues.getC() + ", 0.1)";
    }

    public boolean applyFilter(Filter<SimpleRaceMetadataDTO> racesFilter) {
        boolean fleetVisible = false;
        for (Entry<RegattaCompetitionFleetRace, SimpleRaceMetadataDTO> entry : raceWidgetToDtoMap.entrySet()) {
            boolean raceVisible = racesFilter.matches(entry.getValue());
            entry.getKey().getElement().getStyle().setDisplay(raceVisible ? Display.BLOCK : Display.NONE);
            fleetVisible |= raceVisible;
        }
        setVisible(fleetVisible);
        return fleetVisible;
    }
    
}

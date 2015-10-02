package com.sap.sailing.gwt.home.desktop.partials.regattacompetition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.impl.RGBColor;

public class RegattaCompetitionFleet extends Widget {

    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Element, RegattaCompetitionFleet> {
    }
    
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;
    @UiField DivElement competitorCountUi;
    @UiField DivElement racesContainerUi;
    
    private Map<RegattaCompetitionFleetRace, Collection<SimpleCompetitorDTO>> raceToCompetitorsMap = new HashMap<>();

    public RegattaCompetitionFleet(RaceCompetitionFormatFleetDTO fleet) {
        setElement(uiBinder.createAndBindUi(this));
        getElement().getStyle().setBackgroundColor(getBackgroundColor(fleet.getFleet()));
        this.competitorCountUi.setInnerText(I18N.competitorsCount(fleet.getCompetitorCount()));
        this.competitorCountUi.getStyle().setDisplay(fleet.getCompetitorCount() == 0 ? Display.NONE : Display.BLOCK);
        if (fleet.getFleet() != null) {
            fleetNameUi.setInnerText(fleet.getFleet().getFleetName());
            fleetCornerUi.getStyle().setProperty("borderTopColor", fleet.getFleet().getFleetColor());
            if (LeaderboardNameConstants.DEFAULT_FLEET_NAME.equals(fleet.getFleet().getFleetName())) {
                addStyleName(RegattaCompetitionResources.INSTANCE.css().default_fleet());
            }
        }
    }
    
    public void addRace(SimpleRaceMetadataDTO race, String raceViewerURL) {
        RegattaCompetitionFleetRace competitionRace = new RegattaCompetitionFleetRace(race, raceViewerURL);
        raceToCompetitorsMap.put(competitionRace, race.getCompetitors());
        racesContainerUi.appendChild(competitionRace.getElement());
    }
    
    private String getBackgroundColor(FleetMetadataDTO fleet) {
        Triple<Integer, Integer, Integer> rgbValues = new RGBColor(fleet.getFleetColor()).getAsRGB();
        return "rgba(" + rgbValues.getA() + "," + rgbValues.getB() + "," + rgbValues.getC() + ", 0.1)";
    }
    
    public boolean setCompetitorFilter(Filter<Collection<SimpleCompetitorDTO>> competitorFilter) {
        boolean fleetVisible = false;
        for (Entry<RegattaCompetitionFleetRace, Collection<SimpleCompetitorDTO>> entry : raceToCompetitorsMap.entrySet()) {
            boolean raceVisible = competitorFilter.matches(entry.getValue());
            entry.getKey().setVisible(raceVisible);
            fleetVisible |= raceVisible;
        }
        setVisible(fleetVisible);
        return fleetVisible;
    }
    
}

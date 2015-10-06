package com.sap.sailing.gwt.home.desktop.partials.regattacompetition;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.AbstractRegattaCompetitionFleet;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionRaceView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sse.common.filter.Filter;

public class RegattaCompetitionFleet extends AbstractRegattaCompetitionFleet {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Element, RegattaCompetitionFleet> {
    }
    
    @UiField RegattaCompetitionResources local_res;
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;
    @UiField DivElement competitorCountUi;
    @UiField DivElement racesContainerUi;
    
    private Map<RegattaCompetitionFleetRace, SimpleRaceMetadataDTO> raceWidgetToDtoMap = new HashMap<>();

    public RegattaCompetitionFleet(RaceCompetitionFormatFleetDTO fleet) {
        super(fleet);
        this.competitorCountUi.setInnerText(StringMessages.INSTANCE.competitorsCount(fleet.getCompetitorCount()));
        this.competitorCountUi.getStyle().setDisplay(fleet.getCompetitorCount() == 0 ? Display.NONE : Display.BLOCK);
    }
    
    public boolean applyFilter(Filter<SimpleRaceMetadataDTO> racesFilter) {
        boolean fleetVisible = false;
        for (Entry<RegattaCompetitionFleetRace, SimpleRaceMetadataDTO> entry : raceWidgetToDtoMap.entrySet()) {
            boolean raceVisible = racesFilter.matches(entry.getValue());
            entry.getKey().setVisible(raceVisible);
            fleetVisible |= raceVisible;
        }
        setVisible(fleetVisible);
        return fleetVisible;
    }

    @Override
    public RegattaCompetitionRaceView addRaceView(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        RegattaCompetitionFleetRace raceView = new RegattaCompetitionFleetRace(race, raceViewerUrl);
        racesContainerUi.appendChild(raceView.getElement());
        return raceView;
    }

    @Override
    public void setNumberOfFleetsInSeries(int fleetCount) {
    }

    @Override
    protected void onDefaultFleetName() {
        addStyleName(local_res.css().default_fleet());
    }

    @Override
    protected Element getMainUiElement() {
        return uiBinder.createAndBindUi(this);
    }

    @Override
    protected Element getFleetNameUiElement() {
        return fleetNameUi;
    }

    @Override
    protected Element getFleetCornerUiElement() {
        return fleetCornerUi;
    }
    
}

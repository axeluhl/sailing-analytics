package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.AbstractRegattaCompetitionFleet;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionRaceView;

public class RegattaCompetitionFleet extends AbstractRegattaCompetitionFleet {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Element, RegattaCompetitionFleet> {
    }
    
    @UiField RegattaCompetitionResources local_res;
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;
    @UiField DivElement raceContainerUi;
    
    public RegattaCompetitionFleet(RaceCompetitionFormatFleetDTO fleet) {
        super(fleet);
    }
    
    @Override
    public RegattaCompetitionRaceView addRaceView(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        RegattaCompetitionFleetRace raceView = new RegattaCompetitionFleetRace(race, raceViewerUrl);
        raceContainerUi.appendChild(raceView.getElement());
        return raceView;
    }

    @Override
    public void setNumberOfFleetsInSeries(int fleetCount) {
        raceContainerUi.getStyle().setWidth(100.0 / fleetCount, Unit.PCT);
        setStyleName(raceContainerUi, local_res.css().regattacompetition_phase_fleetfullwidth(), fleetCount < 2);
    }

    @Override
    protected void onDefaultFleetName() {
        fleetCornerUi.removeFromParent();
        fleetNameUi.removeFromParent();
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

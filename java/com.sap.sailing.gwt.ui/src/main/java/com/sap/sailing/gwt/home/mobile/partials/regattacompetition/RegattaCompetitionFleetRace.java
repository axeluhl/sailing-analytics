package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.AbstractRegattaCompetitionFleetRace;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceTrackingState;

public class RegattaCompetitionFleetRace extends AbstractRegattaCompetitionFleetRace {

    private static RegattaCompetitionFleetRaceUiBinder uiBinder = GWT.create(RegattaCompetitionFleetRaceUiBinder.class);

    interface RegattaCompetitionFleetRaceUiBinder extends UiBinder<AnchorElement, RegattaCompetitionFleetRace> {
    }
    
    @UiField RegattaCompetitionResources local_res;
    @UiField DivElement raceNameUi;
    @UiField DivElement raceStateUi;
    @UiField DivElement raceDateUi;

    public RegattaCompetitionFleetRace(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        super(race, raceViewerUrl);
    }

    @Override
    public void doFilter(boolean filter) {
        getElement().getStyle().setDisplay(filter ? Display.NONE : Display.BLOCK);
    }

    @Override
    protected AnchorElement getMainUiElement() {
        return uiBinder.createAndBindUi(this);
    }

    @Override
    protected Element getRaceNameUiElement() {
        return raceNameUi;
    }
    
    @Override
    protected Element getRaceStateUiElement() {
        return raceStateUi;
    }

    @Override
    protected Element getRaceDateUiElement() {
        return raceDateUi;
    }

    @Override
    protected String getRaceLiveStyleName() {
        return local_res.css().regattacompetition_phase_fleet_racelive();
    }

    @Override
    protected String getRacePlannedStyleName() {
        return local_res.css().regattacompetition_phase_fleet_raceplanned();
    }

    @Override
    protected String getRaceUntrackedStyleName() {
        return local_res.css().regattacompetition_phase_fleet_raceuntracked();
    }
    
    @Override
    protected boolean isUntrackedRace(RaceTrackingState trackingState) {
        return true;
    }
    
}

package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.AbstractRegattaCompetitionFleetRace;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionPresenter;

public class RegattaCompetitionFleetRace extends AbstractRegattaCompetitionFleetRace {

    private static RegattaCompetitionFleetRaceUiBinder uiBinder = GWT.create(RegattaCompetitionFleetRaceUiBinder.class);

    interface RegattaCompetitionFleetRaceUiBinder extends UiBinder<Element, RegattaCompetitionFleetRace> {
    }
    
    @UiField RegattaCompetitionResources local_res;
    @UiField DivElement raceNameUi;
    @UiField DivElement raceStateUi;
    @UiField DivElement raceDateUi;

    public RegattaCompetitionFleetRace(SimpleRaceMetadataDTO race, RegattaCompetitionPresenter presenter) {
        super(race, presenter);
    }

    @Override
    public void doFilter(boolean filter) {
        getElement().getStyle().setDisplay(filter ? Display.NONE : Display.BLOCK);
    }

    @Override
    protected Element getMainUiElement() {
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

    public void removeBigRaceTitleCSS() {
        raceNameUi.removeClassName(local_res.css().regattacompetition_phase_fleet_race_title_big());
    }
    
}

package com.sap.sailing.gwt.home.desktop.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
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
        setVisible(!filter);
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
        return local_res.css().fleet_races_racelive();
    }

    @Override
    protected String getRacePlannedStyleName() {
        return local_res.css().fleet_races_raceplanned();
    }

    @Override
    protected String getRaceUntrackedStyleName() {
        return local_res.css().fleet_races_raceuntracked();
    }
}

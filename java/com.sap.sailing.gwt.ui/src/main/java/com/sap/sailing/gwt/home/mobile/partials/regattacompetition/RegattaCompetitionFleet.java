package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.AbstractRegattaCompetitionFleet;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionPresenter;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionRaceView;

public class RegattaCompetitionFleet extends AbstractRegattaCompetitionFleet {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);
    
    private int fleetCount = -1;
    private RegattaCompetitionFleetRace raceView;

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Widget, RegattaCompetitionFleet> {
    }
    
    @UiField RegattaCompetitionResources local_res;
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;
    @UiField FlowPanel raceContainerUi;
    
    public RegattaCompetitionFleet(RaceCompetitionFormatFleetDTO fleet) {
        super(fleet);
    }
    
    @Override
    public RegattaCompetitionRaceView addRaceView(SimpleRaceMetadataDTO race, RegattaCompetitionPresenter presenter) {
        raceView = new RegattaCompetitionFleetRace(race, presenter);
        raceContainerUi.add(raceView);
        updateRaceViewIfReady();
        return raceView;
    }

    @Override
    public void setNumberOfFleetsInSeries(int fleetCount) {
        getElement().getStyle().setWidth(100.0 / fleetCount, Unit.PCT);
        setStyleName(local_res.css().regattacompetition_phase_fleetfullwidth(), fleetCount < 2);
        setStyleName(local_res.css().regattacompetition_phase_fleetcompact(), fleetCount > 4);
        this.fleetCount = fleetCount;
        updateRaceViewIfReady();
    }

    @Override
    protected void onDefaultFleetName() {
        fleetCornerUi.removeFromParent();
        fleetNameUi.removeFromParent();
    }

    @Override
    protected Widget getMainUiElement() {
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

    private void updateRaceViewIfReady() {
        if (raceView != null && fleetCount > 1) {
            raceView.removeBigRaceTitleCSS();
        }
    }
}

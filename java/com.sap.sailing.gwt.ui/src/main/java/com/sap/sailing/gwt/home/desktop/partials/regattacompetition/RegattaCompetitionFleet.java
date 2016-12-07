package com.sap.sailing.gwt.home.desktop.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.AbstractRegattaCompetitionFleet;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionPresenter;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionRaceView;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RegattaCompetitionFleet extends AbstractRegattaCompetitionFleet {

    private static RegattaCompetitionFleetUiBinder uiBinder = GWT.create(RegattaCompetitionFleetUiBinder.class);

    interface RegattaCompetitionFleetUiBinder extends UiBinder<Widget, RegattaCompetitionFleet> {
    }
    
    @UiField RegattaCompetitionResources local_res;
    @UiField DivElement fleetCornerUi;
    @UiField DivElement fleetNameUi;
    @UiField DivElement competitorCountUi;
    @UiField FlowPanel racesContainerUi;
    
    public RegattaCompetitionFleet(RaceCompetitionFormatFleetDTO fleet) {
        super(fleet);
        this.competitorCountUi.setInnerText(StringMessages.INSTANCE.competitorsCount(fleet.getCompetitorCount()));
        this.competitorCountUi.getStyle().setDisplay(fleet.getCompetitorCount() == 0 ? Display.NONE : Display.BLOCK);
    }
    
    @Override
    public RegattaCompetitionRaceView addRaceView(SimpleRaceMetadataDTO race, RegattaCompetitionPresenter presenter) {
        RegattaCompetitionFleetRace raceView = new RegattaCompetitionFleetRace(race, presenter);
        racesContainerUi.add(raceView);
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
    
}

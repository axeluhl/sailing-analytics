package com.sap.sailing.gwt.home.client.place.event.partials.raceCompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceTrackingState;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceViewState;

public class RegattaCompetitionFleetRace extends UIObject {

    private static RegattaCompetitionFleetRaceUiBinder uiBinder = GWT.create(RegattaCompetitionFleetRaceUiBinder.class);

    interface RegattaCompetitionFleetRaceUiBinder extends UiBinder<AnchorElement, RegattaCompetitionFleetRace> {
    }
    
    @UiField RegattaCompetitionResources local_res;
    @UiField DivElement raceNameUi;
    @UiField DivElement raceDateUi;
    private final AnchorElement anchor;

    public RegattaCompetitionFleetRace(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        anchor = uiBinder.createAndBindUi(this);
        anchor.setHref(raceViewerUrl);
        local_res.css().ensureInjected();
        setupRaceState(race.getTrackingState(), race.getViewState());
        this.raceNameUi.setInnerText(race.getRaceName());
        if (race.getStart() != null) {
            this.raceDateUi.setInnerText(DateAndTimeFormatterUtil.defaultDateFormatter.render(race.getStart())); 
        }
        setElement(anchor);
    }
    
    private void setupRaceState(RaceTrackingState trackingState, RaceViewState viewState) {
        if (viewState == RaceViewState.RUNNING) {
             anchor.addClassName(local_res.css().fleet_races_racelive());
        } else if (viewState == RaceViewState.PLANNED || viewState == RaceViewState.SCHEDULED) {
             anchor.addClassName(local_res.css().fleet_races_raceplanned());
        }
        if (trackingState != RaceTrackingState.TRACKED_VALID_DATA) {
             anchor.addClassName(local_res.css().fleet_races_raceuntracked());
        }
    }
}

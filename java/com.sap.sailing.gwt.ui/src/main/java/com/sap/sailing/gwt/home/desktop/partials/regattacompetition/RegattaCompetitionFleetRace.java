package com.sap.sailing.gwt.home.desktop.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceTrackingState;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceViewState;

public class RegattaCompetitionFleetRace extends UIObject {

    private static RegattaCompetitionFleetRaceUiBinder uiBinder = GWT.create(RegattaCompetitionFleetRaceUiBinder.class);

    interface RegattaCompetitionFleetRaceUiBinder extends UiBinder<AnchorElement, RegattaCompetitionFleetRace> {
    }
    
    @UiField RegattaCompetitionResources local_res;
    @UiField StringMessages i18n;
    @UiField DivElement raceNameUi;
    @UiField DivElement raceStateUi;
    @UiField DivElement raceDateUi;
    private final AnchorElement anchorUi;

    public RegattaCompetitionFleetRace(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        anchorUi = uiBinder.createAndBindUi(this);
        if (raceViewerUrl != null) {
            anchorUi.setTarget("_blank");
            anchorUi.setHref(raceViewerUrl);
        }
        setupRaceState(race.getTrackingState(), race.getViewState());
        this.raceNameUi.setInnerText(race.getRaceName());
        if (race.getStart() != null) {
            this.raceDateUi.setInnerText(DateAndTimeFormatterUtil.weekdayMonthAbbrDayDateFormatter.render(race.getStart())); 
        }
        setElement(anchorUi);
    }
    
    private void setupRaceState(RaceTrackingState trackingState, RaceViewState viewState) {
        boolean isUntrackedRace = trackingState != RaceTrackingState.TRACKED_VALID_DATA;
        if (viewState == RaceViewState.RUNNING) {
            anchorUi.addClassName(local_res.css().fleet_races_racelive());
            raceStateUi.setInnerText(isUntrackedRace ? i18n.live() : i18n.actionWatch());
        } else if (viewState == RaceViewState.FINISHED) {
            raceStateUi.setInnerText(isUntrackedRace ? i18n.raceIsFinished() : i18n.actionAnalyze());
        } else {
            anchorUi.addClassName(local_res.css().fleet_races_raceplanned());
            if (viewState == RaceViewState.SCHEDULED) raceStateUi.setInnerText(i18n.raceIsPlanned());
            else raceStateUi.setInnerText(viewState.getLabel());
        }
        setStyleName(anchorUi, local_res.css().fleet_races_raceuntracked(), isUntrackedRace);
    }
}

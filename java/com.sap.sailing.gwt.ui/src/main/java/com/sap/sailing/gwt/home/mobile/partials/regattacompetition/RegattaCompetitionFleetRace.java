package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

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
    @UiField DivElement raceDateUi;
    @UiField DivElement raceStateUi;
    private final AnchorElement anchor;

    public RegattaCompetitionFleetRace(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        setElement(anchor = uiBinder.createAndBindUi(this));
        if (raceViewerUrl != null) this.anchor.setHref(raceViewerUrl);
        local_res.css().ensureInjected();
        setupRaceState(race.getTrackingState(), race.getViewState());
        this.raceNameUi.setInnerText(race.getRaceName());
        if (race.getStart() != null) {
            this.raceDateUi.setInnerText(DateAndTimeFormatterUtil.weekdayMonthAbbrDayDateFormatter.render(race.getStart())); 
        }
    }
    
    private void setupRaceState(RaceTrackingState trackingState, RaceViewState viewState) {
        boolean isUntrackedRace = trackingState != RaceTrackingState.TRACKED_VALID_DATA;
        if (viewState == RaceViewState.RUNNING) {
            anchor.addClassName(local_res.css().regattacompetition_phase_fleet_racelive());
            raceStateUi.setInnerText(isUntrackedRace ? i18n.live() : i18n.actionWatch());
        } else if (viewState == RaceViewState.FINISHED) {
            raceStateUi.setInnerText(isUntrackedRace ? i18n.raceIsFinished() : i18n.actionAnalyze());
        } else {
            anchor.addClassName(local_res.css().regattacompetition_phase_fleet_raceplanned());
            if (viewState == RaceViewState.SCHEDULED) raceStateUi.setInnerText(i18n.raceIsPlanned());
            else raceStateUi.setInnerText(viewState.getLabel());
        }
        setStyleName(anchor, local_res.css().regattacompetition_phase_fleet_raceuntracked(), isUntrackedRace);
    }

}

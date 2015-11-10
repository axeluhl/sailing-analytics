package com.sap.sailing.gwt.home.shared.partials.regattacompetition;

import static com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil.shortTimeFormatter;
import static com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil.weekdayMonthAbbrDayDateFormatter;
import static com.sap.sse.common.impl.MillisecondsTimePoint.now;

import java.util.Date;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO.RaceTrackingState;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO.RaceViewState;
import com.sap.sailing.gwt.home.shared.partials.regattacompetition.RegattaCompetitionView.RegattaCompetitionRaceView;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public abstract class AbstractRegattaCompetitionFleetRace extends UIObject implements RegattaCompetitionRaceView {
    
    private static final StringMessages I18N = StringMessages.INSTANCE;
    protected final AnchorElement anchorUiElement;

    protected AbstractRegattaCompetitionFleetRace(SimpleRaceMetadataDTO race, String raceViewerUrl) {
        this.anchorUiElement = getMainUiElement();
        if (raceViewerUrl != null) {
            anchorUiElement.setTarget("_blank");
            anchorUiElement.setHref(raceViewerUrl);
        }
        setupRaceState(race.getTrackingState(), race.getViewState());
        getRaceNameUiElement().setInnerText(race.getRaceName());
        setupRaceStart(race.getStart());
        setElement(anchorUiElement);
    }
    
    private void setupRaceState(RaceTrackingState trackingState, RaceViewState viewState) {
        // boolean isUntrackedRace = trackingState != RaceTrackingState.TRACKED_VALID_DATA;
        boolean isUntrackedRace = isUntrackedRace(trackingState);
        if (viewState == RaceViewState.RUNNING) {
            anchorUiElement.addClassName(getRaceLiveStyleName());
            getRaceStateUiElement().setInnerText(isUntrackedRace ? I18N.live() : I18N.actionWatch());
        } else if (viewState == RaceViewState.FINISHED) {
            getRaceStateUiElement().setInnerText(isUntrackedRace ? I18N.raceIsFinished() : I18N.actionAnalyze());
        } else {
            anchorUiElement.addClassName(getRacePlannedStyleName());
            if (viewState == RaceViewState.SCHEDULED) getRaceStateUiElement().setInnerText(I18N.raceIsPlanned());
            else getRaceStateUiElement().setInnerText(viewState.getLabel());
        }
        setStyleName(anchorUiElement, getRaceUntrackedStyleName(), isUntrackedRace);
    }
    
    private void setupRaceStart(Date startDate) {
        if (startDate != null) {
            TimePoint range = now().plus(Duration.ONE_HOUR.times(16)), start = new MillisecondsTimePoint(startDate);
            boolean showTime = start.after(now()) && start.before(range);
            DateTimeFormatRenderer renderer = showTime ? shortTimeFormatter : weekdayMonthAbbrDayDateFormatter;
            getRaceDateUiElement().setInnerText(renderer.render(startDate));
        }
    }

    protected abstract AnchorElement getMainUiElement();
    
    protected abstract Element getRaceNameUiElement();
    
    protected abstract Element getRaceStateUiElement();
    
    protected abstract Element getRaceDateUiElement();
    
    protected abstract String getRaceLiveStyleName();
    
    protected abstract String getRacePlannedStyleName();
    
    protected abstract String getRaceUntrackedStyleName();
    
    // TODO: As long as there is no mobile race viewer, show all races as untracked
    //       This is a temporary method to be able to fulfill this requirement
    protected boolean isUntrackedRace(RaceTrackingState trackingState) {
        return trackingState != RaceTrackingState.TRACKED_VALID_DATA;
    }
}

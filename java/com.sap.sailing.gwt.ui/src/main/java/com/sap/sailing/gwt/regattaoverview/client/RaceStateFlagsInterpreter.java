package com.sap.sailing.gwt.regattaoverview.client;

import com.sap.sailing.domain.common.racelog.utils.RaceStateFlagsInterpretationTemplate;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RaceStateFlagsInterpreter extends RaceStateFlagsInterpretationTemplate {
    
    private StringMessages stringMessages;
    
    public RaceStateFlagsInterpreter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    protected String getTextForRunningRace() {
        return stringMessages.raceIsRunning();
    }

    @Override
    protected String getTextForFinishingRace() {
        return stringMessages.raceIsFinishing();
    }

    @Override
    protected String getTextForFinishedRace() {
        return stringMessages.raceIsFinished();
    }

    @Override
    protected String getTextForScheduledRace() {
        return stringMessages.raceIsScheduled();
    }

    @Override
    protected String getTextForRaceInStartphase() {
        return stringMessages.raceIsInStartphase();
    }

    @Override
    protected String getTextForUnscheduledRace() {
        return stringMessages.noStarttimeAnnouncedYet();
    }

    @Override
    protected String getTextForFirstSubstituteFlag() {
        return stringMessages.generalRecall();
    }

    @Override
    protected String getTextForAnsweringPennantFlag() {
        return stringMessages.startPostponed();
    }

    @Override
    protected String getTextForAnsweringPennantWithAlphaFlags() {
        return stringMessages.startPostponed();
    }

    @Override
    protected String getTextForAnsweringPennantWithHotelFlags() {
        return stringMessages.startPostponed();
    }

    @Override
    protected String getTextForNovemberFlag() {
        return stringMessages.raceAbandoned();
    }

    @Override
    protected String getTextForNovemberWithAlphaFlags() {
        return stringMessages.raceAbandoned();
    }

    @Override
    protected String getTextForNovemberWithHotelFlags() {
        return stringMessages.raceAbandoned();
    }

}

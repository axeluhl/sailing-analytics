package com.sap.sailing.gwt.ui.regattaoverview;

import com.sap.sailing.domain.common.racelog.utils.FlagAlphabetInterpretationTemplate;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class FlagAlphabetInterpreter extends FlagAlphabetInterpretationTemplate {
    
    private StringMessages stringMessages;
    
    public FlagAlphabetInterpreter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    protected String getTextForRunningRace() {
        return stringMessages.raceIsRunning();
    }

    @Override
    protected String getTextForRunningRaceWithEarlyStarter() {
        return stringMessages.raceIsRunningWithEarlyStarters();
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
        return stringMessages.startPostponedNoMoreRacingToday();
    }

    @Override
    protected String getTextForAnsweringPennantWithHotelFlags() {
        return stringMessages.startPostponedFurtherSignalsAshore();
    }

    @Override
    protected String getTextForNovemberFlag() {
        return stringMessages.raceAbandoned();
    }

    @Override
    protected String getTextForNovemberWithAlphaFlags() {
        return stringMessages.raceAbandonedNoMoreRacingToday();
    }

    @Override
    protected String getTextForNovemberWithHotelFlags() {
        return stringMessages.raceAbandonedFurtherSignalsAshore();
    }

}

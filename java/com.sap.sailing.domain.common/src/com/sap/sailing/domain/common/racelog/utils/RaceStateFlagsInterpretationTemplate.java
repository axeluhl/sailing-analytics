package com.sap.sailing.domain.common.racelog.utils;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public abstract class RaceStateFlagsInterpretationTemplate {

    public String getMeaningOfRaceStateAndFlags(RaceLogRaceStatus status, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        String statusText = "";
        
        if (status == RaceLogRaceStatus.RUNNING) {
            statusText = getTextForRunningRace();
        } else if (status == RaceLogRaceStatus.FINISHING) {
            statusText = getTextForFinishingRace();
        } else if (status == RaceLogRaceStatus.FINISHED) {
            statusText = getTextForFinishedRace();
        } else if (status == RaceLogRaceStatus.SCHEDULED) {
            statusText = getTextForScheduledRace();
        } else if (status == RaceLogRaceStatus.STARTPHASE) {
            statusText = getTextForRaceInStartphase();
        } else if (status == RaceLogRaceStatus.UNSCHEDULED || status == RaceLogRaceStatus.PRESCHEDULED) {
            statusText = getTextForUnscheduledRace();
            if (upperFlag != null) {
                if (upperFlag == Flags.FIRSTSUBSTITUTE) {
                    statusText = getTextForFirstSubstituteFlag();
                } else if (upperFlag == Flags.AP && lowerFlag == Flags.ALPHA && isDisplayed) {
                    statusText = getTextForAnsweringPennantWithAlphaFlags();
                } else if (upperFlag == Flags.AP && lowerFlag == Flags.HOTEL && isDisplayed) {
                    statusText = getTextForAnsweringPennantWithHotelFlags();
                } else if (upperFlag == Flags.AP && isDisplayed) {
                    statusText = getTextForAnsweringPennantFlag();
                } else if (upperFlag == Flags.NOVEMBER && lowerFlag == Flags.ALPHA && isDisplayed) {
                    statusText = getTextForNovemberWithAlphaFlags();
                } else if (upperFlag == Flags.NOVEMBER && lowerFlag == Flags.HOTEL && isDisplayed) {
                    statusText = getTextForNovemberWithHotelFlags();
                } else if (upperFlag == Flags.NOVEMBER && isDisplayed) {
                    statusText = getTextForNovemberFlag();
                }
            }
        }
        return statusText;
    }
    
    protected abstract String getTextForRunningRace();
    
    protected abstract String getTextForFinishingRace();
    
    protected abstract String getTextForFinishedRace();
    
    protected abstract String getTextForScheduledRace();
    
    protected abstract String getTextForRaceInStartphase();
    
    protected abstract String getTextForUnscheduledRace();
    
    protected abstract String getTextForFirstSubstituteFlag();
    
    protected abstract String getTextForAnsweringPennantFlag();
    
    protected abstract String getTextForAnsweringPennantWithAlphaFlags();
    
    protected abstract String getTextForAnsweringPennantWithHotelFlags();
    
    protected abstract String getTextForNovemberFlag();
    
    protected abstract String getTextForNovemberWithAlphaFlags();
    
    protected abstract String getTextForNovemberWithHotelFlags();
}

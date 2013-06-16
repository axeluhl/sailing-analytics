package com.sap.sailing.domain.common.racelog.utils;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public abstract class FlagAlphabetInterpretationTemplate {

    public String getMeaningOfRaceStateAndFlags(RaceLogRaceStatus status, Flags upperFlag, Flags lowerFlag, boolean isDisplayed) {
        String statusText = "";
        if (status.equals(RaceLogRaceStatus.RUNNING) && upperFlag.equals(Flags.XRAY) && isDisplayed) {
            statusText = getTextForRunningRaceWithEarlyStarter();
        } else if (status.equals(RaceLogRaceStatus.RUNNING) && upperFlag.equals(Flags.XRAY) && !isDisplayed) {
            statusText = getTextForRunningRace();
        } else if (status.equals(RaceLogRaceStatus.RUNNING)) {
            statusText = getTextForRunningRace();
        } else if (status.equals(RaceLogRaceStatus.FINISHING)) {
            statusText = getTextForFinishingRace();
        } else if (status.equals(RaceLogRaceStatus.FINISHED)) {
            statusText = getTextForFinishedRace();
        } else if (status.equals(RaceLogRaceStatus.SCHEDULED)) {
            statusText = getTextForScheduledRace();
        } else if (status.equals(RaceLogRaceStatus.STARTPHASE)) {
            statusText = getTextForRaceInStartphase();
        } else if (status.equals(RaceLogRaceStatus.UNSCHEDULED)) {
            statusText = getTextForUnscheduledRace();
            if (upperFlag != null) {
                if (upperFlag.equals(Flags.FIRSTSUBSTITUTE)) {
                    statusText = getTextForFirstSubstituteFlag();
                } else if (upperFlag.equals(Flags.AP) && isDisplayed) {
                    statusText = getTextForAnsweringPennantFlag();
                } else if (upperFlag.equals(Flags.AP) && lowerFlag.equals(Flags.ALPHA) && isDisplayed) {
                    statusText = getTextForAnsweringPennantWithAlphaFlags();
                } else if (upperFlag.equals(Flags.AP) && lowerFlag.equals(Flags.HOTEL) && isDisplayed) {
                    statusText = getTextForAnsweringPennantWithHotelFlags();
                } else if (upperFlag.equals(Flags.NOVEMBER) && isDisplayed) {
                    statusText = getTextForNovemberFlag();
                } else if (upperFlag.equals(Flags.NOVEMBER) && lowerFlag.equals(Flags.ALPHA) && isDisplayed) {
                    statusText = getTextForNovemberWithAlphaFlags();
                } else if (upperFlag.equals(Flags.NOVEMBER) && lowerFlag.equals(Flags.HOTEL) && isDisplayed) {
                    statusText = getTextForNovemberWithHotelFlags();
                }
            }
        }
        return statusText;
    }
    
    protected abstract String getTextForRunningRace();
    
    protected abstract String getTextForRunningRaceWithEarlyStarter();
    
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

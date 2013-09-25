package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class AbortingFlagFinder extends RaceLogAnalyzer<RaceLogFlagEvent> {

    public AbortingFlagFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogFlagEvent performAnalysis() {
        if (raceLog.getCurrentPassId() <= RaceLog.DefaultPassId) {
            return null;
        }

        int lastButOnePass = raceLog.getCurrentPassId() - 1;
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event.getPassId() == lastButOnePass && event instanceof RaceLogFlagEvent) {
                RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                if (isDisplayedAbortingFlag(flagEvent)) {
                    return flagEvent;
                }
            }
        }

        return null;
    }

    private static boolean isDisplayedAbortingFlag(RaceLogFlagEvent flagEvent) {
        return (flagEvent.getUpperFlag().equals(Flags.AP) && flagEvent.isDisplayed())
                || (flagEvent.getUpperFlag().equals(Flags.NOVEMBER) && flagEvent.isDisplayed())
                || (flagEvent.getUpperFlag().equals(Flags.FIRSTSUBSTITUTE) && flagEvent.isDisplayed());
    }

}

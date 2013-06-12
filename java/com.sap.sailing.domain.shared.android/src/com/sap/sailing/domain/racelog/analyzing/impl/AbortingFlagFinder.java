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
    protected RaceLogFlagEvent performAnalyzation() {
        RaceLogFlagEvent newFlagEvent = null;

        if (raceLog.getCurrentPassId() > RaceLog.DefaultPassId) {
            int relevantPassId = raceLog.getCurrentPassId() - 1;
            for (RaceLogEvent event : getAllEvents()) {
                if (event.getPassId() == relevantPassId) {
                    if (event instanceof RaceLogFlagEvent) {
                        RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                        if (isAbortingFlag(flagEvent)) {
                            newFlagEvent = flagEvent;
                        }
                    }
                }
            }
        }

        return newFlagEvent;
    }

    private static boolean isAbortingFlag(RaceLogFlagEvent flagEvent) {
        return (flagEvent.getUpperFlag().equals(Flags.AP) && flagEvent.isDisplayed())
                || (flagEvent.getUpperFlag().equals(Flags.NOVEMBER) && flagEvent.isDisplayed())
                || (flagEvent.getUpperFlag().equals(Flags.FIRSTSUBSTITUTE) && flagEvent.isDisplayed());
    }

}

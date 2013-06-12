package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class AbortingFlagFinder extends RaceLogAnalyzer {

    public AbortingFlagFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public RaceLogFlagEvent getAbortingFlagEvent() {

        RaceLogFlagEvent newFlagEvent = null;

        this.raceLog.lockForRead();
        try {
            newFlagEvent = searchForAbortingFlagEvent();
        } finally {
            this.raceLog.unlockAfterRead();
        }

        return newFlagEvent;
    }

    private RaceLogFlagEvent searchForAbortingFlagEvent() {
        RaceLogFlagEvent newFlagEvent = null;

        if (raceLog.getCurrentPassId() > 0) {
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

    private boolean isAbortingFlag(RaceLogFlagEvent flagEvent) {
        return (flagEvent.getUpperFlag().equals(Flags.AP) && flagEvent.isDisplayed())
                || (flagEvent.getUpperFlag().equals(Flags.NOVEMBER) && flagEvent.isDisplayed())
                || (flagEvent.getUpperFlag().equals(Flags.FIRSTSUBSTITUTE) && flagEvent.isDisplayed());
    }

}

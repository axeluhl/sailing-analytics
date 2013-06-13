package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class IndividualRecallFinder extends RaceLogAnalyzer {

    public IndividualRecallFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public TimePoint getIndividualRecallDisplayedTime() {
        TimePoint displayedTime = null;
        
        this.raceLog.lockForRead();
        try {
            displayedTime = searchForIndividualRecallDisplayedTime();
        } finally {
            this.raceLog.unlockAfterRead();
        }

        return displayedTime;
    }
    
    public TimePoint getIndividualRecallDisplayedRemovalTime() {
        TimePoint removalTime = null;
        
        this.raceLog.lockForRead();
        try {
            removalTime = searchForIndividualRecallRemovalTime();
        } finally {
            this.raceLog.unlockAfterRead();
        }

        return removalTime;
    }

    private TimePoint searchForIndividualRecallDisplayedTime() {
        TimePoint displayedTime = null;
        
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogFlagEvent) {
                RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                if (flagEvent.getUpperFlag().equals(Flags.XRAY) && flagEvent.isDisplayed()) {
                    displayedTime = flagEvent.getTimePoint();
                }
            }
        }
        
        return displayedTime;
    }
    
    private TimePoint searchForIndividualRecallRemovalTime() {
        TimePoint removalTime = null;
        
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogFlagEvent) {
                RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                if (flagEvent.getUpperFlag().equals(Flags.XRAY) && !flagEvent.isDisplayed()) {
                    removalTime = flagEvent.getTimePoint();
                }
            }
        }
        
        return removalTime;
    }


}

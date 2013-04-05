package com.sap.sailing.domain.racelog.analyzing.impl;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;

public class FinishPositioningListFinder extends RaceLogAnalyzer {

    public FinishPositioningListFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    public List<Competitor> getFinishPositioningList() {
        List<Competitor> lastFinishPositioningList = null;

        this.raceLog.lockForRead();
        try {
            lastFinishPositioningList = searchForLastFinishPositioningList();
        } finally {
            this.raceLog.unlockAfterRead();
        }
        
        return lastFinishPositioningList;
    }
    
    private List<Competitor> searchForLastFinishPositioningList() {
        List<Competitor> lastFinishPositioningList = null;
        
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof RaceLogFinishPositioningListChangedEvent) {
                RaceLogFinishPositioningListChangedEvent finishPositioningEvent = (RaceLogFinishPositioningListChangedEvent) event;
                lastFinishPositioningList = finishPositioningEvent.getInvolvedBoats();
            }
        }
        
        return lastFinishPositioningList;
    }
}

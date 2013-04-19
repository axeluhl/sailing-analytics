package com.sap.sailing.domain.racelog.analyzing.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;

public class FinishPositioningListFinder extends RaceLogAnalyzer {

    public FinishPositioningListFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    public List<Triple<Serializable, String, MaxPointsReason>> getFinishPositioningList() {
        List<Triple<Serializable, String, MaxPointsReason>> lastFinishPositioningList = null;

        this.raceLog.lockForRead();
        try {
            lastFinishPositioningList = searchForLastFinishPositioningList();
        } finally {
            this.raceLog.unlockAfterRead();
        }
        
        return lastFinishPositioningList;
    }
    
    private List<Triple<Serializable, String, MaxPointsReason>> searchForLastFinishPositioningList() {
        List<Triple<Serializable, String, MaxPointsReason>> lastFinishPositioningList = null;
        
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof RaceLogFinishPositioningListChangedEvent) {
                RaceLogFinishPositioningListChangedEvent finishPositioningEvent = (RaceLogFinishPositioningListChangedEvent) event;
                lastFinishPositioningList = finishPositioningEvent.getPositionedCompetitors();
            }
        }
        
        return lastFinishPositioningList;
    }
}

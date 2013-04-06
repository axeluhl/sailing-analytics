package com.sap.sailing.domain.racelog.analyzing.impl;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;

public class FinishPositioningListFinder extends RaceLogAnalyzer {

    public FinishPositioningListFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    public List<Pair<Competitor, MaxPointsReason>> getFinishPositioningList() {
        List<Pair<Competitor, MaxPointsReason>> lastFinishPositioningList = null;

        this.raceLog.lockForRead();
        try {
            lastFinishPositioningList = searchForLastFinishPositioningList();
        } finally {
            this.raceLog.unlockAfterRead();
        }
        
        return lastFinishPositioningList;
    }
    
    private List<Pair<Competitor, MaxPointsReason>> searchForLastFinishPositioningList() {
        List<Pair<Competitor, MaxPointsReason>> lastFinishPositioningList = null;
        
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof RaceLogFinishPositioningListChangedEvent) {
                RaceLogFinishPositioningListChangedEvent finishPositioningEvent = (RaceLogFinishPositioningListChangedEvent) event;
                lastFinishPositioningList = finishPositioningEvent.getPositionedCompetitors();
            }
        }
        
        return lastFinishPositioningList;
    }
}

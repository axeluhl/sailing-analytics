package com.sap.sailing.domain.racelog.analyzing.impl;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningListChangedEvent;

public class FinishPositioningListFinder extends RaceLogAnalyzer<List<Triple<Serializable, String, MaxPointsReason>>> {

    public FinishPositioningListFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected List<Triple<Serializable, String, MaxPointsReason>> performAnalyzation() {
        List<Triple<Serializable, String, MaxPointsReason>> lastFinishPositioningList = null;
        
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogFinishPositioningListChangedEvent) {
                RaceLogFinishPositioningListChangedEvent finishPositioningEvent = (RaceLogFinishPositioningListChangedEvent) event;
                lastFinishPositioningList = finishPositioningEvent.getPositionedCompetitors();
            }
        }
        
        return lastFinishPositioningList;
    }
}

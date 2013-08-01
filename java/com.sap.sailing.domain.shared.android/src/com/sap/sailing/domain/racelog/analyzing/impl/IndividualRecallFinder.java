package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public abstract class IndividualRecallFinder extends RaceLogAnalyzer<TimePoint> {

    public IndividualRecallFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    protected abstract boolean isRelevant(RaceLogFlagEvent flagEvent);
    
    @Override
    protected TimePoint performAnalyzation() {
        for (RaceLogEvent event : getPassEvents()) {
            if (event instanceof RaceLogFlagEvent) {
                RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                if (isRelevant(flagEvent)) {
                    return flagEvent.getTimePoint();
                }
            }
        }
        
        return null;
    }

}

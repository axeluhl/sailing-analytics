package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sse.common.TimePoint;

public abstract class IndividualRecallFinder extends RaceLogAnalyzer<TimePoint> {

    public IndividualRecallFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    protected abstract boolean isRelevant(RaceLogFlagEvent flagEvent);
    
    @Override
    protected TimePoint performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogFlagEvent) {
                RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                if (isRelevant(flagEvent)) {
                    return flagEvent.getLogicalTimePoint();
                }
            }
        }
        
        return null;
    }

}

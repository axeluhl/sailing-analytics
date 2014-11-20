package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sse.common.TimePoint;

public class IndividualRecallRemovedFinder extends IndividualRecallFinder {

    private final TimePoint at;
    
    public IndividualRecallRemovedFinder(RaceLog raceLog) {
        this(raceLog, null);
    }
    
    public IndividualRecallRemovedFinder(RaceLog raceLog, TimePoint at) {
        super(raceLog);
        this.at = at;
    }
    
    protected boolean isRelevant(RaceLogFlagEvent flagEvent) {
        boolean disregardBecauseAfterAt = at != null && flagEvent.getTimePoint().after(at);
        return ! disregardBecauseAfterAt && flagEvent.getUpperFlag().equals(Flags.XRAY) && !flagEvent.isDisplayed();
    }

}

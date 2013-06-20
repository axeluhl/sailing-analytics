package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class IndividualRecallRemovedFinder extends IndividualRecallFinder {

    public IndividualRecallRemovedFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    protected boolean isRelevant(RaceLogFlagEvent flagEvent) {
        return flagEvent.getUpperFlag().equals(Flags.XRAY) && !flagEvent.isDisplayed();
    }

}

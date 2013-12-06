package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;

public class IsIndividualRecallDisplayedAnalyzer extends RaceLogAnalyzer<Boolean> {

    private final IndividualRecallFinder displayedFinder;
    private final IndividualRecallFinder removedFinder;
    
    public IsIndividualRecallDisplayedAnalyzer(RaceLog raceLog) {
        this(raceLog, new IndividualRecallDisplayedFinder(raceLog), new IndividualRecallRemovedFinder(raceLog));
    }

    public IsIndividualRecallDisplayedAnalyzer(RaceLog raceLog, IndividualRecallDisplayedFinder displayedFinder,
            IndividualRecallRemovedFinder removedFinder) {
        super(raceLog);
        this.displayedFinder = displayedFinder;
        this.removedFinder = removedFinder;
    }

    @Override
    protected Boolean performAnalysis() {
        TimePoint displayedAt = displayedFinder.analyze();
        TimePoint removedAt = removedFinder.analyze();
        if (displayedAt != null) {
            if (removedAt == null || displayedAt.after(removedAt)) {
                return true;
            }
        }
        return false;
    }

}

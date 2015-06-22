package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sse.common.TimePoint;

public class IsIndividualRecallDisplayedAnalyzer extends RaceLogAnalyzer<Boolean> {

    private final IndividualRecallFinder displayedFinder;
    private final IndividualRecallFinder removedFinder;
    
    public IsIndividualRecallDisplayedAnalyzer(RaceLog raceLog) {
        this(raceLog, null);
    }
    
    public IsIndividualRecallDisplayedAnalyzer(RaceLog raceLog, TimePoint at) {
        this(raceLog, new IndividualRecallDisplayedFinder(raceLog, at), new IndividualRecallRemovedFinder(raceLog, at), at);
    }

    public IsIndividualRecallDisplayedAnalyzer(RaceLog raceLog, IndividualRecallDisplayedFinder displayedFinder,
            IndividualRecallRemovedFinder removedFinder, TimePoint at) {
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

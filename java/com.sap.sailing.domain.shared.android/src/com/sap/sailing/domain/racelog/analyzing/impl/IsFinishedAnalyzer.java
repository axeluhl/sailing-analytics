package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sse.common.TimePoint;

public class IsFinishedAnalyzer extends RaceLogAnalyzer<Boolean> {

    private final FinishedTimeFinder finishedTimeFinder;
    private final TimePoint at;
    
    public IsFinishedAnalyzer(RaceLog raceLog) {
        this(raceLog, null);
    }
    
    public IsFinishedAnalyzer(RaceLog raceLog, TimePoint at) {
        this(raceLog, new FinishedTimeFinder(raceLog), at);
    }
    
    public IsFinishedAnalyzer(RaceLog raceLog, FinishedTimeFinder finishedTimeFinder, TimePoint at) {
        super(raceLog);
        this.at = at;
        this.finishedTimeFinder = finishedTimeFinder;
    }

    @Override
    protected Boolean performAnalysis() {
        TimePoint displayedAt = finishedTimeFinder.analyze();
        if (displayedAt != null && at != null && displayedAt.before(at)) {
            return true;
        }
        return false;
    }

}

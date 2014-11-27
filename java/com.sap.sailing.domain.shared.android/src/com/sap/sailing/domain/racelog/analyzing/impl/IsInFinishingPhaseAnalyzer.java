package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sse.common.TimePoint;

public class IsInFinishingPhaseAnalyzer extends RaceLogAnalyzer<Boolean> {

    private final FinishingTimeFinder finishingTimeFinder;
    private final TimePoint at;
    
    public IsInFinishingPhaseAnalyzer(RaceLog raceLog) {
        this(raceLog, null);
    }
    
    public IsInFinishingPhaseAnalyzer(RaceLog raceLog, TimePoint at) {
        this(raceLog, new FinishingTimeFinder(raceLog), at);
    }
    
    public IsInFinishingPhaseAnalyzer(RaceLog raceLog, FinishingTimeFinder finishingTimeFinder, TimePoint at) {
        super(raceLog);
        this.at = at;
        this.finishingTimeFinder = finishingTimeFinder;
    }

    @Override
    protected Boolean performAnalysis() {
        TimePoint displayedAt = finishingTimeFinder.analyze();
        if (displayedAt != null && at != null && displayedAt.before(at)) {
            return true;
        }
        return false;
    }

}

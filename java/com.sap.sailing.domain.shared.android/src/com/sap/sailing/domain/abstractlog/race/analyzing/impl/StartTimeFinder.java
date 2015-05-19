package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class StartTimeFinder extends RaceLogAnalyzer<Pair<StartTimeFinderStatus, TimePoint>> {

    public StartTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Pair<StartTimeFinderStatus, TimePoint> performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogStartTimeEvent) {
                return new Pair<StartTimeFinderStatus, TimePoint>(StartTimeFinderStatus.STARTTIME_FOUND,
                        ((RaceLogStartTimeEvent) event).getStartTime());
            } else if (event instanceof RaceLogDependentStartTimeEvent) {
                return new Pair<StartTimeFinderStatus, TimePoint>(StartTimeFinderStatus.STARTTIME_DEPENDENT, null);
            }
        }

        return new Pair<StartTimeFinderStatus, TimePoint>(StartTimeFinderStatus.STARTTIME_UNKNOWN, null);
    }

}

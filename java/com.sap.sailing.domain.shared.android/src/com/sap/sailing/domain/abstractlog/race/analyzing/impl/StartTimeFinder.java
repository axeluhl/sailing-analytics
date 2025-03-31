package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderResult.ResolutionFailed;
import com.sap.sse.common.TimePoint;

public class StartTimeFinder extends RaceLogAnalyzer<StartTimeFinderResult> {

    private final RaceLogResolver resolver;

    public StartTimeFinder(RaceLogResolver resolver, RaceLog raceLog) {
        super(raceLog);
        this.resolver = resolver;
    }

    @Override
    protected StartTimeFinderResult performAnalysis() {
        return analyze(new ArrayList<SimpleRaceLogIdentifier>());
    }

    public StartTimeFinderResult analyze(Iterable<SimpleRaceLogIdentifier> dependingOnRaces) {
        log.lockForRead();
        try {
            for (RaceLogEvent event : getPassEventsDescending()) {
                if (event instanceof RaceLogStartTimeEvent) {
                    final RaceLogStartTimeEvent startTimeEvent = (RaceLogStartTimeEvent) event;
                    final TimePoint startTime = startTimeEvent.getStartTime();
                    return new StartTimeFinderResult(dependingOnRaces, startTime, null,
                            startTimeEvent.getCourseAreaId());
                } else if (event instanceof RaceLogDependentStartTimeEvent) {
                    final DependentStartTimeResolver dependentStartTimeResolver = new DependentStartTimeResolver(resolver);
                    return dependentStartTimeResolver.internalResolve((RaceLogDependentStartTimeEvent) event, dependingOnRaces);
                }
            }
            return new StartTimeFinderResult(dependingOnRaces, null, null,
                    ResolutionFailed.NO_START_TIME_SET, /* courseAreaId */ null);
        } finally {
            log.unlockAfterRead();
        }
    }
}

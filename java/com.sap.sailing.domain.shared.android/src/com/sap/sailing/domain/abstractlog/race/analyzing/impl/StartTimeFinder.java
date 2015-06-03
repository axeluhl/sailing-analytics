package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.TimePoint;

public class StartTimeFinder extends RaceLogAnalyzer<StartTimeFinderResult> {

    private final RaceLogResolver resolver;
    private List<SimpleRaceLogIdentifier> dependingOnRaces;
    
    public StartTimeFinder(RaceLogResolver resolver, RaceLog raceLog) {
        super(raceLog);
        this.resolver = resolver;
    }

    public StartTimeFinder(RaceLogResolver raceLogResolver, RaceLog raceLog,
            List<SimpleRaceLogIdentifier> dependingOnRaces) {
        super(raceLog);
        this.resolver = raceLogResolver;
        this.dependingOnRaces = dependingOnRaces;
    }

    @Override
    protected StartTimeFinderResult performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogStartTimeEvent) {
                TimePoint startTime = ((RaceLogStartTimeEvent) event).getStartTime();
                return new StartTimeFinderResult(startTime);
            } else if (event instanceof RaceLogDependentStartTimeEvent) {
                DependentStartTimeResolver dependentStartTimeResolver = new DependentStartTimeResolver(resolver);
                return dependentStartTimeResolver.internal_resolve((RaceLogDependentStartTimeEvent) event, dependingOnRaces);
            }
        }
        return null;
    }
}

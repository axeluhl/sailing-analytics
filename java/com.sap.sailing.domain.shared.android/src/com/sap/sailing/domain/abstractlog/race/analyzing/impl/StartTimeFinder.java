package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sse.common.TimePoint;

public class StartTimeFinder extends RaceLogAnalyzer<TimePoint> {

    private final RaceLogResolver resolver;
    
    public StartTimeFinder(RaceLogResolver resolver, RaceLog raceLog) {
        super(raceLog);
        this.resolver = resolver;
    }

    @Override
    protected TimePoint performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogStartTimeEvent) {
                return ((RaceLogStartTimeEvent) event).getStartTime();
            } else if (event instanceof RaceLogDependentStartTimeEvent) {
                DependentStartTimeResolver dependentStartTimeResolver = new DependentStartTimeResolver(resolver);
                return dependentStartTimeResolver.resolve((RaceLogDependentStartTimeEvent) event);
            }
        }
        return null;
    }
}

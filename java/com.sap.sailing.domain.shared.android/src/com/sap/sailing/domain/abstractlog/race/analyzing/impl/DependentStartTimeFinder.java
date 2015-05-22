package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sse.common.TimePoint;

public class DependentStartTimeFinder extends RaceLogAnalyzer<TimePoint> {

    private static final Logger logger = Logger.getLogger(DependentStartTimeFinder.class.getName());
    private final RaceLogResolver resolver;
    
    public DependentStartTimeFinder(RaceLogResolver resolver, RaceLog raceLog) {
        super(raceLog);
        this.resolver = resolver;
    }

    @Override
    protected TimePoint performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogStartTimeEvent) {
                return ((RaceLogStartTimeEvent) event).getStartTime();
            } else if (event instanceof RaceLogDependentStartTimeEvent) {
                
                try {
                    DependentStartTimeResolver dependentStartTimeResolver = new DependentStartTimeResolver(resolver);
                     return dependentStartTimeResolver.resolve((RaceLogDependentStartTimeEvent) event);
                } catch (RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException e) {
                    logger.warning("Regata Like Name of identifier doesn't match actual RegattaLikeName");
                    return null;
                }
            }
        }
        return null;
    }
}

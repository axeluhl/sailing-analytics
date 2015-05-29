package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class DependentStartTimeResolver {

    private RaceLogResolver raceLogResolver;

    public DependentStartTimeResolver(RaceLogResolver raceLogResolver) {
        this.raceLogResolver = raceLogResolver;
    }

    public TimePoint resolve(RaceLogDependentStartTimeEvent event) {
        SimpleRaceLogIdentifier identifier = event.getDependentOnRaceIdentifier();
        Duration startTimeDifference = event.getStartTimeDifference();

        RaceLog raceLog = raceLogResolver.resolve(identifier);
        StartTimeFinder dependentStartTimeFinder = new StartTimeFinder(raceLogResolver, raceLog);
        TimePoint startTimeOfDependentRace = dependentStartTimeFinder.analyze();
        
        if (startTimeOfDependentRace == null){
            return null;
        }

        return startTimeOfDependentRace.plus(startTimeDifference);
    }

}

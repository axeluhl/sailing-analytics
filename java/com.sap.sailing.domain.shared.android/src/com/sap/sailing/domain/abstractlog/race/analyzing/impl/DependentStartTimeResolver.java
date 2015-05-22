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

    TimePoint resolve(RaceLogDependentStartTimeEvent event) throws RegataLikeNameOfIdentifierDoesntMatchActualRegattaLikeNameException {
        SimpleRaceLogIdentifier identifier = event.getDependentOnRaceIdentifier();
        Duration startTimeDifference = event.getStartTimeDifference();

        RaceLog raceLog = raceLogResolver.resolve(identifier);
        DependentStartTimeFinder dependentStartTimeFinder = new DependentStartTimeFinder(raceLogResolver, raceLog);
        TimePoint startTimeOfDependentRace = dependentStartTimeFinder.analyze();

        return startTimeOfDependentRace.plus(startTimeDifference);
    }

}

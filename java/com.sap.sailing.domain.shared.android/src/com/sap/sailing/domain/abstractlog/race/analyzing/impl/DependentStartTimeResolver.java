package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.Duration;

public class DependentStartTimeResolver {

    private RaceLogResolver raceLogResolver;

    public DependentStartTimeResolver(RaceLogResolver raceLogResolver) {
        this.raceLogResolver = raceLogResolver;

    }

    public StartTimeFinderResult resolve(RaceLogDependentStartTimeEvent event) {
        List<SimpleRaceLogIdentifier> dependingOnRaces = new ArrayList<SimpleRaceLogIdentifier>();

        return internal_resolve(event, dependingOnRaces);
    }

    StartTimeFinderResult internal_resolve(RaceLogDependentStartTimeEvent event,
            List<SimpleRaceLogIdentifier> dependingOnRaces) {
        
        SimpleRaceLogIdentifier identifier = event.getDependentOnRaceIdentifier();
        Duration startTimeDifference = event.getStartTimeDifference();

        RaceLog raceLog = raceLogResolver.resolve(identifier);

        dependingOnRaces.add(identifier);

        if (containsCycle(dependingOnRaces)) {
            return new StartTimeFinderResult(dependingOnRaces, null);
        }

        StartTimeFinder dependentStartTimeFinder = new StartTimeFinder(raceLogResolver, raceLog);
        StartTimeFinderResult resultOfDependentRace = dependentStartTimeFinder.analyze(dependingOnRaces);

        if (resultOfDependentRace.getStartTime() == null) {
            return new StartTimeFinderResult(dependingOnRaces, null);
        }

        resultOfDependentRace.addToStartTime(startTimeDifference);
        return resultOfDependentRace;
    }

    private boolean containsCycle(List<SimpleRaceLogIdentifier> dependingOnRaces) {
        HashSet<SimpleRaceLogIdentifier> dependingOnRacesHashSet = new HashSet<SimpleRaceLogIdentifier>(
                dependingOnRaces);
        return (dependingOnRacesHashSet.size() < dependingOnRaces.size());
    }

}

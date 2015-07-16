package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;

public class DependentStartTimeResolver {

    private RaceLogResolver raceLogResolver;

    public DependentStartTimeResolver(RaceLogResolver raceLogResolver) {
        this.raceLogResolver = raceLogResolver;
    }

    public StartTimeFinderResult resolve(RaceLogDependentStartTimeEvent event) {
        List<SimpleRaceLogIdentifier> dependingOnRaces = new ArrayList<>();
        return internalResolve(event, dependingOnRaces);
    }

    StartTimeFinderResult internalResolve(RaceLogDependentStartTimeEvent event,
            Iterable<SimpleRaceLogIdentifier> dependingOnRaces) {
        SimpleRaceLogIdentifier identifier = event.getDependentOnRaceIdentifier();
        Duration startTimeDifference = event.getStartTimeDifference();
        RaceLog raceLog = raceLogResolver.resolve(identifier);

        StartTimeFinderResult result;
        if (raceLog == null) {
            result = new StartTimeFinderResult(dependingOnRaces, null, startTimeDifference);
        } else {
            List<SimpleRaceLogIdentifier> extendedDependingOnRaces = new ArrayList<>();
            Util.addAll(dependingOnRaces, extendedDependingOnRaces);
            extendedDependingOnRaces.add(identifier);
            if (containsCycle(extendedDependingOnRaces)) {
                result = new StartTimeFinderResult(extendedDependingOnRaces, null, null);
            } else {
                StartTimeFinder dependentStartTimeFinder = new StartTimeFinder(raceLogResolver, raceLog);
                StartTimeFinderResult resultOfDependentRace = dependentStartTimeFinder
                        .analyze(extendedDependingOnRaces);
                if (resultOfDependentRace.getStartTime() == null) {
                    result = resultOfDependentRace;
                } else {
                    result = new StartTimeFinderResult(resultOfDependentRace.getRacesDependingOn(),
                            resultOfDependentRace.getStartTime().plus(startTimeDifference), startTimeDifference);
                }
            }
        }
        return result;
    }

    private boolean containsCycle(Iterable<SimpleRaceLogIdentifier> dependingOnRaces) {
        HashSet<SimpleRaceLogIdentifier> dependingOnRacesHashSet = new HashSet<>();
        Util.addAll(dependingOnRaces, dependingOnRacesHashSet);
        return (dependingOnRacesHashSet.size() < Util.size(dependingOnRaces));
    }

}

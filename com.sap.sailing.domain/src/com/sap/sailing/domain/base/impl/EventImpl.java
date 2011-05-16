package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;

public class EventImpl extends NamedImpl implements Event {
    private final Map<BoatClass, Collection<RaceDefinition>> races;
    
    public EventImpl(String name) {
        super(name);
        races = new HashMap<BoatClass, Collection<RaceDefinition>>();
    }

    @Override
    public Iterable<RaceDefinition> getAllRaces() {
        return new CompositeIterable<RaceDefinition>(races.values());
    }
    
    @Override
    public void addRace(RaceDefinition race) {
        Collection<RaceDefinition> rc = races.get(race.getBoatClass());
        if (rc == null) {
            rc = new ArrayList<RaceDefinition>();
            races.put(race.getBoatClass(), rc);
        }
        rc.add(race);
    }

    @Override
    public Iterable<RaceDefinition> getAllRaces(BoatClass boatClass) {
        Collection<RaceDefinition> racesInClass = races.get(boatClass);
        return racesInClass == null ? null : Collections.unmodifiableCollection(racesInClass);
    }

    @Override
    public Iterable<BoatClass> getClasses() {
        return races.keySet();
    }

    @Override
    public Iterable<Competitor> getCompetitors() {
        Set<Competitor> result = new HashSet<Competitor>();
        for (RaceDefinition race : getAllRaces()) {
            for (Competitor c : race.getCompetitors()) {
                result.add(c);
            }
        }
        return result;
    }

}

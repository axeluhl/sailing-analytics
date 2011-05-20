package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;

public class EventImpl extends NamedImpl implements Event {
    private final Collection<RaceDefinition> races;
    
    public EventImpl(String name) {
        super(name);
        races = new ArrayList<RaceDefinition>();
    }

    @Override
    public Iterable<RaceDefinition> getAllRaces() {
        return races;
    }
    
    @Override
    public void addRace(RaceDefinition race) {
        if (getBoatClass() != null && race.getBoatClass() != getBoatClass()) {
            throw new IllegalArgumentException("Boat class "+race.getBoatClass()+" doesn't match event's boat class "+getBoatClass());
        }
        races.add(race);
    }

    @Override
    public BoatClass getBoatClass() {
        Iterator<RaceDefinition> raceIter = getAllRaces().iterator();
        if (raceIter.hasNext()) {
            return raceIter.next().getBoatClass();
        } else {
            return null;
        }
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

package com.sap.sailing.domain.base.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class EventImpl extends NamedImpl implements Event {
    private static final long serialVersionUID = 6509564189552478869L;
    private final Set<RaceDefinition> races;
    private final BoatClass boatClass;
    
    public EventImpl(String name, BoatClass boatClass) {
        super(name+(boatClass==null?"":" ("+boatClass.getName()+")"));
        races = new HashSet<RaceDefinition>();
        this.boatClass = boatClass;
    }

    @Override
    public Iterable<RaceDefinition> getAllRaces() {
        return races;
    }
    
    @Override
    public RaceDefinition getRaceByName(String raceName) {
        Iterable<RaceDefinition> allRaces = getAllRaces();
        synchronized (allRaces) {
            for (RaceDefinition r : getAllRaces()) {
                if (r.getName().equals(raceName)) {
                    return r;
                }
            }
            return null;
        }
    }
    
    @Override
    public void addRace(RaceDefinition race) {
        if (getBoatClass() != null && race.getBoatClass() != getBoatClass()) {
            throw new IllegalArgumentException("Boat class "+race.getBoatClass()+" doesn't match event's boat class "+getBoatClass());
        }
        synchronized (races) {
            races.add(race);
        }
    }
    
    @Override
    public void removeRace(RaceDefinition race) {
        synchronized (races) {
            races.remove(race);
        }
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
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

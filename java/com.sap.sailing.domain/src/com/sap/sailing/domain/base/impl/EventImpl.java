package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.EventIdentifier;
import com.sap.sailing.domain.common.EventName;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class EventImpl extends NamedImpl implements Event {
    private static final long serialVersionUID = 6509564189552478869L;
    private final Set<RaceDefinition> races;
    private final BoatClass boatClass;
    private transient Set<EventListener> eventListeners;
    
    public EventImpl(String baseName, BoatClass boatClass) {
        super(baseName+(boatClass==null?"":" ("+boatClass.getName()+")"));
        races = new HashSet<RaceDefinition>();
        eventListeners = new HashSet<EventListener>();
        this.boatClass = boatClass;
    }
    
    @Override
    public String getBaseName() {
        String result;
        if (boatClass == null) {
            result = getName();
        } else {
            result = getName().substring(0, getName().length()-boatClass.getName().length()-3); // remove tralining boat class name and " (" and ")"
        }
        return result;
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        eventListeners = new HashSet<>();
    }

    @Override
    public Iterable<RaceDefinition> getAllRaces() {
        return races;
    }
    
    @Override
    public EventIdentifier getEventIdentifier() {
        return new EventName(getName());
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
        synchronized (eventListeners) {
            for (EventListener l : eventListeners) {
                l.raceAdded(this, race);
            }
        }
    }
    
    @Override
    public void removeRace(RaceDefinition race) {
        synchronized (races) {
            races.remove(race);
        }
        synchronized (eventListeners) {
            for (EventListener l : eventListeners) {
                l.raceRemoved(this, race);
            }
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

    @Override
    public void addEventListener(EventListener listener) {
        synchronized (eventListeners) {
            eventListeners.add(listener);
        }
    }

    @Override
    public void removeEventListener(EventListener listener) {
        synchronized (eventListeners) {
            eventListeners.remove(listener);
        }
    }

}

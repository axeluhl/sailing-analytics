package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class RegattaImpl extends NamedImpl implements Regatta {
    private static final long serialVersionUID = 6509564189552478869L;
    private final Set<RaceDefinition> races;
    private final BoatClass boatClass;
    private transient Set<RegattaListener> regattaListeners;
    
    public RegattaImpl(String baseName, BoatClass boatClass) {
        super(baseName+(boatClass==null?"":" ("+boatClass.getName()+")"));
        races = new HashSet<RaceDefinition>();
        regattaListeners = new HashSet<RegattaListener>();
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
        regattaListeners = new HashSet<>();
    }

    @Override
    public Iterable<RaceDefinition> getAllRaces() {
        return races;
    }
    
    @Override
    public RegattaIdentifier getRegattaIdentifier() {
        return new RegattaName(getName());
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
            throw new IllegalArgumentException("Boat class "+race.getBoatClass()+" doesn't match regatta's boat class "+getBoatClass());
        }
        synchronized (races) {
            races.add(race);
        }
        synchronized (regattaListeners) {
            for (RegattaListener l : regattaListeners) {
                l.raceAdded(this, race);
            }
        }
    }
    
    @Override
    public void removeRace(RaceDefinition race) {
        synchronized (races) {
            races.remove(race);
        }
        synchronized (regattaListeners) {
            for (RegattaListener l : regattaListeners) {
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
    public void addRegattaListener(RegattaListener listener) {
        synchronized (regattaListeners) {
            regattaListeners.add(listener);
        }
    }

    @Override
    public void removeRegattaListener(RegattaListener listener) {
        synchronized (regattaListeners) {
            regattaListeners.remove(listener);
        }
    }

}

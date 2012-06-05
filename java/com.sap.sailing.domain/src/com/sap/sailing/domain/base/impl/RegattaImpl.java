package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.RegattaListener;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;

public class RegattaImpl extends NamedImpl implements Regatta {
    private static final Logger logger = Logger.getLogger(RegattaImpl.class.getName());
    private static final long serialVersionUID = 6509564189552478869L;
    private final Set<RaceDefinition> races;
    private final BoatClass boatClass;
    private transient Set<RegattaListener> regattaListeners;
    private final Iterable<? extends Series> series;
    
    /**
     * Regattas may be constructed as implicit default regattas in which case they won't need to be stored
     * durably and don't contain valuable information worth being preserved; or they are constructed explicitly
     * with series and race columns in which case this data needs to be protected. This flag indicates whether
     * the data of this regatta needs to be maintained persistently.
     * 
     * @see #isPersistent
     */
    private final boolean persistent;
    
    /**
     * Constructs a regatta with a single default series with empty race column list, and a single default fleet which
     * is not {@link #isPersistent() marked for persistence}.
     * @param trackedRegattaRegistry TODO
     */
    public RegattaImpl(String baseName, BoatClass boatClass, TrackedRegattaRegistry trackedRegattaRegistry) {
        this(baseName, boatClass, Collections.singletonList(new SeriesImpl("Default", /* isMedal */false, Collections
                .singletonList(new FleetImpl("Default")), /* race column names */new ArrayList<String>(),
                trackedRegattaRegistry)), /* persistent */false);
    }

    /**
     * @param series
     *            all {@link Series} in this iterable will have their {@link Series#setRegatta(Regatta) regatta set} to
     *            this new regatta.
     */
    public RegattaImpl(String baseName, BoatClass boatClass, Iterable<? extends Series> series, boolean persistent) {
        super(baseName+(boatClass==null?"":" ("+boatClass.getName()+")"));
        races = new HashSet<RaceDefinition>();
        regattaListeners = new HashSet<RegattaListener>();
        this.boatClass = boatClass;
        this.series = series;
        for (Series s : series) {
            s.setRegatta(this);
        }
        this.persistent = persistent;
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
    
    @Override
    public boolean isPersistent() {
        return persistent;
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        regattaListeners = new HashSet<>();
    }

    @Override
    public Iterable<? extends Series> getSeries() {
        return series;
    }
    
    @Override
    public Series getSeriesByName(String name) {
        for (Series s : getSeries()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public Iterable<RaceDefinition> getAllRaces() {
        synchronized (races) {
            return new ArrayList<RaceDefinition>(races);
        }
    }
    
    @Override
    public RegattaIdentifier getRegattaIdentifier() {
        return new RegattaName(getName());
    }

    @Override
    public RaceDefinition getRaceByName(String raceName) {
        for (RaceDefinition r : getAllRaces()) {
            if (r.getName().equals(raceName)) {
                return r;
            }
        }
        return null;
    }
    
    @Override
    public void addRace(RaceDefinition race) {
        logger.info("Adding race "+race.getName()+" to regatta "+getName()+" ("+hashCode()+")");
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
            logger.info("Removing race "+race.getName()+" from regatta "+getName()+" ("+hashCode()+")");
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
